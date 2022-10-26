package com.example.stockp.controller;

import com.example.stockp.IntegrationTest;
import com.example.stockp.TestUtil;
import com.example.stockp.entity.Stock;
import com.example.stockp.exception.BadRequestException;
import com.example.stockp.exception.StockNotFoundException;
import com.example.stockp.repository.StockRepository;
import com.example.stockp.service.dto.ResponseDto;
import com.example.stockp.service.dto.StockDto;
import com.example.stockp.service.mapper.StockMapper;
import com.example.stockp.util.ConvertorUtil;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Stock Endpoint REST controller.
 * Verify that the endpoint can interact with infrastructure services such as databases and other application services.
 */

@IntegrationTest
class StockEndpointImplIT {


    private static final String NAME = "Stock#1";
    private static final String UPDATED_NAME = "Updated Stock#1";
    private static final String PARTIAL_UPDATED_NAME = "Partial Updated Stock#1";

    private static final Long CURRENT_PRICE = 1L;
    private static final Long UPDATED_CURRENT_PRICE = 2L;

    private static final String ENTITY_API_URL = "/api/stocks";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private MockMvc mockMvc;


    /**
     * Create an entity for this test.
     */
    public Stock createEntity() {
        Stock stock = new Stock()
                .name(NAME)
                .currentPrice(CURRENT_PRICE);
        return stock;
    }

