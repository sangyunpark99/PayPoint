package org.sangyunpark.pointpay.config.worker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class EventWorkerExecutorConfig {

    @Bean(name = "eventWorkerExecutor")
    public Executor eventWorkerExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(1);
        exec.setMaxPoolSize(1);
        exec.setThreadNamePrefix("event-worker-");
        exec.initialize();
        return exec;
    }
}
