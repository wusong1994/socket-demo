# Demo说明
springBoot下socket一对一建立长连接通讯，包括客户端心跳检测、掉线重连、收发信息等

# 代码结构说明

## socket-client
客户端

## socket-server
服务端
由于可能向一个客户端socket进行并发发送消息，所以采用了一个发送线程，从消息队列中不断取消息发送

## socket-common
公共模块


# 项目技术架构
基于springBoot框架为主进行开发。

# 打包部署
## 测试环境
例如： mvn clean package  -pl socket-client -am -Dmaven.test.skip=true
## 生产环境
例如： mvn clean package -Pprod  -pl socket-client -am -Dmaven.test.skip=true


# 项目配置说明

## 运行环境
- **jdk**
 ```1.8以上```
 
- **其他**
  
 ## 配置文件部分参数属性说明
- **qxts.socket.host和qxts.socket.port**
  ```实时推送接口数据中消息通讯使用到的socket配置```
