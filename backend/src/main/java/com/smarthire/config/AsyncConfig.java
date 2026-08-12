package com.smarthire.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Enables @Async so processJobAsync runs on a background thread pool. */
@Configuration
@EnableAsync
public class AsyncConfig {
}