    @Test
    @Transactional
    void shouldCreateStock_whenCreatStockIsCalled() throws Exception {
        long databaseSizeBeforeCreate = stockRepository.count();
        Stock stock = createEntity();
        stock.setName("Stock#1-" + ThreadLocalRandom.current().nextInt(100));
        // Create the Stock
        StockDto stockDtoForSave = stockMapper.toDto(stock);
        MvcResult result = mockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(stockDtoForSave)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error_code").value(0))
                .andExpect(jsonPath("$.payload[0].id").exists())
                .andExpect(jsonPath("$.payload[0].name").value(stock.getName()))
                .andExpect(jsonPath("$.payload[0].current_price").value(CURRENT_PRICE))
                .andExpect(jsonPath("$.payload[0].last_update").exists())
                .andReturn();

        //read the ResponseDto object from the result of mockMvc call.
        ResponseDto<StockDto> expectedResponseDtoReadFromEndpoint = ConvertorUtil.toResponseDto(result.getResponse().getContentAsString());
        StockDto expectedStockDtoReadFromEndpoint = expectedResponseDtoReadFromEndpoint.getPayload().get(0);//stock saved in the position 0 of payload

        // Validate the Stock in the database by reading just created object
        Stock stockActualAfterUpdateReadFromDb = stockRepository.findById(expectedStockDtoReadFromEndpoint.getId()).get();// read it from database
        assertThat(stockActualAfterUpdateReadFromDb.getId()).isEqualTo(expectedStockDtoReadFromEndpoint.getId());
        assertThat(stockActualAfterUpdateReadFromDb.getName()).isEqualTo(expectedStockDtoReadFromEndpoint.getName());
        assertThat(stockActualAfterUpdateReadFromDb.getCurrentPrice()).isEqualTo(expectedStockDtoReadFromEndpoint.getCurrentPrice());
        assertThat(stockActualAfterUpdateReadFromDb.getLastUpdate()).isEqualTo(expectedStockDtoReadFromEndpoint.getLastUpdate());

        long databaseSizeAfterCreate = stockRepository.count();
        // Validate the test by size database
        assertThat(databaseSizeAfterCreate).isEqualTo(databaseSizeBeforeCreate + 1);
    }
    @Test
    void shouldCThrowDataIntegrityViolationException_whenCreateRecipeIsCalled2TimesWithTheSameStockname() throws Exception {
        Stock stock = createEntity();
        stock.setName("Stock#5-" + ThreadLocalRandom.current().nextInt(100));
        // Create the Stock
        StockDto stockDtoForSave = stockMapper.toDto(stock);
         mockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(stockDtoForSave)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error_code").value(0))
                .andExpect(jsonPath("$.payload[0].id").exists())
                .andExpect(jsonPath("$.payload[0].name").value(stock.getName()))
                .andExpect(jsonPath("$.payload[0].current_price").value(CURRENT_PRICE))
                .andExpect(jsonPath("$.payload[0].last_update").exists())
                .andReturn();

        // test if we save the same name in dto from our endpoint, we get bad request with error code 4005?
        //shouldJdbcSQLIntegrityConstraintViolationExceptionWithDuplicateRecipeTitleAndUsername_whenCreateRecipeIsCalled
        mockMvc.perform(
                        post(ENTITY_API_URL)
                                .contentType(MediaType.APPLICATION_JSON).content(new Gson().toJson(stockDtoForSave)))//Duplicate stock name
                .andExpect(status().isBadRequest()).andDo(print())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DataIntegrityViolationException))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error_code").value(4005));

    }

    @Test
    @Transactional
    void shouldThrowMethodArgumentNotValidExceptionInPostRequest_whenStockNameIsNullIsCalled() throws Exception {
        // Create the Stock
        Stock stock = createEntity();
        stock.setName(null);
        // Create the Stock
        StockDto stockDtoForSave = stockMapper.toDto(stock);
        mockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(stockDtoForSave)))
                .andExpect(header().doesNotExist("Location"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }


    @Test
    @Transactional
    void shouldReturnAllStocks_whenGetAllStockIsCalled() throws Exception {
        // Initialize the database
        Stock stock=createEntity();
        stock.setName("Stock#2-" + ThreadLocalRandom.current().nextInt(1000));
        stockRepository.saveAndFlush(stock);
        long count = stockRepository.count();

        // Get all the stockList
        mockMvc
                .perform(get(ENTITY_API_URL + "?&page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Page-Current", "0"))
                .andExpect(header().exists("X-Page-Total"))
                .andExpect(header().string("X-Total-Count", (count + "")))
                .andExpect(jsonPath("$._links.last").exists())
                .andExpect(jsonPath("$._links.next").exists());

        // Get all the stockList
        mockMvc
                .perform(get(ENTITY_API_URL + "?&page=1&size=1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Page-Current", "1"))
                .andExpect(header().exists("X-Page-Total"))
                .andExpect(header().string("X-Total-Count", (count + "")))
                .andExpect(jsonPath("$._links.last").exists())
                .andExpect(jsonPath("$._links.next").exists())
                .andExpect(jsonPath("$._links.prev").exists())
                .andExpect(jsonPath("$._links.first").exists());
    }

    @Test
    @Transactional
    void shouldFindOne_whenGetByIdIsCalled() throws Exception {
        // Initialize the database
        Stock stock=createEntity();
        stock.setName("Stock#3-" + ThreadLocalRandom.current().nextInt(1000));
        stockRepository.saveAndFlush(stock);

        // Get the stock
        mockMvc
                .perform(get(ENTITY_API_URL_ID, stock.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.payload[0].id").value(stock.getId().intValue()))
                .andExpect(jsonPath("$.payload[0].name").value(stock.getName()))
                .andExpect(jsonPath("$.payload[0].current_price").value(CURRENT_PRICE.longValue()))
                .andExpect(jsonPath("$.payload[0].last_update").exists());
    }

    @Test
    @Transactional
    void shouldUpdateStockWithPut_whenUpdateIsCalled() throws Exception {
        //1- save the object
        //2- update it from endpoint
        //3- read database and check it with endpoint answer

        // Initialize the database
        Stock stockExpected = new Stock()
                .name(NAME + "-" + ThreadLocalRandom.current().nextInt(1000))
                .currentPrice(CURRENT_PRICE);

        Stock expectedSavedStock = stockRepository.saveAndFlush(stockExpected);

        long databaseSizeBeforeUpdate = stockRepository.count();

        // Update the stockDto using update. I just want to update the name
        StockDto stockDtoForUpdate = StockDto.builder()
                .id(expectedSavedStock.getId())
                .name(UPDATED_NAME).build();// for partialUpdate. I left currentPrice intentionally null. it must fill null.

        MvcResult result = mockMvc
                .perform(
                        put(ENTITY_API_URL + "/" + stockDtoForUpdate.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(new Gson().toJson(stockDtoForUpdate))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload[0].name").value(UPDATED_NAME))
                .andExpect(jsonPath("$.payload[0].current_price").doesNotExist()) // I did not update current_price
                .andReturn();

        //read the ResponseDto object from the result of mockMvc call.
        ResponseDto<StockDto> expectedResponseDtoReadFromEndpoint = ConvertorUtil.toResponseDto(result.getResponse().getContentAsString());
        StockDto expectedStockDtoReadFromEndpoint = expectedResponseDtoReadFromEndpoint.getPayload().get(0);//stock saved in the position 0 of payload

        // Validate the Stock in the database
        long databaseSizeAfterUpdate = stockRepository.count();
        assertEquals(databaseSizeBeforeUpdate, databaseSizeAfterUpdate); //It means nothing added to our database

        Stock stockActualAfterUpdateReadFromDb = stockRepository.findById(stockExpected.getId()).get();
        assertThat(stockActualAfterUpdateReadFromDb.getName()).isEqualTo(expectedStockDtoReadFromEndpoint.getName());
        assertNull(expectedStockDtoReadFromEndpoint.getCurrentPrice()); // I don't pass the current_price as an input. it must be null
        assertThat(stockActualAfterUpdateReadFromDb.getCurrentPrice()).isEqualTo(expectedStockDtoReadFromEndpoint.getCurrentPrice());
    }

    @Test
    @Transactional
    void shouldGetBadRequestUpdateStock_whenNonExistingStockUpdateIsCalled() throws Exception {
        // Create the Stock
        Stock stock=createEntity();
        StockDto expectedStockDto = stockMapper.toDto(stock);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        mockMvc
                .perform(
                        put(ENTITY_API_URL_ID, Integer.MAX_VALUE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new Gson().toJson(expectedStockDto))
                )
                .andExpect(status().isBadRequest());

    }

    @Test
    @Transactional
    void shouldGetBadRequestInPutRequest_whenStockIdIsNullUpdateIsCalled() throws Exception {
        // Create the Stock
        Stock stock=createEntity();
        StockDto expectedStockDto = stockMapper.toDto(stock);
        stockRepository.save(stock);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        mockMvc
                .perform(
                        put(ENTITY_API_URL_ID, stock.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new Gson().toJson(expectedStockDto))//  this dto id is null
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void partialUpdateStockWithPatch_whenPartialUpdateIsCalled() throws Exception {
        //1- save the object
        //2- partial update it from endpoint
        //3- read database and check it with endpoint answer
        // Initialize the database
        Stock expectedStockBeforeUpdate = new Stock()
                .name(NAME + "-" + ThreadLocalRandom.current().nextInt(1000))
                .currentPrice(CURRENT_PRICE);
        //save the stock
        Stock expectedStockAfterSaved = stockRepository.saveAndFlush(expectedStockBeforeUpdate);

        long databaseSizeBeforeUpdate = stockRepository.count();//count all save stocks

        // partial Update the stockDto using partial update. I just want to update the name
        StockDto stockDtoForPartialUpdate = StockDto.builder()
                .id(expectedStockAfterSaved.getId())
                .name(PARTIAL_UPDATED_NAME).build();// for partialUpdate. I left currentPrice intentionally null. it must fill form saved stock

        MvcResult result = mockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, stockDtoForPartialUpdate.getId())
                                .contentType("application/merge-patch+json")
                                .content(new Gson().toJson(stockDtoForPartialUpdate))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload[0].name").value(PARTIAL_UPDATED_NAME))
                .andExpect(jsonPath("$.payload[0].current_price").value(CURRENT_PRICE)) // I did not update current_price. I left currentPrice intentionally null
                .andReturn();

        //read the ResponseDto object from the result of mockMvc call.
        ResponseDto<StockDto> expectedResponseDtoReadFromEndpoint = ConvertorUtil.toResponseDto(result.getResponse().getContentAsString());
        StockDto expectedStockDtoReadFromEndpoint = expectedResponseDtoReadFromEndpoint.getPayload().get(0);//stock saved in the position 0 of payload

        // Validate the Stock in the database
        long expectedStockSizeAtferUpdate = stockRepository.count();
        assertEquals(databaseSizeBeforeUpdate, expectedStockSizeAtferUpdate); //It means nothing added to our database

        Stock expectedStockAfterPartialUpdateReadFromDb = stockRepository.findById(expectedStockBeforeUpdate.getId()).get();

        assertThat(expectedStockAfterPartialUpdateReadFromDb.getName()).isEqualTo(expectedStockDtoReadFromEndpoint.getName());
        assertThat(expectedStockAfterPartialUpdateReadFromDb.getCurrentPrice()).isEqualTo(expectedStockBeforeUpdate.getCurrentPrice()); // We don't pass CurrentPrice to our endpoint. it must have perevious result
        assertNotNull(expectedStockDtoReadFromEndpoint.getCurrentPrice());
    }

    @Test
    @Transactional
    void shouldGetBadRequestPartialUpdate_whenNonExistingStockPartialUpdateIsCalled() throws Exception {
        // Create the Stock
        Stock stock=createEntity();
        StockDto expectedStockDto = stockMapper.toDto(stock);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        mockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, Integer.MAX_VALUE)
                                .contentType("application/merge-patch+json")
                                .content(new Gson().toJson(expectedStockDto))
                )
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException));

    }

    @Test
    @Transactional
    void shouldGetBadRequestInPatchRequest_whenStockIdIsNullPartialUpdateIsCalled() throws Exception {
        // Create the Stock
        Stock stock=createEntity();
        StockDto expectedStockDto = stockMapper.toDto(stock);
        stockRepository.save(stock);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        mockMvc
                .perform(
                        patch(ENTITY_API_URL_ID, stock.getId())
                                .contentType("application/merge-patch+json")
                                .content(new Gson().toJson(expectedStockDto))//  this dto id is null
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void shouldDeleteStock_whenDeleteIsCalled() throws Exception {
        // Initialize the database
        Stock stock=createEntity();
        stockRepository.saveAndFlush(stock);

        long databaseSizeBeforeDelete = stockRepository.count();

        // Delete the stock
        mockMvc
                .perform(delete(ENTITY_API_URL_ID, stock.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Validate the database contains one less item
        long databaseSizeAfterDelete = stockRepository.count();
        assertEquals(databaseSizeBeforeDelete, databaseSizeAfterDelete + 1);
    }

    @Test
    @Transactional
    void shouldGetNotFound_whenNonExistingStockGet() throws Exception {
        // Get the stock
        mockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(result -> {assertTrue(result.getResolvedException() instanceof StockNotFoundException);});
    }


    @Test
    void shouldGetNotFound_whenPathIsNotExist() throws Exception {
        mockMvc.perform(
                        get(ENTITY_API_URL + "/not/existing-path")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void shouldGetBadRequest_whenCreateStockWithExistingId() throws Exception {
        // Create the Stock with an existing ID
        Stock stock=createEntity();
        stock.setId(1L);
        StockDto expectedStockDto = stockMapper.toDto(stock);

        long databaseSizeBeforeCreate = stockRepository.count();

        // An entity with an existing ID cannot be created, so this API call must fail
        mockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(expectedStockDto)))
                .andExpect(status().isBadRequest());

        // Validate the Stock in the database
        long databaseSizeAfterCreate = stockRepository.count();
        assertThat(databaseSizeAfterCreate).isEqualTo(databaseSizeBeforeCreate);// Nothing created
    }

}
