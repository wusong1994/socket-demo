package com.ws.server.socketServer;

import com.ws.common.util.SocketUtil;
import com.ws.common.util.StreamUtil;
import com.ws.common.vo.SocketMsgDataVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @desc 服务端消息发送线程
 */
public class ServerSendThread implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ServerSendThread.class);

    //阻塞安全队列，设置队列容量，否则为无限大
    private final BlockingQueue<SocketMsgDataVo> msgQueue = new LinkedBlockingQueue<>(600);
    //等待放进队列秒数
    private final int WAIT_PUT_QUEUE_SECONDS = 10;

    private Socket socket;

    private volatile boolean isStop = false;

    public ServerSendThread(Socket socket) {
        this.socket = socket;
    }

    public boolean addMsgToQueue(SocketMsgDataVo msgDataVo) {
        try {
            //队列已满，阻塞直到未满放进元素, 废弃不要
            //msgQueue.put(msgDataVo);
            //队列已满，阻塞等待直到未满放进元素，超过10秒算了，返回false
            return msgQueue.offer(msgDataVo, WAIT_PUT_QUEUE_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void run() {
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            while (!this.isStop && !socket.isClosed()) {
                //队列为空阻塞，直到队列不为空，再取出
                SocketMsgDataVo msgDataVo = null;
                try {
                    msgDataVo = msgQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (msgDataVo != null && msgDataVo.getBody() != null) { //正文内容不能为空，否则不发
                    SocketUtil.writeMsgData(dataOutputStream, msgDataVo);
                    log.info("服务端消息已发送！");
                }
            }
        } catch (Exception e) {
            log.error("服务端消息发送异常");
            e.printStackTrace();
        } finally {
            log.info("服务端旧消息发送线程已摧毁");
            //释放资源
            msgQueue.clear();
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
