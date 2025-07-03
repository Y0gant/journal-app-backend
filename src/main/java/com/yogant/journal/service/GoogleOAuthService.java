package com.yogant.journal.service;

import com.yogant.journal.dto.GoogleAuthResponseDTO;
import com.yogant.journal.entity.User;
import com.yogant.journal.repository.UserRepo;
import com.yogant.journal.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class GoogleOAuthService {

    private final RestTemplate restTemplate;
    private final UserDetailServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepository;
    private final JwtUtils jwtUtil;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public GoogleOAuthService(RestTemplate restTemplate,
                              UserDetailServiceImpl userDetailsService,
                              PasswordEncoder passwordEncoder,
                              UserRepo userRepository,
                              JwtUtils jwtUtil) {
        this.restTemplate = restTemplate;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public GoogleAuthResponseDTO authHandler(String code) {
        try {
            // STEP 1: Exchange code for tokens
            String tokenEndpoint = "https://oauth2.googleapis.com/token";
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", "https://developers.google.com/oauthplayground");
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);

            if (tokenResponse.getStatusCode() != HttpStatus.OK || tokenResponse.getBody() == null) {
                throw new RuntimeException("Token exchange failed");
            }

            String idToken = (String) tokenResponse.getBody().get("id_token");

            // STEP 2: Get user info from id_token
            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);

            if (userInfoResponse.getStatusCode() != HttpStatus.OK || userInfoResponse.getBody() == null) {
                throw new RuntimeException("User info fetch failed");
            }

            Map<String, Object> userInfo = userInfoResponse.getBody();
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String picture = (String) userInfo.get("picture");

            // STEP 3: Check or create user in DB
            UserDetails userDetails ;
            Map<String ,Object> claims = new HashMap<>();
            try {
              userDetails = userDetailsService.loadUserByUsername(name);
              if (userDetails!=null) {
                  User byUserName = userRepository.findByUserName(userDetails.getUsername());
                  claims.put("email",byUserName.getEmail());
                  claims.put("sentiment_analysis",byUserName.isSentimentAnalysis());
                  claims.put("roles",byUserName.getRoles());
              }
            } catch (Exception e) {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUserName(name);
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                newUser.setRoles(Collections.singletonList("USER"));
                User saved = userRepository.save(newUser);
                claims.put("email",saved.getEmail());
                claims.put("sentiment_analysis",saved.isSentimentAnalysis());
                claims.put("roles",saved.getRoles());
            }

            // STEP 4: Generate JWT
            String jwtToken = jwtUtil.generateToken(name,claims);

            return new GoogleAuthResponseDTO(jwtToken, email, name, picture);

        } catch (Exception e) {
            log.error("OAuth processing failed: ", e);
            throw new RuntimeException("Google login failed");
        }
    }
}
