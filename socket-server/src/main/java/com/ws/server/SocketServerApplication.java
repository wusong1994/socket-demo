package com.ws.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * add by ws 2020/02/12
 */
@SpringBootApplication
public class SocketServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SocketServerApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SocketServerApplication.class);
    }
}
