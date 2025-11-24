package com.hellcat.movie_app;

import org.springframework.boot.SpringApplication;

public class TestHuyetaApplication {

    public static void main(String[] args) {
        SpringApplication.from(MovieAppApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
