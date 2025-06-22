package com.y0gant.springDemo.controller;

import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.entity.Weather;
import com.y0gant.springDemo.service.UserService;
import com.y0gant.springDemo.service.WeatherService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping({"/public"})
public class PublicController {

    private final UserService userService;
    private final WeatherService weatherService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.info("Trying to save new user");
        Optional<User> saved = userService.saveNewUser(user);
        if (saved.isPresent()) {
            User savedUser = saved.get();
            log.info("user saved successfully with username :{}", savedUser.getUserName());
            return ResponseEntity.ok(user);
        } else
            log.error("failed to save user with username :{}", user.getUserName());
        return ResponseEntity.badRequest().build();

    }

    @GetMapping("/{city}")
    public ResponseEntity<String> healthCheckup(@PathVariable String city) {
        log.info("Health check triggered for city: {}", city);

        String weatherReport;

        try {
            Weather response = weatherService.getWeather(city);

            if (response != null) {
                log.info("Successfully fetched weather data. Preparing weather report.");

                weatherReport = String.format("""
                                Weather Report for %s:
                                Temperature     : %.1f°C
                                Feels Like     : %.1f°C
                                Humidity       : %d%%
                                Pressure       : %d hPa
                                """,
                        city,
                        response.getTemp(),
                        response.getFeelsLike(),
                        response.getHumidity(),
                        response.getPressure()
                );
            } else {
                log.warn("Weather data unavailable for city: {}. Returning placeholder message.", city);
                weatherReport = "Weather data is currently unavailable.";
            }

        } catch (Exception e) {
            log.error("Exception occurred while fetching weather data for city: {}", city, e);
            weatherReport = "Unable to fetch weather data due to an internal error.";
        }

        String responseString = "Server is running...\n" + weatherReport;
        return ResponseEntity.ok(responseString);
    }
}
