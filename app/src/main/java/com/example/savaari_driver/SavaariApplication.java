package com.example.savaari_driver;

import android.app.Application;
import com.example.savaari_driver.services.location.LocationUpdateUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class SavaariApplication extends Application
{
    // Main Attributes
    public ExecutorService executorService;
    public ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    public com.example.savaari_driver.Repository repository;
    private final int numThreads = 4;


    // Constructor: initializes thread pool to run in repository
    public SavaariApplication()
    {
        executorService = Executors.newFixedThreadPool(numThreads);
        repository = new com.example.savaari_driver.Repository(executorService);
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        LocationUpdateUtil.setRepository(repository);
    }

    // Getters
    public com.example.savaari_driver.Repository getRepository() { return repository; }
}
