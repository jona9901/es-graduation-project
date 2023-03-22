package com.griddynamics.indexer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import java.util.List;

import static java.util.Arrays.asList;

public class Application implements CommandLineRunner {
    private static final String RECREATE_INDEX_ARG = "recreateIndex";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... strings) {
        List<String> args = asList(strings);
        boolean needRecreateIndex = args.contains(RECREATE_INDEX_ARG);
        if (needRecreateIndex) {
            //typeaheadService.recreateIndex();
        }
    }
}
