package com.ws.client.socketClient;

import com.ws.common.enums.SocketMsgTypeEnum;
import com.ws.common.util.SocketUtil;
import com.ws.common.util.StreamUtil;
import com.ws.common.vo.SocketMsgDataVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 客户端心跳检测、保持长连接状态
 */
public class ClientHeartBeatThread implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ClientHeartBeatThread.class);

    private Socket socket;
    private Object lockObject = new Object(); //锁对象，用于线程通讯，唤醒重试线程

    //3间隔多长时间发送一次心跳检测
    private int socketHeartIntervalTime;

    private volatile boolean isStop = false;

    public ClientHeartBeatThread(Socket socket, int socketHeartIntervalTime, Object lockObject) {
        this.socket = socket;
        this.socketHeartIntervalTime = socketHeartIntervalTime;
        this.lockObject = lockObject;
    }

    @Override
    public void run() {
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            //客户端心跳检测
            while (!this.isStop && !socket.isClosed()) {
                SocketMsgDataVo msgDataVo = new SocketMsgDataVo();
                msgDataVo.setType(SocketMsgTypeEnum.HEART_BEAT.getType());
                msgDataVo.setBody("from client：Is connect ok ?");
                if (msgDataVo != null && msgDataVo.getBody() != null) { //正文内容不能为空，否则不发)
                    SocketUtil.writeMsgData(dataOutputStream, msgDataVo);
                }
                try {
                    Thread.sleep(socketHeartIntervalTime * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            log.error("客户端心跳消息发送异常");
            e.printStackTrace();
        } finally {
            this.isStop = true;
            log.info("客户端旧心跳线程已摧毁");
            StreamUtil.closeOutputStream(dataOutputStream);
            StreamUtil.closeOutputStream(outputStream);
            SocketUtil.closeSocket(socket);
            //最后唤醒线程、重建连接
            synchronized (lockObject) {
                lockObject.notify();
            }
        }
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }
}
