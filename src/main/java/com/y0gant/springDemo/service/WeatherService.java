package com.y0gant.springDemo.service;

import com.y0gant.springDemo.cache.JournalConfigCache;
import com.y0gant.springDemo.entity.Weather;
import com.y0gant.springDemo.entity.WeatherResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private JournalConfigCache configCache;
    private Map<String, String> configs;
    /* Now fetching these values from the Database
    private static final String WEATHER = "https://api.openweathermap.org/data/2.5/weather?q={CITY}&units=metric&appid={apiKEY}";
    @Value("${weather.api.key}")
    private String apiKEY;*/

    public WeatherService(RestTemplate restTemplate, JournalConfigCache configCache) {
        this.restTemplate = restTemplate;
        this.configCache = configCache;
    }

    public Weather getWeather(String city) {
        configs = configCache.getConfigurations();
        String url = configs.get("WEATHER_URL");
        String apiKey = configs.get("API_KEY");
        String finalURL = url.replace("<city>", city).replace("<apiKey>", apiKey);
        ResponseEntity<WeatherResponse> response = restTemplate.exchange(finalURL, HttpMethod.GET, null, WeatherResponse.class);
        WeatherResponse weatherResponse = response.getBody();
        if (weatherResponse != null) {
            return weatherResponse.getMain();
        } else throw new NullPointerException("Unable to fetch user response.");
    }
}
