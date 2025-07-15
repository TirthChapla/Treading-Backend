package com.treading_backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treading_backend.model.Coin;
import com.treading_backend.service.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coins")
public class CoinController {

    @Autowired
    private CoinService coinService;

    @Autowired
    private ObjectMapper objectMapper;

    // 👉 Get coin list for a specific page
    @GetMapping
    ResponseEntity<List<Coin>> getCoinList(@RequestParam("page") int page) throws Exception
    {
        // ✅ Fetch paginated list of coins (10 per page)
        List<Coin> coins = coinService.getCoinList(page);

        // ✅ Return list with 200 OK
        return new ResponseEntity<>(coins, HttpStatus.OK);
    }

    // 👉 Get market chart for a coin by ID and number of days
    @GetMapping("/{coinId}/chart")
    ResponseEntity<JsonNode> getMarketChart(@PathVariable String coinId,
                                            @RequestParam("days") int days) throws Exception
    {
        // ✅ Call service to get JSON as a string
        String coins = coinService.getMarketChart(coinId, days);

        // ✅ Parse JSON string to JsonNode for structured access
        JsonNode jsonNode = objectMapper.readTree(coins);

        return ResponseEntity.ok(jsonNode); // ✅ Return JSON data
    }

    // 👉 Search coin by keyword
    @GetMapping("/search")
    ResponseEntity<JsonNode> searchCoin(@RequestParam("q") String keyword) throws JsonProcessingException
    {
        // ✅ Call service to search coin using keyword
        String coin = coinService.searchCoin(keyword);

        // ✅ Convert JSON string to JsonNode
        JsonNode jsonNode = objectMapper.readTree(coin);

        return ResponseEntity.ok(jsonNode); // ✅ Return search result
    }

    // 👉 Get top 50 coins sorted by market cap
    @GetMapping("/top50")
    ResponseEntity<JsonNode> getTop50CoinByMarketCapRank() throws JsonProcessingException
    {
        // ✅ Call service to get top 50 coins
        String coin = coinService.getTop50CoinsByMarketCapRank();

        // ✅ Parse the JSON string to JsonNode
        JsonNode jsonNode = objectMapper.readTree(coin);

        return ResponseEntity.ok(jsonNode); // ✅ Return top 50 list
    }

    // 👉 Get trending coins
    @GetMapping("/trading")
    ResponseEntity<JsonNode> getTreadingCoin() throws JsonProcessingException
    {
        // ✅ Call service to get trending coins
        String coin = coinService.getTreadingCoins();

        // ✅ Parse the JSON response into JsonNode
        JsonNode jsonNode = objectMapper.readTree(coin);

        return ResponseEntity.ok(jsonNode); // ✅ Return trending coins list
    }

    // 👉 Get full details of a specific coin by ID
    @GetMapping("/details/{coinId}")
    ResponseEntity<JsonNode> getCoinDetails(@PathVariable String coinId) throws JsonProcessingException
    {
        // ✅ Call service to fetch detailed info of the coin
        String coin = coinService.getCoinDetails(coinId);

        // ✅ Convert raw JSON string to JsonNode
        JsonNode jsonNode = objectMapper.readTree(coin);

        return ResponseEntity.ok(jsonNode); // ✅ Return coin details
    }


}
