# websocket_subscribe
[![OSCS Status](https://www.oscs1024.com/platform/badge/aibaixun/service-orchestration-backend.git.svg?size=small)](https://www.murphysec.com/dr/EyDvMc4eSKdruyD1hA)

## 场景
用户使用webSockets实时感知数据变更时，用户可以订阅多组或者多种类数据，并不会在新开webSocket 连接

- 比如用户在大屏中订阅 

## 设计方法
subId 单个连接全局唯一订阅id,推送数据后会带该id返回给前端<br/>
主要使用观察者模式 推送数据

## 使用
1. 引入此包结构，应开启http 服务，配置websocket 连接
2. 管理订阅消息，并在适当的地方调用调用消息的 consumer
3. 订阅管理类需要手动填写
