package com.example.stockp.service.impl;


import com.example.stockp.entity.Stock;
import com.example.stockp.exception.BadRequestException;
import com.example.stockp.exception.StockNotFoundException;
import com.example.stockp.repository.StockRepository;
import com.example.stockp.service.StockService;
import com.example.stockp.service.dto.StockDto;
import com.example.stockp.service.mapper.StockMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * Service Implementation for managing Stock
 */
@Service
@Transactional
public class StockServiceImpl implements StockService {

    private final Logger log = LoggerFactory.getLogger(StockServiceImpl.class);

    private final StockRepository stockRepository;

    private final StockMapper stockMapper;

    public StockServiceImpl(StockRepository stockRepository, StockMapper stockMapper) {
        this.stockRepository = stockRepository;
        this.stockMapper = stockMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockDto> findAll(Pageable pageable) {
        log.debug("#Request to get all Stocks");
        return stockRepository.findAll(pageable).map(stockMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public StockDto findOne(Long id) throws StockNotFoundException {
        log.debug("#Request to get Stock : {}", id);
        Optional<Stock> stockOptional= stockRepository.findById(id);
        return  stockOptional.map(stockMapper::toDto).orElseThrow(()->new StockNotFoundException(id+ ""));
    }

    @Override
    public StockDto save(StockDto stockDto) {
        log.debug("#Request to save Stock : {}", stockDto);
        if (stockDto.getId() != null) {
            throw new BadRequestException("ID exist. A new stock cannot already have an ID! id is: " + stockDto.getId());
        }
        Stock stock = stockMapper.toEntity(stockDto);
        stock.setLastUpdate(new Date());
        stock = stockRepository.save(stock);
        return stockMapper.toDto(stock);
    }

    @Override
    public StockDto update(StockDto stockDto) {
        log.debug("#Request to save Stock : {}", stockDto);
        if (stockDto.getId() == null) {
            throw new BadRequestException("#Invalid id! id is null! Dto must have an id.");
        }
        if (!stockRepository.existsById(stockDto.getId())) {
            throw new BadRequestException("Entity not found! There no entity with this id for update. actual dto.id is: " + stockDto.getId());
        }
        Stock stock = stockMapper.toEntity(stockDto);
        stock.setLastUpdate(new Date()); // I took for every update it needs to be updated.
        stock = stockRepository.save(stock);
        return stockMapper.toDto(stock);
    }

    @Override
    public StockDto partialUpdate(StockDto stockDto) {
        log.debug("#Request to partially update Stock : {}", stockDto);

        if (stockDto.getId() == null) {
            throw new BadRequestException("id is null! Invalid id! Dto must have an id.");
        }
        if (!stockRepository.existsById(stockDto.getId())) {
            throw new BadRequestException("Entity not found! There no entity with this id for partial update. actual dto.id is: " + stockDto.getId());
        }
        Optional<Stock> stock = stockRepository.findById(stockDto.getId());
        Optional<Stock> stockAfterMergingWithInputDtoOptional = stock.map(existingStock -> {
            stockMapper.partialUpdate(existingStock, stockDto);
            return existingStock;
        });
        Optional<Stock> stockOptional= stockAfterMergingWithInputDtoOptional.map(stockRepository::save);
        return stockOptional.map(stockMapper::toDto).orElseThrow(()->new RuntimeException("Something unhandled occurred in partial update! id is: "+stockDto.getId()));
    }



    @Override
    public void delete(Long id) {
        log.debug("#Request to delete Stock : {}", id);
        stockRepository.deleteById(id);
    }
}
