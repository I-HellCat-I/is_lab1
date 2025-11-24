package com.hellcat.movie_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
public class MovieAppApplication { // Наследуемся

    public static void main(String[] args) {
        SpringApplication.run(MovieAppApplication.class, args);
    }

    // Этот метод нужен для запуска во внешнем контейнере Tomcat
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MovieAppApplication.class);
    }
}