package com.y0gant.springDemo.service;

import com.y0gant.springDemo.entity.Weather;
import com.y0gant.springDemo.entity.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    private static final String WEATHER = "https://api.openweathermap.org/data/2.5/weather?q={CITY}&units=metric&appid={apiKEY}";
    private final RestTemplate restTemplate;
    @Value("${weather.api.key}")
    private String apiKEY;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Weather getWeather(String city) {
        String finalURL = WEATHER.replace("{CITY}", city).replace("{apiKEY}", apiKEY);
        ResponseEntity<WeatherResponse> response = restTemplate.exchange(finalURL, HttpMethod.GET, null, WeatherResponse.class);
        WeatherResponse weatherResponse = response.getBody();
        if (weatherResponse != null) {
            return weatherResponse.getMain();
        } else
            throw new NullPointerException("Unable to fetch user response.");
    }
}
