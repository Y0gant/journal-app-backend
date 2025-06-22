package com.y0gant.springDemo.service;

import com.y0gant.springDemo.entity.Weather;
import com.y0gant.springDemo.entity.WeatherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final String APIKEY = "bc40e54194e73b4b8d3bb9e8cff71547";

    private static final String WEATHER = "https://api.openweathermap.org/data/2.5/weather?q={CITY}&units=metric&appid={APIKEY}";

    private final RestTemplate restTemplate;


    public Weather getWeather(String city) {
        String finalURL = WEATHER.replace("{CITY}", city).replace("{APIKEY}", APIKEY);
        ResponseEntity<WeatherResponse> response = restTemplate.exchange(finalURL, HttpMethod.GET, null, WeatherResponse.class);
        WeatherResponse weatherResponse = response.getBody();
        if (weatherResponse != null) {
            return weatherResponse.getMain();
        } else
            throw new NullPointerException("Unable to fetch user response.");
    }
}
