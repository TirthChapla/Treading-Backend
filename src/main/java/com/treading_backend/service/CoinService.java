package com.treading_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.treading_backend.model.Coin;

import java.util.List;

public interface CoinService
{
    List<Coin> getCoinList(int page) throws Exception;

    String getMarketChart(String coinId,int days) throws Exception;

    ///👉This we get details from API
    String getCoinDetails(String coinId) throws JsonProcessingException;

    ///👉This we get details from DB
    Coin findById(String coinId) throws Exception;

    String searchCoin(String keyword);

    String getTop50CoinsByMarketCapRank();

    String getTreadingCoins();
}
