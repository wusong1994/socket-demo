package com.ws.server.socketServer;

import com.ws.common.util.SocketUtil;
import com.ws.common.util.StreamUtil;
import com.ws.common.vo.SocketMsgDataVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @desc 服务端消息发送线程（区别： 所用队列不是安全, is not used）
 *
 */
public class ServerSendThread_bf implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ServerSendThread_bf.class);

    //不安全非阻塞队列
    private final Deque<SocketMsgDataVo> msgDeque = new ArrayDeque<>();

    private Socket socket;

    private volatile boolean isStop = false;

    public ServerSendThread_bf(Socket socket) {
        this.socket = socket;
    }

    public void add(SocketMsgDataVo msgDataVo) {
        synchronized (msgDeque) {
            msgDeque.add(msgDataVo);
            log.info("服务端消息入队列.....");
            if (msgDeque.element() == msgDataVo) {
                log.info("服务端消息入队列后唤醒线程.....");
                msgDeque.notify();  //唤醒线程
            }
        }
    }

    @Override
    public void run() {
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            while (!this.isStop) {
                synchronized (msgDeque) {
                    if (msgDeque.isEmpty()) {
                        log.info("服务端消息发送等待中.....");
                        msgDeque.wait();  //阻塞，让出cpu,较轮询不停频繁切换线程方式好
                    }
                }
                while (!msgDeque.isEmpty()) {
                    SocketMsgDataVo msgDataVo = msgDeque.remove();
                    msgDataVo.setType((byte) 1);
                    msgDataVo.setBody("from server：msg");
                    SocketUtil.writeMsgData(dataOutputStream, msgDataVo);
                    log.info("服务端消息已发送！");
                }
            }
        } catch (Exception e) {
            log.error("服务端消息发送异常");
            this.isStop = true;
            e.printStackTrace();
        } finally {
            log.info("服务端旧消息发送线程已摧毁");
            //释放资源
            msgDeque.clear();
            StreamUtil.closeOutputStream(dataOutputStream);
            StreamUtil.closeOutputStream(outputStream);
            SocketUtil.closeSocket(socket);
        }
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }
}
