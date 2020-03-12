package com.ws.client.socketClient;

import com.ws.common.util.SocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;

/**
 * @Desc socket客户端服务
 * @Notice 服务端仅允许一个socket客户端连接,先来后到
 * @Author ws
 * @Time 2020/2/21
 */
@Component
public class ClientSocketService implements InitializingBean {

    private final static Logger log = LoggerFactory.getLogger(ClientSocketService.class);

    private Socket socket;
    private final Object lockObject = new Object(); //锁对象，用于线程通讯，唤醒重试线程

    private final static int THREAD_SLEEP_MILLS = 30000;

    @Value("${qxts.socket.host}")
    private String host;

    @Value("${qxts.socket.port}")
    private int port;

    //30s 间隔多少秒发送一次心跳检测
    @Value("${qxts.socket.heart.interval.time}")
    private int socketHeartIntervalTime;

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
    public void start(){
        Thread socketServiceThread = new Thread(() -> {
            while (true) {
                try {
                    //尝试重新建立连接
                    //socket = SocketUtil.createClientSocket("127.0.0.1", 9999);
                    socket = SocketUtil.createClientSocket(host, port);
                    log.info("客户端 socket 在[{}]连接正常", port);
                    ClientRecvThread recvThread = new ClientRecvThread(socket);
                    new Thread(recvThread).start();
                    ClientHeartBeatThread heartBeatThread = new ClientHeartBeatThread(socket, socketHeartIntervalTime, lockObject);
                    new Thread(heartBeatThread).start();
                    //1、连接成功后阻塞，由心跳检测异常唤醒
                    //方式1
                    synchronized (lockObject) {
                        try {
                            lockObject.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //方式2
                    /*while (!heartBeatThread.isStop()) {
                        //进行空循环, 掉线休眠,防止损耗过大， 随即重连
                        try {
                            Thread.sleep(ClientSocketService.THREAD_SLEEP_MILLS);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }*/
                    //旧的、接收线程、心跳线程摧毁，准备重建连接、接收线程、心跳线程
                } catch (IOException e) {
                    log.error("socket客户端进行连接发生异常");
                    e.printStackTrace();
                    //2、第一次启动时连接异常发生，休眠, 重建连接
                    try {
                        Thread.sleep(ClientSocketService.THREAD_SLEEP_MILLS);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        socketServiceThread.setName("socket client main thread");
        socketServiceThread.start();
    }

    public Socket getSocket() {
        if (socket != null && !socket.isClosed()) {
            return socket;
        }
        return null;
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {
        ClientSocketService clientSocket = new ClientSocketService();
        clientSocket.start();
    }
}
