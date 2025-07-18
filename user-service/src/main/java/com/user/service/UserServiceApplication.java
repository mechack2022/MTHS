package com.user.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        System.out.println("We are here");
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("We got to the last");
    }

}
