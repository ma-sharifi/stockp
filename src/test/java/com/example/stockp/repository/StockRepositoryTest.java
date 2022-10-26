package com.example.stockp.repository;

import com.example.stockp.StockpApplication;
import com.example.stockp.entity.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mahdi Sharifi
 */

@SpringBootTest(classes = StockpApplication.class)
class StockRepositoryTest {

    @Autowired
    private StockRepository repository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void shouldSaveAndLoadAndDelete() {
        //The first saves a newly created Stock in the database
        Stock stockExpected= transactionTemplate.execute((ts) -> {
            Stock stock = new Stock("stock-"+ ThreadLocalRandom.current().nextInt(1000),1L);
            repository.save(stock);
            return stock;
        });
        //The second transaction loads the Stock and verifies that its fields are properly initialized
        Stock stockJustLoadActual=transactionTemplate.execute((ts) -> {
            Stock stockJustLoad= repository.findById(stockExpected.getId()).get();
            assertEquals(stockExpected.getId(), stockJustLoad.getId());
            assertEquals(stockExpected.getName(), stockJustLoad.getName());
            return stockJustLoad;
        });
        //The third transaction delete entity and verifies it is deleted
        transactionTemplate.execute((ts) -> {
            repository.deleteById(stockJustLoadActual.getId());
            return null;
        });

        //The forth transaction loads the entity and verifies that its fields are properly initialized
        transactionTemplate.execute((ts) -> {
            Optional<Stock> StockOptionalPersisted = repository.findById(stockJustLoadActual.getId());
            assertFalse(StockOptionalPersisted.isPresent());
            return null;
        });
    }

    @Test
    void shouldUpdateSavedStock(){
        //At first transaction saves a newly created Stock in the database
        Stock stockExpected= transactionTemplate.execute((ts) -> {
            Stock stock = new Stock("stock-"+ ThreadLocalRandom.current().nextInt(1000),1L);
            repository.save(stock);
            return stock;
        });
        //The second transaction loads the Stock and verifies that its fields are properly initialized
        Stock stockJustLoadActual=transactionTemplate.execute((ts) -> {
            Stock stockJustLoad= repository.findById(stockExpected.getId()).get();
            assertEquals(stockExpected.getId(), stockJustLoad.getId());
            assertEquals(stockExpected.getName(), stockJustLoad.getName());
            return stockJustLoad;
        });
        stockJustLoadActual.setName("Updated Name-"+ThreadLocalRandom.current().nextInt(1000));
        stockJustLoadActual.setCurrentPrice(100L);

        //The third transaction loads the Stock and verifies that its fields are properly updated
        transactionTemplate.execute((ts) -> {
            Stock updatedStock = repository.save(stockJustLoadActual);
            assertEquals(stockExpected.getId(), updatedStock.getId());
            assertEquals(stockJustLoadActual.getName(), updatedStock.getName());
            assertEquals(100L, updatedStock.getCurrentPrice());
            return null;
        });
    }

    @Test
    void shouldFindAll() {
        int countExpected = transactionTemplate.execute((ts) -> {
            Pageable pageable= PageRequest.of(0,100);
            List<Stock> stockListActual = repository.findAll(pageable).getContent();
            return stockListActual.size();
        });

        //The first saves a newly created Stock in the database
        transactionTemplate.execute((ts) -> {
            Stock stock = new Stock("stock-" + ThreadLocalRandom.current().nextInt(1000), 1L);
            repository.save(stock);
            return null;
        });

        int countActual = transactionTemplate.execute((ts) -> {
            Pageable pageable= PageRequest.of(0,100);
            List<Stock> stockListActual = repository.findAll(pageable).getContent();
            return stockListActual.size();
        });

        assertEquals(countExpected,countActual-1);

    }
}