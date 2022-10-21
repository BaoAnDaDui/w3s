# websocket_subscribe
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



## 实现方案 （待补充）


## 使用 （待完善）

