package com.ws.client.socketClient;

import com.ws.common.enums.SocketMsgTypeEnum;
import com.ws.common.util.SocketUtil;
import com.ws.common.util.StreamUtil;
import com.ws.common.vo.RealTimeMsgEntity;
import com.ws.common.vo.SocketMsgDataVo;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;

/**
 * 客户端发送，服务端消息接收线程
 */
public class ClientRecvThread implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ClientRecvThread.class);

    private Socket socket;

    private volatile boolean isStop = false;

    public ClientRecvThread(Socket socket) {
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
                log.info("客户端收到消息:{}",msgDataVo.toString());
                //相对耗时，可以开线程来处理消息，否则影响后续消息接收处理速率
                if (msgDataVo.getType() == SocketMsgTypeEnum.REALTIME_PUSH_NOTIFY.getType()) {
                    //------------------此处业务逻辑处理-----------------------

                    //根据通讯协议，解析正文json数据
                    /*if (msgDataVo.getBody() != null) {
                        JSONObject jsonObject = JSONObject.fromObject(msgDataVo.getBody());
                        RealTimeMsgEntity realTimeMsgEntity = (RealTimeMsgEntity) JSONObject.toBean(jsonObject, RealTimeMsgEntity.class);
                    }*/
                } else if (msgDataVo.getType() == SocketMsgTypeEnum.SERVER_NOT_ALLOW.getType()){
                    log.error("已有客户端跟服务端建立连接， 暂时不被允许");
                    //结束、释放资源
                    break;
                } else {
                    //其它消息类型不处理
                }
            }
        } catch (IOException e) {
            log.error("客户端接收消息发生异常");
            e.printStackTrace();
        } finally {
            this.isStop = true;
            log.info("客户端旧接收线程已摧毁");
            StreamUtil.closeInputStream(dataInputStream);
            StreamUtil.closeInputStream(inputStream);
            SocketUtil.closeSocket(socket);
            /*if (socket.isClosed()) {
                System.out.println("socket.isClosed");
            }*/
        }

    }

    public boolean getStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }
}
