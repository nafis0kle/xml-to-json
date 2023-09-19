package com.inovus.testtask.xmltojson;

import com.inovus.testtask.xmltojson.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class XmlToJsonApplication {

    private final FileStorageService fileStorageService;

    public static void main(String[] args) {
        SpringApplication.run(XmlToJsonApplication.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return (args) -> {
            fileStorageService.deleteAll();
            fileStorageService.init();
        };
    }

}
