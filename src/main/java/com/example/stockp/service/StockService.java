package com.example.stockp.service;

import com.example.stockp.exception.StockNotFoundException;
import com.example.stockp.service.dto.StockDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing Stock
 */
public interface StockService {

    StockDto save(StockDto stockDto);

    StockDto update(StockDto stockDto);

    StockDto partialUpdate(StockDto stockDto);

    Page<StockDto> findAll(Pageable pageable);

    StockDto findOne(Long id) throws StockNotFoundException;

    void delete(Long id);
}
