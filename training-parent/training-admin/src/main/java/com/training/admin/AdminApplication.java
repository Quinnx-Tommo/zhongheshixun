package com.training.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 后台管理启动类（端口 8080）
 */
@SpringBootApplication(scanBasePackages = "com.training")
@MapperScan("com.training.mapper")
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
        System.out.println("===========================================");
        System.out.println("后台管理启动成功！端口: 8080");
        System.out.println("API 地址: http://localhost:8080/admin");
        System.out.println("===========================================");
    }
}
