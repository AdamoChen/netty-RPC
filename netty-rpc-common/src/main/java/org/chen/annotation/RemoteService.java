package org.chen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chenchonggui
 * @version 1.0
 * @date_time 2020/12/2 11:33
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
// todo 是否需要实例化
public @interface RemoteService {

    /**
     * 服务名
     * @return
     */
    String value();

}
