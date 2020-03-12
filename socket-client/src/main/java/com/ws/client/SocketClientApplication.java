package com.ws.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * add by ws 2020/02/12
 */
@SpringBootApplication
public class SocketClientApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SocketClientApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SocketClientApplication.class);
    }
}
