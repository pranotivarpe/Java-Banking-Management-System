package com.pranotivarpe.bankingsystem.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulingConfig {

    // Spring's auto-configured default TaskScheduler creates non-daemon threads, which blocks the JVM
    // from exiting naturally after the console menu loop finishes — DestroyJavaVM waits for every
    // non-daemon thread, including one that only wakes up once a month for the interest cron. Found via
    // a thread dump (kill -3) showing "scheduling-1" running without the "daemon" tag that HikariCP's
    // and MySQL's own background threads carry.
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("scheduling-");
        scheduler.setDaemon(true);
        scheduler.initialize();
        return scheduler;
    }
}
