package com.example.security.springbootvirtualthreads.controller;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
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
        } catch (InterruptedException ignored) {}

        return "Hello from Virtual Thread: " + Thread.currentThread().toString();
    }
    @GetMapping("/check")
    public String check() {
        return Thread.currentThread().toString();
    }
    @Async("virtualExecutor")
    @GetMapping("/async")
    public CompletableFuture<String> asyncTask() { // <----------This entire method runs in a virtual thread !!!
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
                        "\nThread: " + Thread.currentThread(); // <--- This is virtual thread like VirtualThread[#74]/runnable@ForkJoinPool-1-worker-8
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, virtualExecutor);
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