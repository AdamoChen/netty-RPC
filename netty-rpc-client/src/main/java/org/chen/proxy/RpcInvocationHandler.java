package org.chen.proxy;

import com.adamo.service.dto.Request;
import com.adamo.service.dto.Response;
import com.alibaba.fastjson.JSONObject;
import org.chen.annotation.RemoteService;
import org.chen.constant.StatusCodeEnum;
import org.chen.netty.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * @author chenchonggui
 * @version 1.0
 * @date_time 2020/12/8 20:20
 */
@Component
public class RpcInvocationHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(RpcInvocationHandler.class);

    @Autowired
    RpcClient rpcClient;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Request request = new Request();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassFullName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setArgsType(method.getParameterTypes());
        request.setArgs(args);

        RemoteService remoteService = method.getDeclaringClass().getDeclaredAnnotation(RemoteService.class);
        Response response = rpcClient.send(request, remoteService.value());
        if(StatusCodeEnum.SUCCESS.code == response.getCode()){
            // ccg 这里返回响应结果的响应需要注意
            Class<?> returnType = method.getReturnType();
            if(returnType.isPrimitive() || String.class.isAssignableFrom(returnType)){
                return response.getData();
            }else if(Collection.class.isAssignableFrom(returnType)){
                return JSONObject.parseArray(response.getData().toString(), Object.class);
            }else if(Map.class.isAssignableFrom(returnType)){
                return JSONObject.parseObject(response.getData().toString(), Map.class);
            }else{
                return JSONObject.parseObject(response.getData().toString(), returnType);
            }
        }else{
            log.error("远程调用响应异常：{}", response.getErrorMsg());
            throw new RuntimeException("远程调用响应异常" + response.getErrorMsg());
        }
    }
}