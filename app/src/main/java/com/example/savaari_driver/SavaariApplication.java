package com.example.savaari_driver;

import android.app.Application;
import com.example.savaari_driver.services.location.LocationUpdateUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavaariApplication extends Application
{
    // Main Attributes
    public ExecutorService executorService;
    public com.example.savaari_driver.Repository repository;

    // Constructor: initializes thread pool to run in repository
    public SavaariApplication()
    {
        executorService = Executors.newFixedThreadPool(4);
        repository = new com.example.savaari_driver.Repository(executorService);
        LocationUpdateUtil.setRepository(repository);
    }

    // Getters
    public com.example.savaari_driver.Repository getRepository() { return repository; }
}
