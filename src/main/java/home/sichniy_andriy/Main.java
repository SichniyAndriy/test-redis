package home.sichniy_andriy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPooled;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static final Faker faker = new Faker();
    private static final int LEN = 100_000;
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Logger LOGGER = LoggerFactory.getLogger("Main");

    public static void main(String[] args) {
        ConnectionPoolConfig config = new ConnectionPoolConfig();
        config.setMaxTotal(80);
        config.setMaxIdle(4);
        config.setMinIdle(4);

        try (JedisPooled jedisPooled = new JedisPooled(config, "localhost", 6379)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 16; ++i) {
                Thread thread = new Thread(() -> {
                    LOGGER.info("{} started work", Thread.currentThread().getName());
                    for (int j = 0; j < LEN; ++j) {
                        jedisPooled.pfadd("emails", faker.internet().emailAddress());
                    }
                    LOGGER.info("{} finished work", Thread.currentThread().getName());
                });
                Future<?> future = executor.submit(thread);
                System.out.println(future + " submitted and ran");
                futures.add(future);
            }
            for (Future<?> future: futures) {
                System.out.println(future);
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            executor.shutdown();
        }
        System.out.println("Main finished");
    }
}