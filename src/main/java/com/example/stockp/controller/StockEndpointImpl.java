package com.example.stockp.controller;


import com.example.stockp.exception.BadRequestException;
import com.example.stockp.service.StockService;
import com.example.stockp.service.dto.ResponseDto;
import com.example.stockp.service.dto.StockDto;
import com.example.stockp.util.PaginationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

/**
 * REST controller for managing Stock.
 */
@RestController
@RequestMapping("/api/stocks")
@Tag(name = "stock-controller for handling stock requests", description = "get, create, update, partial update , and delete provided")
@Slf4j
public class StockEndpointImpl implements StockEndpoint {

    private static final String HEADER_X_TOTAL_COUNT = "X-Total-Count";
    private static final String HEADER_X_PAGE_CURRETN = "X-Page-Current";
    private static final String HEADER_X_PAGE_TOTAL = "X-Page-Total";

    private final StockService stockService;

    @Value("${hateoas.disabled}")
    private boolean disabledHateoas;

    public StockEndpointImpl(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    @GetMapping(value="", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto<StockDto>> getAllStocks(@ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Stocks;");
        Page<StockDto> page = stockService.findAll(pageable);
        ResponseDto<StockDto> responseDto = ResponseDto.<StockDto>builder().payload(page.getContent()).build();
        HttpHeaders headers = setPaginationHeader(page); // Put some useful information about pagination in header

        if (!disabledHateoas) {//We can put the link in Header as well
            List<Link> linkBuilderList = PaginationUtil.generatePaginationList(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            responseDto.add(linkBuilderList);
        }
        return ResponseEntity.ok().headers(headers).body(responseDto);
    }


    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto<StockDto>> getStock(@PathVariable Long id) {
        log.debug("REST request to get Stock : {}", id);
        ResponseDto<StockDto> responseDto = ResponseDto.<StockDto>builder()
                .payload(List.of(stockService.findOne(id)))
                .build();
        return ResponseEntity.ok(responseDto);
    }

    /**
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Override
    @PostMapping(value ="", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto<StockDto>> createStock(@Valid @RequestBody StockDto stockDto) throws URISyntaxException {
        log.debug("REST request to save Stock : {}", stockDto);
        StockDto result = stockService.save(stockDto);
        ResponseDto<StockDto> responseDto = ResponseDto.<StockDto>builder().payload(List.of(result)).build();
        return ResponseEntity
                .created(new URI(getBaseUri().toUriString() + "/" + result.getId()))
                .body(responseDto);
    }

    @Override
    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto<StockDto>> updateStock(
            @PathVariable(value = "id") final Long id, @Valid @RequestBody StockDto stockDto) {
        log.debug("REST request to update Stock : {}, {}", id, stockDto);

        if (!Objects.equals(id, stockDto.getId())) {
            throw new BadRequestException("id invalid! Id of dto must be equal to id provided by path! dto.id is: " + stockDto.getId() + " ,path id is: " + id);
        }

        StockDto result = stockService.update(stockDto);
        ResponseDto<StockDto> responseDto = ResponseDto.<StockDto>builder().httpStatus(HttpStatus.OK).payload(List.of(result)).build();
        return ResponseEntity.status(responseDto.getHttpStatus()).body(responseDto);
    }

    @Override
    @PatchMapping(value = "/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<ResponseDto<StockDto>> partialUpdateStock(
            @PathVariable(value = "id", required = false) final Long id,
            @RequestBody StockDto stockDto
    ) {
        log.debug("REST request to partial update Stock partially : {}, {}", id, stockDto);

        if (!Objects.equals(id, stockDto.getId())) {
            throw new BadRequestException("id invalid! Id of dto must be equal to id provided by path! dto.id is: " + stockDto.getId() + " ,path id is: " + id);
        }
        StockDto result = stockService.partialUpdate(stockDto);
        ResponseDto<StockDto> responseDto = ResponseDto.<StockDto>builder()
                .httpStatus(HttpStatus.OK)
                .payload(List.of(result)).build();

        return ResponseEntity.status(responseDto.getHttpStatus()).body(responseDto);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteStock(@PathVariable Long id) {
        log.debug("REST request to delete Stock : {}", id);
        stockService.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }

    private UriComponentsBuilder getBaseUri() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return ServletUriComponentsBuilder.fromServletMapping(request)
                .path(request.getRequestURI());
    }

    private HttpHeaders setPaginationHeader(Page<StockDto> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_X_TOTAL_COUNT, Long.toString(page.getTotalElements()));
        headers.add(HEADER_X_PAGE_CURRETN, Long.toString(page.getNumber()));
        headers.add(HEADER_X_PAGE_TOTAL, Long.toString(page.getTotalPages()));
        return headers;
    }
}
