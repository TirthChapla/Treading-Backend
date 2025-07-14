package com.treading_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treading_backend.model.Coin;
import com.treading_backend.repository.CoinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;


import java.util.List;
import java.util.Optional;

@Service
public class CoinServiceImpl implements CoinService{
    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Value("${coingecko.api.key}")
    private String API_KEY;



    @Override
    public List<Coin> getCoinList(int page) throws Exception
    {
        ///👉 Our API:
        ///✅   usd : https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=10&page=

        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=inr&per_page=10&page="+page;


        ///👉 This is used to make HTTP requests in Spring Boot
        RestTemplate restTemplate = new RestTemplate();
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-cg-demo-api-key", API_KEY);


            ///👉 HttpEntity represents the entire HTTP request including headers.
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);


            /// 📲 Making the GET request:

                /// exchange(...) performs the HTTP call.
                /// url: the full API URL
                /// HttpMethod.GET: it’s a GET request
                /// entity: contains headers (API key)
                /// String.class: response is received as plain text (JSON string)
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            ///👉 Printing the response JSON
            System.out.println(response.getBody());

            //👉 Converts JSON string into a List<Coin> using Jackson’s objectMapper.
            //👉 TypeReference<List<Coin>> helps Jackson understand the generic type.
            List<Coin> coins = objectMapper.readValue(response.getBody(), new TypeReference<List<Coin>>() {});

            return coins;

        } catch (HttpClientErrorException | HttpServerErrorException | JsonProcessingException e) {
            System.err.println("Error: " + e);
            // Handle error accordingly
            throw new Exception("please wait for time because you are using free plan");
        }

    }

    /// 👉 This method takes :

        ///✅ coinId – e.g., "bitcoin" or "ethereum"
        ///✅ days – how many days of chart data to fetch (e.g., 1, 7, 30)

    @Override
    public String getMarketChart(String coinId, int days) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/"+coinId+"/market_chart?vs_currency=inr&days="+days;

        RestTemplate restTemplate = new RestTemplate();
        try
        {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-cg-demo-api-key", API_KEY);

            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            /// ✅ Returns the JSON response as a raw string — it typically includes:
                /// prices array
                /// market_caps
                /// total_volumes
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("Error: " + e);
            /// Handle error accordingly
            /// return null;
            throw new Exception("you are using free plan");
        }

    }

    private double convertToDouble(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Double) {
            return (Double) value;
        } else {
            throw new IllegalArgumentException("Unsupported data type: " + value.getClass().getName());
        }
    }

    @Override
    public String getCoinDetails(String coinId) throws JsonProcessingException {

        String baseUrl ="https://api.coingecko.com/api/v3/coins/"+coinId;

        System.out.println("------------------ get coin details base url "+baseUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-cg-demo-api-key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);


        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);

