# Spring boot application enabled using Virtual Threads
## Blocking I/O calls like database, http client calls to remote rest services, file i/o, in REST APIs should use Virtual Threads
## Refer most important code snippet below
```
    @Async("virtualExecutor") <---------- This is the virtual thread bean reference (Executor that will execute using virtual threads)
    @GetMapping("/async")
    public CompletableFuture<String> asyncTask() {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            System.out.println("running on thread: " + Thread.currentThread());
            return "Running on: " + Thread.currentThread();
        }, virtualExecutor);
    }

@Configuration
@EnableAsync
public class VirtualThreadConfig {

    /**
     * Executor backed by virtual threads.
     * Spring will use this executor for any method annotated with @Async("virtualExecutor").
     */
    @Bean(name = "virtualExecutor") <----- This bean has to be referened in the REST API (/async)
    public Executor executor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}

| Use Case                               | Recommended? | Notes                                |
| -------------------------------------- | ------------ | ------------------------------------ |
| Blocking JDBC                          | ✅ Yes        | Perfect match                        |
| REST API calls (synchronous)           | ✅ Yes        | Helps concurrency                    |
| File I/O                               | ✅ Yes        | Uses OS-level blocking               |
| Messaging frameworks (Kafka)           | ⚠ Depends    | Works, but rarely needed             |
| CPU-heavy tasks (image processing, ML) | ❌ No         | Use platform threads or thread pools |

❗ Important Rule:

Virtual threads do not fix CPU bottlenecks — they fix thread contention caused by blocking.


```
