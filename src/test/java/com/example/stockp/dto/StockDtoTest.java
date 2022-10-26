package com.example.stockp.dto;

import com.example.stockp.service.dto.StockDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class StockDtoTest {

    @Test
    void dtoEqualsVerifier()  {
        StockDto stockDto1 = StockDto.builder().name("stock1").id(1L).build();
        StockDto stockDto2 = StockDto.builder().name("stock1").id(1L).build();
        stockDto2.setId(stockDto1.getId());
        assertThat(stockDto1).isEqualTo(stockDto2);
        stockDto2.setId(2L);
        assertThat(stockDto1).isNotEqualTo(stockDto2);
        stockDto1.setId(null);
        assertThat(stockDto1).isNotEqualTo(stockDto2);
    }
}
