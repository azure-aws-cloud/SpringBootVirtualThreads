package com.example.security.springbootvirtualthreads.controller;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
public class DemoVirtualThreadController {

    private final Executor virtualExecutor;
    public DemoVirtualThreadController(final Executor virtualExecutor) {
        this.virtualExecutor = virtualExecutor;
    }

    @GetMapping("/work")
    public String doWork() {
        System.out.println("Handling request on thread: " + Thread.currentThread());

        // Simulate blocking call (database, API call, file IO)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        return "Hello from Virtual Thread: " + Thread.currentThread().toString();
    }
    @GetMapping("/check")
    public String check() {
        return Thread.currentThread().toString();
    }
    @Async("virtualExecutor")
    @GetMapping("/async")
    public CompletableFuture<String> asyncTask() {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            System.out.println("running on thread: " + Thread.currentThread());
            return "Running on: " + Thread.currentThread();
        }, virtualExecutor);
    }


}

