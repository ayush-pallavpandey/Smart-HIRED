package com.smarthire;

import com.smarthire.config.MlProperties;
import com.smarthire.config.UploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({MlProperties.class, UploadProperties.class})
public class SmartHireApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartHireApplication.class, args);
    }
}
