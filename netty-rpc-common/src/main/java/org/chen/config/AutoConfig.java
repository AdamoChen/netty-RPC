package org.chen.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * ccg 通过spring.factories文件指定RPC组件所在的包扫描，从而不需要服务中@import指定，
 * 此类可以写独立成一个starter，此处为了项目结构简单并入common中。
 */
@Configuration
@ComponentScan({"org.chen.*"})
public class AutoConfig {

}
