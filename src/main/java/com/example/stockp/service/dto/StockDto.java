package com.example.stockp.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * A Dto for the Stock entity.
 */
@Data
@Builder
@Schema(description = "save Stock data")
public class StockDto {

    private Long id;

    @NotNull(message = "#name can't be null")
    @Size(min = 2, max = 60, message = "#size is important for name")
    private String name;

    @JsonProperty("current_price")
    @SerializedName("current_price")//for converting string to object for IT test
    private Long currentPrice;

    @JsonProperty("last_update")
    @SerializedName("last_update")
    private Date lastUpdate;

}
