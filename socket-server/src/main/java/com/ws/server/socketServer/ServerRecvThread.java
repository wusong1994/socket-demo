package com.ws.server.socketServer;

import com.ws.common.enums.SocketMsgTypeEnum;
import com.ws.common.util.SocketUtil;
import com.ws.common.util.StreamUtil;
import com.ws.common.vo.SocketMsgDataVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * 客户端发送，服务端消息接收线程
 */
public class ServerRecvThread implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ServerRecvThread.class);

    private Socket socket;

    private volatile boolean isStop = false;

    public ServerRecvThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        //线程终止条件： 设置标志位为 true or socket 已关闭
        InputStream inputStream = null;
        DataInputStream dataInputStream = null;
        try {
            inputStream = socket.getInputStream();
            dataInputStream = new DataInputStream(inputStream);
            while (!isStop && !socket.isClosed()) {
                SocketMsgDataVo msgDataVo = SocketUtil.readMsgData(dataInputStream);
                if (msgDataVo.getType() == SocketMsgTypeEnum.HEART_BEAT.getType()) {
                    //客户端心跳监测不用处理
                    log.info("收到客户端心跳消息");
                }
            }
        } catch (IOException e) {
            log.error("服务端接收消息发生异常");
            e.printStackTrace();
        } finally {
            log.info("服务端旧接收线程已摧毁");
            StreamUtil.closeInputStream(dataInputStream);
            StreamUtil.closeInputStream(inputStream);
            SocketUtil.closeSocket(socket);
        }

    }

    public boolean getStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }
}
