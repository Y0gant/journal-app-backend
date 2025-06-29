package com.yogant.journal.service;

import com.yogant.journal.cache.JournalConfigCache;
import com.yogant.journal.entity.Weather;
import com.yogant.journal.entity.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private JournalConfigCache configCache;
    private Map<String, String> configs;
    private RedisService redisService;



    /* Now fetching these values from the Database
    private static final String WEATHER = "https://api.openweathermap.org/data/2.5/weather?q={CITY}&units=metric&appid={apiKEY}";
    @Value("${weather.api.key}")
    private String apiKEY;*/


    public WeatherService(RestTemplate restTemplate, JournalConfigCache configCache, RedisService redisService) {
        this.restTemplate = restTemplate;
        this.configCache = configCache;
        this.redisService = redisService;
    }

    public Weather getWeather(String city) {
        if (!city.matches("^[a-zA-Z\\s-]{2,50}$")) {
            throw new IllegalArgumentException("Invalid city format");
        }
        String cacheKey = "weather_of_" + city;
        WeatherResponse cachedData = redisService.getData(cacheKey, WeatherResponse.class);
        if (cachedData != null) {
            log.info("Fetched weather data from redis cache");
            return cachedData.getMain();
        }
        configs = configCache.getConfigurations();
        String apiKey = configs.get("API_KEY");
        URI uri = UriComponentsBuilder
                .fromUri(URI.create("https://api.openweathermap.org/data/2.5/weather"))
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build(true)
                .toUri();
        ResponseEntity<WeatherResponse> response = restTemplate.exchange(uri, HttpMethod.GET, null, WeatherResponse.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            log.info("Fetched weather data using API call");
            redisService.setData(cacheKey, response.getBody(), 600L);
            return response.getBody().getMain();
        } else {
            throw new NullPointerException("Failed to fetch weather data");
        }
    }
}
