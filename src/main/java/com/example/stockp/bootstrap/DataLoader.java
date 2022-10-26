package com.example.stockp.bootstrap;


import com.example.stockp.entity.Stock;
import com.example.stockp.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

@Configuration
//@Profile("!prod")
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final Environment environment;


    private final StockRepository repository;

    private final List<Stock> list = List.of(
            new Stock("Stock1", 1L)
            , new Stock("Stock2", 2L)
            , new Stock("Stock3", 3L)
            , new Stock("Stock4", 4L)
    );

    public DataLoader(Environment environment, StockRepository stockRepository) {
        this.environment = environment;
        this.repository = stockRepository;
    }

    @Override
    public void run(String... args) {
        log.info("#data is loading.....");
        loadData();
        log.info("#Currently active profile - " + Arrays.toString(environment.getActiveProfiles()));

    }

    public void loadData() {
        repository.saveAll(list);
    }
}
