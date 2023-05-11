package com.yilin.yixueblog.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

//注册服务，到注册中心
@EnableDiscoveryClient
@SpringBootApplication
//扫描的路径
@ComponentScan(basePackages = {
        "com.yilin.yixueblog.service",
        "com.yilin.yixueblog.utils",
        "com.yilin.yixueblog.admin"})
@MapperScan("com.yilin.yixueblog.service.mapper")
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class,args);
    }
}