//        Coin coins = objectMapper.readValue(response.getBody(), new TypeReference<>() {
//        });
//        coinRepository.save(coins);
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        jsonNode.get("image").get("large");
        System.out.println(jsonNode.get("image").get("large"));

        Coin coin = new Coin();

        coin.setId(jsonNode.get("id").asText());
        coin.setSymbol(jsonNode.get("symbol").asText());
        coin.setName(jsonNode.get("name").asText());
        coin.setImage(jsonNode.get("image").get("large").asText());

        JsonNode marketData = jsonNode.get("market_data");

        coin.setCurrentPrice(marketData.get("current_price").get("inr").asDouble());
        coin.setMarketCap(marketData.get("market_cap").get("inr").asLong());
        coin.setMarketCapRank(jsonNode.get("market_cap_rank").asInt());
        coin.setTotalVolume(marketData.get("total_volume").get("inr").asLong());
        coin.setHigh24h(marketData.get("high_24h").get("inr").asDouble());
        coin.setLow24h(marketData.get("low_24h").get("inr").asDouble());

 //        coin.setCurrentPrice(marketData.get("current_price").get("usd").asDouble());
 //        coin.setMarketCap(marketData.get("market_cap").get("usd").asLong());
 //        coin.setMarketCapRank(jsonNode.get("market_cap_rank").asInt());
 //        coin.setTotalVolume(marketData.get("total_volume").get("usd").asLong());
 //        coin.setHigh24h(marketData.get("high_24h").get("usd").asDouble());
 //        coin.setLow24h(marketData.get("low_24h").get("usd").asDouble());
        coin.setPriceChange24h(marketData.get("price_change_24h").asDouble());
        coin.setPriceChangePercentage24h(marketData.get("price_change_percentage_24h").asDouble());
        coin.setMarketCapChange24h(marketData.get("market_cap_change_24h").asLong());
        coin.setMarketCapChangePercentage24h(marketData.get("market_cap_change_percentage_24h").asDouble());
        coin.setCirculatingSupply(marketData.get("circulating_supply").asLong());
        coin.setTotalSupply(marketData.get("total_supply").asLong());

        /// 👉 Save Data to DB
        coinRepository.save(coin);
        return response.getBody();
    }

    @Override
    public Coin findById(String coinId) throws Exception{
        Optional<Coin> optionalCoin = coinRepository.findById(coinId);
        if(optionalCoin.isEmpty()) throw new Exception("invalid coin id");
        return  optionalCoin.get();
    }

    @Override
    public String searchCoin(String keyword)
    {
        // 👉 Build the CoinGecko search API URL using the provided keyword
        String baseUrl = "https://api.coingecko.com/api/v3/search?query=" + keyword;

        // 👉 Create HTTP headers and ✅ include your API key
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-cg-demo-api-key", API_KEY);  // ✅ Required to access CoinGecko API with rate limits

        // 👉 Create an HttpEntity with just the headers (no body needed for GET)
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 👉 Create a RestTemplate to perform the HTTP GET request
        RestTemplate restTemplate = new RestTemplate();

        // ✅ Make the API request and get the response as a String
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);

        // ✅ Print the raw JSON response to the console for debugging
        System.out.println(response.getBody());

        // ✅ Return the JSON string result to the caller
        return response.getBody();
    }


    @Override
    public String getTop50CoinsByMarketCapRank()
    {
        // 👉 getting top 50 coins sorted by market cap in INR
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=inr&page=1&per_page=50";

        // 👉 Create a RestTemplate to perform the HTTP GET request
        RestTemplate restTemplate = new RestTemplate();
        try
        {
            // 👉 Set up HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-cg-demo-api-key", API_KEY); // ✅ Include your CoinGecko demo API key

            // 👉 Wrap headers in an HttpEntity object
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            // ✅ Make a GET request to the CoinGecko API and store the response
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // ✅ Return the raw JSON response body containing top 50 coins
            return response.getBody();

        }
        catch (HttpClientErrorException | HttpServerErrorException e)
        {
            // ❌ Log the error if something goes wrong (like rate limit exceeded)
            System.err.println("Error: " + e);

            // 👉 Return null or handle this more gracefully in production
            return null;
        }
    }


    @Override
    public String getTreadingCoins()
    {
        // 👉 getting endpoint for trending coins
        String url = "https://api.coingecko.com/api/v3/search/trending";

        // 👉 Create RestTemplate to send the GET request
        RestTemplate restTemplate = new RestTemplate();
        try
        {
            // 👉 Set up the HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-cg-demo-api-key", API_KEY); // ✅ Add your CoinGecko demo API key for authentication

            // 👉 Create an HttpEntity with headers
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            // ✅ Send the request to the API and get the response as a String
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // ✅ Return the response body (JSON string with trending coins)
            return response.getBody();

        }
        catch (HttpClientErrorException | HttpServerErrorException e)
        {
            // ❌ Log any client/server errors like 429 Too Many Requests
            System.err.println("Error: " + e);

            // 👉 Return null or handle gracefully depending on your use case
            return null;
        }
    }

}
