package com.ws.server.socketServer;

import com.ws.common.enums.SocketMsgTypeEnum;
import com.ws.common.exception.SocketSendThreadException;
import com.ws.common.util.SocketUtil;
import com.ws.common.vo.SocketMsgDataVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Desc socket服务端服务
 * @Author ws
 * @Time 2020/2/21
 */
@Component
public class ServerSocketService implements InitializingBean {

    private final static Logger log = LoggerFactory.getLogger(ServerSocketService.class);

    private volatile Socket clientSocket;

    private volatile ServerSendThread sendThread;

    @Value("${qxts.socket.port}")
    private int port;

    @Value("${qxts.socket.read.timeout}")
    private int socketReadTimeout;

    //只能存在一个socket客户端
    private Map<Socket, ServerRecvThread> threadMap = new ConcurrentHashMap<>(1);


    //在该类的依赖注入完毕之后，会自动调用afterPropertiesSet方法,否则外部tomcat部署会无法正常启动socket

    //jar包的启动时直接由项目的主函数开始启动，此时会先初始化IOC容器，然后才会进行内置Servlet环境（一般为Tomcat）的启动。
    //war包通常使用Tomcat进行部署启动，在tomcat启动war应用时，会先进行Servlet环境的初始化，之后才会进行到IOC容器的初始化，也就是说，在servlet初始化过程中是不能使用IOC依赖注入的
    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    /**
     * 启动服务
     */
    public void start() {
        Thread socketServiceThread = new Thread(() -> {
            ServerSocket serverSocket = null;
            try {
                //serverSocket = new ServerSocket(8000);
                serverSocket = new ServerSocket(port);
                log.info("服务端 socket 在[{}]启动正常", port);
                //记录已开启的线程，便于管理
                while (true) {
                    Socket newSocket = serverSocket.accept();
                    //设定输入流读取阻塞超时时间(60秒收不到客户端消息判定断线；与客户端心跳检测结合一起使用的)
                    newSocket.setSoTimeout(socketReadTimeout * 1000);  //注意：读取时间等待超时时间，必须比心跳检测消息发送时间大；否则就不断在中断连接的循环之中
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        //如果已有一个连接上客户端且没有关闭，则丢弃新连进来的
                        OutputStream outputStream = newSocket.getOutputStream();
                        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                        SocketMsgDataVo msgDataVo = new SocketMsgDataVo();
                        //开启这个接收线程，纯属关闭资源用
                        ServerRecvThread otherRecvThread = new ServerRecvThread(newSocket);
                        new Thread(otherRecvThread).start();
                        //发送个无用的消息，告知具体情况
                        msgDataVo.setType(SocketMsgTypeEnum.SERVER_NOT_ALLOW.getType());
                        msgDataVo.setBody("from server: one is connected and other is not allowed at present");
                        SocketUtil.writeMsgData(dataOutputStream, msgDataVo);
                        log.warn("one is connected and new is not allowed at present");
                        //继续监听
                        continue;
                    }
                    //1、关闭已开启的线程
                    this.closeOpenedThreads();
                    //2、重建新的socket服务
                    clientSocket = newSocket;
                    ServerRecvThread newRecvThread = new ServerRecvThread(clientSocket);
                    threadMap.put(clientSocket, newRecvThread);
                    new Thread(newRecvThread).start();
                    ServerSendThread newServerSendThread = new ServerSendThread(clientSocket);
                    sendThread = newServerSendThread;
                    new Thread(newServerSendThread).start();
                }
            } catch (IOException e) {
                log.error("socket服务端发生异常");
                e.printStackTrace();
                //释放资源
                //关闭已开启的线程
                this.closeOpenedThreads();
                if (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        socketServiceThread.setName("socket server main thread");
        socketServiceThread.start();
    }

    /**
     *关闭已开启的线程
     */
    private void closeOpenedThreads() {
        if (clientSocket != null) {
            log.info("删除旧的无效连接及其接收、发送线程");
            ServerRecvThread oldRecvThread = threadMap.remove(clientSocket);
            oldRecvThread.setStop(true);
            sendThread.setStop(true);
            SocketMsgDataVo msgDataVo = new SocketMsgDataVo();
            //发送个无用的消息，唤醒线程（可能处于阻塞），以便结束旧的发送线程
            msgDataVo.setType(SocketMsgTypeEnum.HEART_BEAT.getType());
            msgDataVo.setBody("from server: null message");
            sendThread.addMsgToQueue(msgDataVo);
            log.info("旧的无效连接及其接收、发送线程已回收");
        }
    }

    /**
     * 测试
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ServerSocketService socketService = new ServerSocketService();
        socketService.start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            int count = 1;
            while(true) {
                if (socketService.getClientSocket() != null) {
                    SocketMsgDataVo msgDataVo = new SocketMsgDataVo();
                    msgDataVo.setType(SocketMsgTypeEnum.HEART_BEAT.getType());
                    msgDataVo.setBody("from server: " + count++);
                    try {
                        socketService.addMsgToQueue(msgDataVo);
                    } catch (SocketSendThreadException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public Socket getClientSocket() {
        if (clientSocket != null && !clientSocket.isClosed()) {
            return clientSocket;
        }
        return null;
    }

    /**
     * 发送消息
     * @param msgDataVo
     */
    public boolean addMsgToQueue(SocketMsgDataVo msgDataVo) throws SocketSendThreadException {
        if (sendThread != null) {
            return sendThread.addMsgToQueue(msgDataVo);
        } else {
            throw new SocketSendThreadException("连接不上接收端，无法进行数据推送");
        }
    }

}
