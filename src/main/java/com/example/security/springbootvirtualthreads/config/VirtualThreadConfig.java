package com.example.security.springbootvirtualthreads.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import java.util.concurrent.*;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class VirtualThreadConfig {

    /**
     * Executor backed by virtual threads.
     * Spring will use this executor for any method annotated with @Async("virtualExecutor").
     */
    @Bean(name = "virtualExecutor")
    public Executor executor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}





