#### netty-RPC
git分支
    base_rpc 基本的rpc可本机直接运行的实现，不依赖注册中心。
    main 完整的版本，依赖nacos注册中心

基于netty的RPC简单实现

涉及的主要技术点：  
-- 基于netty的网络编程  
-- 针对接口的代理  
-- 请求与响应参数的序列化反序列化  
-- 服务注册与发现   
-- 心跳检测与连接维护  

组件关系：  
-- 服务端、客户端关系为多对多，即调用方存在调用多个服务提供方，服务提供方会被多个调用方使用，且同一服务还存在多个节点。

想法
基于springboot封闭成starter、普通spring也能使用

    
