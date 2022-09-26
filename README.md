# websocket_subscribe
[![OSCS Status](https://www.oscs1024.com/platform/badge/aibaixun/service-orchestration-backend.git.svg?size=small)](https://www.murphysec.com/dr/EyDvMc4eSKdruyD1hA)

## 场景
在物联网以及推送领域，用户需要动态的查看一些实时数据，也就是websocket 订阅功能,举例 用户在前端界面中选择需要查看那些实时数据<br/>
当用户订阅了某一id 或者说某一类型后 后续的当实体发生变化后会推送给前端
## 设计方法
subId 单个连接全局唯一订阅id,推送数据后会带该id返回给前端<br/>
主要使用观察者模式 推送数据
## 使用
1. 引入此包结构，应开启http 服务，配置websocket 连接
2. 管理订阅消息，并在适当的地方调用调用消息的 cousmer
3. 订阅管理类需要手动填写
