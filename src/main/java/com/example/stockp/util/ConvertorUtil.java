package com.example.stockp.util;

import com.example.stockp.service.dto.ResponseDto;
import com.example.stockp.service.dto.StockDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * @author Mahdi Sharifi
 */
public enum ConvertorUtil {
    INSTANCE;

    public static ResponseDto<StockDto> toResponseDto(String jsonStrings) {
        Type collectionType = new TypeToken<ResponseDto<StockDto>>() {
        }.getType(); //because ResponseDto has a generic type we need defied TypeToken for it.
        Gson GSON = new GsonBuilder().create();
        return GSON.fromJson(jsonStrings, collectionType);
    }
}
