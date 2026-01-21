package com.notifyhub.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotifyhubWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifyhubWorkerApplication.class, args);
    }

}
