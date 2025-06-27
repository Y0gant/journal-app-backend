package com.yogant.journal.service;

import com.yogant.journal.cache.JournalConfigCache;
import com.yogant.journal.entity.Weather;
import com.yogant.journal.entity.WeatherResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @InjectMocks
    private WeatherService weatherService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JournalConfigCache configCache;

    @Mock
    private RedisService redisService;


    @Test
    void testGetWeather_WhenDataInCache_ReturnsCachedWeather() {

        String city = "pune";
        Weather weather = new Weather();
        weather.setTemp(24.62);
        weather.setPressure(1005);
        weather.setHumidity(85);
        WeatherResponse cachedResponse = new WeatherResponse();
        cachedResponse.setMain(weather);

        when(redisService.getData("weather_of_" + city, WeatherResponse.class)).thenReturn(cachedResponse);


        Weather result = weatherService.getWeather(city);


        assertNotNull(result);
        assertEquals(24.62, result.getTemp());
        assertEquals(1005, result.getPressure());
        assertEquals(85, result.getHumidity());
        verify(redisService, times(1)).getData("weather_of_" + city, WeatherResponse.class);
        verifyNoMoreInteractions(redisService, restTemplate, configCache);
    }

    @Test
    void testGetWeather_WhenDataNotInCache_FetchFromAPIAndCacheIt() {

        String city = "Pune";
        String finalUrl = "https://api.openweathermap.org/data/2.5/weather?q=Pune&units=metric&appid=12345";

        when(redisService.getData("weather_of_" + city, WeatherResponse.class)).thenReturn(null);

        Map<String, String> configs = new HashMap<>();
        configs.put("WEATHER_URL", "https://api.openweathermap.org/data/2.5/weather?q=<city>&units=metric&appid=<apiKey>");
        configs.put("API_KEY", "12345");

        when(configCache.getConfigurations()).thenReturn(configs);

        Weather mockWeather = new Weather();
        mockWeather.setTemp(30.0);

        WeatherResponse apiResponse = new WeatherResponse();
        apiResponse.setMain(mockWeather);

        ResponseEntity<WeatherResponse> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        when(restTemplate.exchange(eq(finalUrl), eq(org.springframework.http.HttpMethod.GET), isNull(), eq(WeatherResponse.class)))
                .thenReturn(responseEntity);


        Weather result = weatherService.getWeather(city);


        assertNotNull(result);
        assertEquals(30.0, result.getTemp());

        verify(redisService).getData("weather_of_" + city, WeatherResponse.class);
        verify(configCache).getConfigurations();
        verify(restTemplate).exchange(eq(finalUrl), eq(org.springframework.http.HttpMethod.GET), isNull(), eq(WeatherResponse.class));
        verify(redisService).setData("weather_of_" + city, apiResponse, 600L);
    }

    @Test
    void testGetWeather_WhenAPIResponseIsNull_ThrowsException() {

        String city = "Mumbai";
        when(redisService.getData("weather_of_" + city, WeatherResponse.class)).thenReturn(null);

        Map<String, String> configs = new HashMap<>();
        configs.put("WEATHER_URL", "https://api.openweathermap.org/data/2.5/weather?q=<city>&units=metric&appid=<apiKey>");
        configs.put("API_KEY", "dummyKey");

        when(configCache.getConfigurations()).thenReturn(configs);

        ResponseEntity<WeatherResponse> nullResponse = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(org.springframework.http.HttpMethod.GET), isNull(), eq(WeatherResponse.class)))
                .thenReturn(nullResponse);


        assertThrows(NullPointerException.class, () -> weatherService.getWeather(city));
    }
}
