package com.example.stockp.controller;

import com.example.stockp.service.dto.ResponseDto;
import com.example.stockp.service.dto.StockDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.net.URISyntaxException;

/**
 * @author Mahdi Sharifi
 */

public interface StockEndpoint {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return list od stocks successfully"),
            @ApiResponse(responseCode = "400", description = "If the request is not valid. For more information read the message of the error",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "if the stock couldn't be partial updated.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))})
    })
    @Operation(summary = "Find all stocks. The pagination provided by size and page.")
    ResponseEntity<ResponseDto<StockDto>> getAllStocks(@ParameterObject Pageable pageable) throws URISyntaxException;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Find the stock successfully"),
            @ApiResponse(responseCode = "400", description = "If the request is not valid. For more information read the message of the error",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "if the stock is not found.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "if the stock couldn't be partial updated.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))})
    })
    @Operation(summary = "Find a stock by id")
    ResponseEntity<ResponseDto<StockDto>> getStock(@PathVariable Long id) throws URISyntaxException;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created successfully"),
            @ApiResponse(responseCode = "400", description = "If the request is not valid. For example if the stock has already an id.For more information read the message of the error",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "if the stock is not found.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "if the stock couldn't be partial updated.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))})
    })
    @Operation(summary = "Create a new stock by stockDto. The dto must not have an id")
    ResponseEntity<ResponseDto<StockDto>> createStock(
            @Parameter(description = "The stockDto that you want to create it.", required = true,
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StockDto.class))})
            @Valid @RequestBody StockDto stockDTO) throws URISyntaxException;//if the Location URI syntax is incorrect.

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated success"),
            @ApiResponse(responseCode = "400", description = "If the request is not valid. For more information read the message of the error",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "if the stock is not found.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "if the stock couldn't be partial updated.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))})
    })
    @Operation(summary = "Updates an existing stock. It must already have an id")
    ResponseEntity<ResponseDto<StockDto>> updateStock(
            @Parameter(description = "The id of the stock that you are going to update it.", example = "1", required = true)
            @PathVariable(value = "id", required = false) Long id,
            @Parameter(description = "The stockDto that you want to update it.", required = true,
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StockDto.class))})
            @Valid @RequestBody StockDto stockDto);


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partial updates success"),
            @ApiResponse(responseCode = "400", description = "If the request is not valid. For more information read the message of the error",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "if the stock is not found.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "500", description = "if the stock couldn't be updated.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))})
    })
    @Operation(summary = "Partial updates an existing stock. It must already have an id. " +
            "Partial updates given fields of an existing stock, field will ignore if it is null")
    ResponseEntity<ResponseDto<StockDto>> partialUpdateStock(
            @Parameter(description = "The id of the stock ayou are going to update it.", example = "1", required = true)
            @PathVariable(value = "id", required = false) Long id,
            @Parameter(description = "The stockDto that you want to update it.", required = true,
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StockDto.class))})
            @RequestBody StockDto stockDto
    );


    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "If delete is success"),
            @ApiResponse(responseCode = "404", description = "If it is not exist",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))})
    })
    @Operation(summary = "Delete an existing stock provided by id")
    ResponseEntity<ResponseDto<Void>> deleteStock(
            @Parameter(description = "The id of the stock to delete.", example = "1", required = true)
            @PathVariable Long id);
}
