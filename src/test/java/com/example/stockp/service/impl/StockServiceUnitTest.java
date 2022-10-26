package com.example.stockp.service.impl;

import antlr.Utils;
import com.example.stockp.entity.Stock;
import com.example.stockp.exception.BadRequestException;
import com.example.stockp.exception.StockNotFoundException;
import com.example.stockp.repository.StockRepository;
import com.example.stockp.service.StockService;
import com.example.stockp.service.dto.StockDto;
import com.example.stockp.service.mapper.StockMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Mahdi Sharifi
 */

@ExtendWith(MockitoExtension.class)
class StockServiceUnitTest {

    StockService service;

    @Mock
    StockRepository repository;

    @Captor
    ArgumentCaptor<Stock> captor;//You should call it during the verification phase of the test.

    @Spy
    StockMapper mapper = Mappers.getMapper(StockMapper.class);


    @BeforeEach
    void initializeService() {
        service = new StockServiceImpl(repository, mapper);
    }


    @Test
    void shouldDeleteAStock_whenDeleteIsCall() {
        Stock stockEntityExpected = new Stock("Stock1",1L);
        StockDto stockDtoExpected = mapper.toDto(stockEntityExpected);

        // Arrange stub save method. It must return given entity.
        when(repository.save(ArgumentMatchers.any(Stock.class))).thenReturn(stockEntityExpected);
        // Act
        StockDto stockDtoActual = service.save(stockDtoExpected);

        // Assert
        assertEquals(stockDtoActual.getId(), stockEntityExpected.getId());
        assertEquals(stockDtoActual.getName(), stockEntityExpected.getName());

        // Act
        service.delete(10000L);

        StockNotFoundException thrown = Assertions.assertThrows(StockNotFoundException.class, () -> {
            service.findOne(10000L);
        });
        assertTrue(thrown.getMessage().contains("Could not find the stock"));
    }

    @Test
    void shouldSaveStockDto_whenSaveIsCall() {
        Stock stockEntityExpected = new Stock("Stock1",1L);
        StockDto stockDtoExpected = mapper.toDto(stockEntityExpected);

        // Arrange stub save method. It must return given entity.
        when(repository.save(ArgumentMatchers.any(Stock.class))).thenReturn(stockEntityExpected);

        // Act
        StockDto stockDtoActual = service.save(stockDtoExpected);

        //to verify if the save() method of the mocked repository has been called.
        verify(repository, times(1)).save(ArgumentMatchers.any(Stock.class));

        // Assert
        assertEquals(stockDtoActual.getId(), stockEntityExpected.getId());
        assertEquals(stockDtoActual.getName(), stockEntityExpected.getName());
        assertEquals(stockDtoActual.getCurrentPrice(), stockEntityExpected.getCurrentPrice());
    }

    @Test
    void shouldUpdateCurrentStockDto_whenUpdateIsCall() {
        Stock stockEntityExpected = new Stock("Stock1",1L);
        stockEntityExpected.setId(1L);
        StockDto stockDtoExpected = mapper.toDto(stockEntityExpected);

        // Arrange stub save method. It must return given entity.
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(ArgumentMatchers.any(Stock.class))).thenReturn(stockEntityExpected);

        // Act
        StockDto stockDtoActual = service.update(stockDtoExpected);

        //to verify if the save() method of the mocked repository has been called.
        verify(repository, times(1)).save(ArgumentMatchers.any(Stock.class));

