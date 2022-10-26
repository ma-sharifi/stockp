package com.example.stockp.controller;


import com.example.stockp.EndToEndTest;
import com.example.stockp.entity.Stock;
import com.example.stockp.repository.StockRepository;
import com.example.stockp.service.dto.ResponseDto;
import com.example.stockp.service.dto.StockDto;
import com.example.stockp.service.mapper.StockMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;

import static com.example.stockp.util.ConvertorUtil.toResponseDto;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Mahdi Sharifi
 */
@EndToEndTest
@DisplayName("Stock controller End to End test")
class StockControllerE2ETest {


    private static final String NAME = "Stock#1";

    private static final Long CURRENT_PRICE = 1L;

    private static final String ENTITY_API_URL = "/api/stocks";

    @Autowired
    private StockMapper stockMapper;

    private String uri;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Create an entity for this test.
     */
    public Stock createEntity() {
        Stock stock = new Stock()
                .name(NAME)
                .currentPrice(CURRENT_PRICE);
        return stock;
    }

    @PostConstruct
    public void init() {
        uri = "http://localhost:" + port;
    }

    @Test
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
    }


    @Test
    void shouldCreateStockThenReturnStockDto_whenCreateIsCalled_thenGetIsCalled() {
        // Create entity
        Stock stock = createEntity();
        StockDto dto = stockMapper.toDto(stock);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<StockDto> entity = new HttpEntity<>(dto, headers);
        ResponseEntity<ResponseDto> responseEntityCreate = this.restTemplate.exchange(uri + ENTITY_API_URL, HttpMethod.POST, entity, ResponseDto.class);

        //Test Created successfully
        String urlCreatedObject = responseEntityCreate.getHeaders().get("Location").get(0);//get the location of saved stock-> /v1/stocks/6
        assertEquals(HttpStatus.CREATED, responseEntityCreate.getStatusCode());

        // Get saved entity
        HttpEntity<String> entityGet = new HttpEntity<>(headers);
        ResponseEntity<StockDto> responseEntity = this.restTemplate.exchange(urlCreatedObject, HttpMethod.GET, entityGet, StockDto.class);

        //Test Fetch saved entity successfully
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void shouldReturnNoContent_whenCreateIsCalled_thenDeleteIsCalled() {
        // Create entity
        Stock stock = createEntity();
        stock.setName("Dummy data for test delete");
        StockDto dto = stockMapper.toDto(stock);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<StockDto> entity = new HttpEntity<>(dto, headers);
        ResponseEntity<ResponseDto> responseEntityCreate = this.restTemplate.exchange(uri + ENTITY_API_URL, HttpMethod.POST, entity, ResponseDto.class);

        //Test Created successfully
        String urlCreatedObject = responseEntityCreate.getHeaders().get("Location").get(0);//get the location of saved stock-> /v1/stocks/6
        assertEquals(HttpStatus.CREATED, responseEntityCreate.getStatusCode());

        // Get saved Stock
        HttpEntity<String> entityGet = new HttpEntity<>(headers);
        ResponseEntity<StockDto> responseEntity = this.restTemplate.exchange(urlCreatedObject, HttpMethod.DELETE, entityGet, StockDto.class);

        //Test Fetch saved Stock successfully
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    @Test
    void shouldReturnStock_whenCreateIsCalled_thenUpdateIsCalled() {
        // Create Stock
        Stock stock = createEntity();
        stock.setName("Dummy data for test update");
        StockDto dto = stockMapper.toDto(stock);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<StockDto> entity = new HttpEntity<>(dto, headers);
        ResponseEntity<String> responseEntityCreated = this.restTemplate.exchange(uri + ENTITY_API_URL, HttpMethod.POST, entity, String.class);

        //Test Created successfully
        String urlCreatedObject = responseEntityCreated.getHeaders().get("Location").get(0);//get the location of saved stock-> /v1/stocks/6
        assertEquals(HttpStatus.CREATED, responseEntityCreated.getStatusCode());

        ResponseDto<StockDto> responseDtoStockDto = toResponseDto(responseEntityCreated.getBody());

        StockDto stockDtoCreated = responseDtoStockDto.getPayload().get(0);
        assertEquals("Dummy data for test update", stockDtoCreated.getName());

        //intentionally just update the name not price. price must be null
        StockDto stockDtoForUpdate=StockDto.builder().id(stockDtoCreated.getId()).name("Updated title Dummy data for test update").build();

        // Update Stock
        HttpEntity<StockDto> entityPut = new HttpEntity<>(stockDtoForUpdate, headers);
        ResponseEntity<String> responseEntityUpdated = this.restTemplate.exchange(urlCreatedObject, HttpMethod.PUT, entityPut, String.class);

        //Test updated Stock successfully
        assertEquals(HttpStatus.OK, responseEntityUpdated.getStatusCode());

        StockDto dtoUpdated = toResponseDto(responseEntityUpdated.getBody()).getPayload().get(0);
        assertEquals(stockDtoCreated.getId(), dtoUpdated.getId());
        assertEquals("Updated title Dummy data for test update", dtoUpdated.getName());
        assertNull(dtoUpdated.getCurrentPrice()); // because I don't update it. see the partial update.
    }

    @Test
    void shouldReturnStock_whenCreateIsCalled_thenPartialUpdateIsCalled() {
        // Create Stock
        Stock stock = createEntity();
        stock.setName("Dummy data for test update");
        StockDto dto = stockMapper.toDto(stock);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/merge-patch+json");
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<StockDto> entity = new HttpEntity<>(dto, headers);
        ResponseEntity<String> responseEntityCreated = this.restTemplate.exchange(uri + ENTITY_API_URL, HttpMethod.POST, entity, String.class);

        //Test Created successfully
        String urlCreatedObject = responseEntityCreated.getHeaders().get("Location").get(0);//get the location of saved stock-> /v1/stocks/6
        assertEquals(HttpStatus.CREATED, responseEntityCreated.getStatusCode());

        ResponseDto<StockDto> responseDtoStockDto = toResponseDto(responseEntityCreated.getBody());

        StockDto stockDtoCreated = responseDtoStockDto.getPayload().get(0);
        assertEquals("Dummy data for test update", stockDtoCreated.getName());

        stockDtoCreated.setName("Updated name Dummy data for test partial update");


        HttpClient httpClient = HttpClientBuilder.create().build();
        // partial update is call
        HttpEntity<StockDto> entityPatch = new HttpEntity<>(stockDtoCreated, headers);
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
        ResponseEntity<String> responseEntityUpdated =  this.restTemplate.exchange(urlCreatedObject, HttpMethod.PATCH, entityPatch, String.class);

        //Test updated Stock successfully
        assertEquals(HttpStatus.OK, responseEntityUpdated.getStatusCode());

        StockDto dtoUpdated = toResponseDto(responseEntityUpdated.getBody()).getPayload().get(0);
        assertEquals(stockDtoCreated.getId(), dtoUpdated.getId());
        assertEquals("Updated name Dummy data for test partial update", dtoUpdated.getName());
        assertEquals(1L, dtoUpdated.getCurrentPrice()); // read it from database and replace it into stock before update it. see the update
    }


}
