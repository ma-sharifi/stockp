package com.example.stockp.service.mapper;

import com.example.stockp.entity.Stock;
import com.example.stockp.service.dto.StockDto;
import org.mapstruct.*;

/**
 * Mapper for the entity  Stock and its Dto StockDto.
 */
@Mapper(componentModel = "spring")
public interface StockMapper extends EntityMapper<StockDto, Stock> {}
