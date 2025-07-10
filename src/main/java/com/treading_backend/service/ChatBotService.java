package com.treading_backend.service;

import com.treading_backend.model.CoinDTO;
import com.treading_backend.response.ApiResponse;

public interface ChatBotService {
    ApiResponse getCoinDetails(String coinName);

    CoinDTO getCoinByName(String coinName);

    String simpleChat(String prompt);
}
