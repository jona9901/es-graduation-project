package com.griddynamics.indexer;

import com.griddynamics.indexer.services.ProductIndexerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

import static java.util.Arrays.asList;

@SpringBootApplication(scanBasePackages = "com.griddynamics")
public class Application {
    private static final String RECREATE_INDEX_ARG = "recreateIndex";

    @Autowired
    ProductIndexerService productIndexerService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void run(String... strings) {
        List<String> args = asList(strings);
        boolean needRecreateIndex = args.contains(RECREATE_INDEX_ARG);
        if (needRecreateIndex) {
            productIndexerService.recreateIndex();
        }
    }
}
