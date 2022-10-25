# w3s websocket_subscribe/wss subscribe
[![OSCS Status](https://www.oscs1024.com/platform/badge/aibaixun/service-orchestration-backend.git.svg?size=small)](https://www.murphysec.com/dr/EyDvMc4eSKdruyD1hA)

## 场景
用户使用webSockets实时感知数据变更时，用户可以订阅多组或者多种类数据，并不会在新开webSocket 连接

- 比如用户在大屏中订阅 三个设备数据 用户可以下拉选择增加某一个设备数据
- 用户使用一个webSocket 可以 动态选择订单还是商品的实时变更数据

## 功能
- 用户连接websocket 并发送订阅数据
- 维护websocket心跳
- wss 接收到用户订阅之后使用订阅ID 维护订阅列表，当数据变更时候 触发订阅数据更新通知
- 数据变更 一般是消息队列或者其他地方 直接调用预设的接口
- 提供用户授权接口与连接限制功能



## 实现方案 
1. 前端建立websocket 并订阅想要监听的数据 ，订阅消息模版是
```
 int subId; // subId  全局唯一

 String entityId; // 订阅实体数据变化 的id
 
 String entityType; // entity type 

 boolean unSub;  // 是否取消订阅
```

2. 后端收到订阅数据后 将订阅信息存储起来

3. 当某个实体数据变化 比如接收到消息队列数据 使用 LocalSubscriptionManager类的如下方法通知
```
<T> void onSubscriptionUpdate(String entityId, SubscriptionDataUpdate update, ServiceCallback<T> callback);
```

## 使用 （待完善）

1. 引入包
    ```
   <dependency>
            <groupId>com.github.baoan.w3s</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version></version> // 暂时没有传递到仓库 需要本地编译
   </dependency>
    ```
2. 更改配置文件
```
w3s:
  opened: true
  wsUrlPrefix: ws
  sendTimeOut: 1000
  maxSubOfSession: 19
  pingTimeout: 1000
  numberOfPingAttempts: 10
```
3. 实现授权接口WebSocketAuthService  如果默认不实现则没有鉴权 也不会做连接数限制
4. 启动程序