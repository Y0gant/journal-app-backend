package com.yogant.journal.controller;

import com.yogant.journal.entity.User;
import com.yogant.journal.entity.Weather;
import com.yogant.journal.model.LoginDTO;
import com.yogant.journal.model.SaveNewUserDTO;
import com.yogant.journal.service.UserDetailServiceImpl;
import com.yogant.journal.service.UserService;
import com.yogant.journal.service.WeatherService;
import com.yogant.journal.utils.JwtUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping({"/public"})
public class PublicController {

    private final UserService userService;
    private final WeatherService weatherService;
    private final AuthenticationManager authenticationManager;
    private UserDetailServiceImpl userDetailService;
    private JwtUtils jwtUtils;

    @PostMapping("/signup")
    public ResponseEntity<User> signUp(@RequestBody SaveNewUserDTO user) {
        log.info("Trying to save new user");
        Optional<User> saved = userService.saveNewUser(user);
        if (saved.isPresent()) {
            User savedUser = saved.get();
            log.info("user saved successfully with username :{}", savedUser.getUserName());
            return ResponseEntity.ok(savedUser);
        } else
            log.error("failed to save user with username :{}", user.getUserName());
        return ResponseEntity.badRequest().build();

    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO user) {
        try {
            log.info("Trying to login..");
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword()));
            log.info("user authenticated generating jwt token");
            UserDetails userDetails = userDetailService.loadUserByUsername(user.getUserName());
            String token = jwtUtils.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(token);
        } catch (AuthenticationException e) {
            log.warn("Unable to authenticate user with user name {} {}", user.getUserName(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("failed to login user with username :{}", user.getUserName());

        }
        return ResponseEntity.badRequest().build();

    }


    @GetMapping("/{city}")
    public ResponseEntity<String> healthCheckup(@PathVariable String city) {
        log.info("Health check triggered for city: {}", city);

        String weatherReport;

        try {
            if (!city.matches("^[a-zA-Z\\s-]{2,50}$")) {
                throw new IllegalArgumentException("Invalid city format");
            }
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
