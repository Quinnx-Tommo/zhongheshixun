package com.training.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 小程序 API 启动类（端口 9899）
 */
@SpringBootApplication(scanBasePackages = "com.training")
@MapperScan("com.training.mapper")
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
        System.out.println("===========================================");
        System.out.println("小程序 API 启动成功！端口: 9899");
        System.out.println("API 地址: http://localhost:9899/api");
        System.out.println("===========================================");
    }
}
