package org.li.hotpot_reservationsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用程序主类
 * 火锅店预约管理系统后端服务入口
 * 
 * @MapperScan 指定MyBatis Mapper接口的扫描路径
 */
@SpringBootApplication
@MapperScan("org.li.hotpot_reservationsystem.mapper")
public class HotpotReservationsystemApplication {

    /**
     * 应用程序入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(HotpotReservationsystemApplication.class, args);
    }

}
