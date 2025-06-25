package com.yogant.journal.service;

import com.yogant.journal.cache.JournalConfigCache;
import com.yogant.journal.entity.Weather;
import com.yogant.journal.entity.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
        WeatherResponse cachedData = redisService.getData("weather_of_" + city, WeatherResponse.class);
        if (cachedData == null) {
            configs = configCache.getConfigurations();
            String url = configs.get("WEATHER_URL");
            String apiKey = configs.get("API_KEY");
            String finalURL = url.replace("<city>", city).replace("<apiKey>", apiKey);
            ResponseEntity<WeatherResponse> response = restTemplate.exchange(finalURL, HttpMethod.GET, null, WeatherResponse.class);
            WeatherResponse weatherResponse = response.getBody();
            if (weatherResponse != null) {
                log.info("Fetched weather data using API call");
                redisService.setData("weather_of_" + city, weatherResponse, 600L);
                return weatherResponse.getMain();
            } else throw new NullPointerException("Unable to fetch user response.");
        }
        log.info("Fetched weather data from redis cache");
        return cachedData.getMain();
    }
}
