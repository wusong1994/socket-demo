package com.ws.common.util;

import com.ws.common.vo.SocketMsgDataVo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketUtil {

    private static final int  BLANK_SPACE_COUNT = 5;

    public static Socket createClientSocket(String host, int port) throws IOException {
        Socket socket = new Socket(host,port);
        return socket;
    }

    public static void closeSocket(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeMsgData(DataOutputStream dataOutputStream, SocketMsgDataVo msgDataVo) throws IOException {
        byte[] data = msgDataVo.getBody().getBytes();
        int len = data.length + SocketUtil.BLANK_SPACE_COUNT;
        dataOutputStream.writeByte(msgDataVo.getType());
        dataOutputStream.writeInt(len);
        dataOutputStream.write(data);
        dataOutputStream.flush();
    }

    public static SocketMsgDataVo readMsgData(DataInputStream dataInputStream) throws IOException {
        byte type = dataInputStream.readByte();
        int len = dataInputStream.readInt();
        byte[] data = new byte[len - SocketUtil.BLANK_SPACE_COUNT];
        dataInputStream.readFully(data);
        String str = new String(data);
        System.out.println("获取的数据类型为：" + type);
        System.out.println("获取的数据长度为：" + len);
        System.out.println("获取的数据内容为：" + str);
        SocketMsgDataVo msgDataVo = new SocketMsgDataVo();
        msgDataVo.setType(type);
        msgDataVo.setBody(str);
        return msgDataVo;
    }
}
