package com.example.security.springbootvirtualthreads.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        } catch (InterruptedException ignored) {
        }

        return "Hello from Platform Thread: " + Thread.currentThread().toString();
    }

    @GetMapping("/check")
    public String check() {
        return Thread.currentThread().toString();
    }

    @GetMapping("/async")
    @Async("virtualExecutor")
    @CircuitBreaker(name = "externalApiCB", fallbackMethod = "fallback")
    /*
        Running this code will create 2 virtual threads (not required)
     */
    public CompletableFuture<String> asyncTask(@RequestParam(defaultValue = "false") boolean fail) {
        if (fail) {
            throw new RuntimeException("Forced failure for testing");
        }

        System.out.println("received async request " + Thread.currentThread());//<------First Virtual thread 1  like VirtualThread[#78]/runnable@ForkJoinPool-1-worker-8
        HttpClient httpClient = HttpClient.newHttpClient();

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> response = httpClient.send(
                        HttpRequest.newBuilder(URI.create("https://httpbin.org/delay/3"))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );
                return "Response: " + response.body() +
                        "\nThread: " + Thread.currentThread(); // <--- This is second virtual thread like VirtualThread[#74]/runnable@ForkJoinPool-1-worker-8
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, virtualExecutor);
    }


    @Async("virtualExecutor")
    @GetMapping("/async1")
    public CompletableFuture<String> externalPLMCloudAPI() {
        HttpClient httpClient = HttpClient.newHttpClient();
        System.out.println("received async request " + Thread.currentThread());
        try {
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder(URI.create("https://httpbin.org/delay/3"))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            return CompletableFuture.completedFuture(
                    "Response: " + response.body() +
                            "\nThread: " + Thread.currentThread());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }






    /**
     * Fallback method for circuit breaker
     * Must match return type and accept Throwable as param
     */
    private CompletableFuture<String> fallback(Throwable ex) {
        return CompletableFuture.completedFuture(
                "Fallback response — external service unavailable.\n" +
                        "Cause: " + ex.getMessage()
        );
    }
}




/*
     HttpClient httpClient = HttpClient.newHttpClient();

        return httpClient.sendAsync(
                        HttpRequest.newBuilder(URI.create("https://httpbin.org/delay/3")).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                )
                .thenApply(HttpResponse::body)
                .thenApply(body -> "Response: " + body);
 */