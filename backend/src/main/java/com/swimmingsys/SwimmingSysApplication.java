package com.swimmingsys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 游泳馆会员管理系统主启动类
 */
@SpringBootApplication
public class SwimmingSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwimmingSysApplication.class, args);
        System.out.println("游泳馆会员管理系统启动成功!");
    }

}
