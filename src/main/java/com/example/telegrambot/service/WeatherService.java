package com.example.telegrambot.service;

import com.example.telegrambot.dto.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast";

    private final RestTemplate restTemplate;

    public WeatherService() {
        this.restTemplate = new RestTemplate();
    }

    public WeatherResponse getWeatherAndSun(double latitude, double longitude) {
        try {
            String url = UriComponentsBuilder.fromUriString(API_URL)
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("current_weather", true)
                    .queryParam("daily", "sunrise,sunset")
                    .queryParam("timezone", "auto")
                    .toUriString();

            logger.info("Fetching weather data from: {}", url);
            return restTemplate.getForObject(url, WeatherResponse.class);
        } catch (Exception e) {
            logger.error("Failed to fetch weather data", e);
            return null;
        }
    }
}