        // Assert
        assertEquals(stockDtoActual.getId(), stockEntityExpected.getId());
        assertEquals(stockDtoActual.getName(), stockEntityExpected.getName());
        assertEquals(stockDtoActual.getCurrentPrice(), stockEntityExpected.getCurrentPrice());
    }


    @Test
    void shouldFindAStockDto_whenFindOneIsCall() {
        Stock stockEntityExpected = new Stock("Stock1",1L);
        stockEntityExpected.setId(100L);
        StockDto stockDtoExpected = mapper.toDto(stockEntityExpected);

        // Arrange stub save method. It must return given entity.
        when(repository.findById(100L)).thenReturn(Optional.of(stockEntityExpected));

        // Act
        StockDto stockDtoActual = service.findOne(100L);

        //to verify if the save() method of the mocked repository has been called.
        verify(repository, times(1)).findById(100L);

        // Assert
        assertEquals(100L, stockDtoActual.getId());
        assertEquals(stockDtoExpected.getName(), stockDtoActual.getName());
        assertEquals(stockDtoExpected.getCurrentPrice(), stockDtoActual.getCurrentPrice());
    }

    @Test
    void shouldFindAllStockDto_whenFindAllIsCall() {
        Stock stockEntityExpected1 = new Stock("Stock11",11L);
        Stock stockEntityExpected2 = new Stock("Stock22",22L);
        Stock stockEntityExpected3 = new Stock("Stock33",33L);
        stockEntityExpected1.setId(100L);
        stockEntityExpected2.setId(101L);
        stockEntityExpected3.setId(102L);
        StockDto stockDtoExpected1 = mapper.toDto(stockEntityExpected1);
        StockDto stockDtoExpected2 = mapper.toDto(stockEntityExpected2);
        StockDto stockDtoExpected3 = mapper.toDto(stockEntityExpected3);
        List<StockDto> stockDtoListExpected=List.of(stockDtoExpected1,stockDtoExpected2,stockDtoExpected3);
        List<Stock> stockListExpected=List.of(stockEntityExpected1,stockEntityExpected2,stockEntityExpected3);
        // Arrange stub save method. It must return given entity.
        Pageable pageable= PageRequest.of(0,10);
        Page<Stock> postPage = new PageImpl<>(stockListExpected);
        when(repository.findAll(pageable)).thenReturn(postPage);

        // Act
        List<StockDto> stockDtoListActual = service.findAll(pageable).getContent();

        //to verify if the save() method of the mocked repository has been called.
        verify(repository, times(1)).findAll(pageable) ;

        // Assert
        assertThat(stockDtoListActual).hasSameElementsAs(stockDtoListExpected);
    }

    @Test
    void shouldReturnStockNotFoundException_whenGetIsCalled() {
        StockNotFoundException thrown = Assertions.assertThrows(StockNotFoundException.class, () -> {
            service.findOne(Long.MAX_VALUE);
        });
        assertTrue(thrown.getMessage().contains("Could not find the stock"));
    }
    @Test
    void shouldThrowBadRequestException_whenIdOfUpdateIsNoExistAndUpdateIsCalled() {
        Stock stockEntityExpected = new Stock("Stock1",1L);
        StockDto stockDtoExpected = mapper.toDto(stockEntityExpected);
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            StockDto stockDtoActual = service.update(stockDtoExpected);
        });
        assertTrue(thrown.getMessage().contains("Dto must have an id"));
    }

    @Test
    void shouldPartialUpdateCurrentStockDto_whenPartialUpdateIsCall() {
        Stock stockEntityExpected = new Stock("Stock1",1L);
        stockEntityExpected.setId(1L);
        StockDto stockDtoExpected = mapper.toDto(stockEntityExpected);

        // Arrange stub save method. It must return given entity.
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(stockEntityExpected));
        when(repository.save(ArgumentMatchers.any(Stock.class))).thenReturn(stockEntityExpected);

        // Act
        StockDto stockDtoActual = service.partialUpdate(stockDtoExpected);

        //to verify if the save() method of the mocked repository has been called.
        verify(repository, times(1)).save(ArgumentMatchers.any(Stock.class));

        // Assert
        assertEquals(stockDtoActual.getId(), stockEntityExpected.getId());
        assertEquals(stockDtoActual.getName(), stockEntityExpected.getName());
        assertEquals(stockDtoActual.getCurrentPrice(), stockEntityExpected.getCurrentPrice());
    }
}