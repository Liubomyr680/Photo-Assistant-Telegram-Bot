package com.example.telegrambot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherResponse(
        @JsonProperty("current_weather") CurrentWeather currentWeather,
        @JsonProperty("daily") Daily daily) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentWeather(
            @JsonProperty("temperature") double temperature,
            @JsonProperty("windspeed") double windspeed,
            @JsonProperty("weathercode") int weathercode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Daily(
            @JsonProperty("sunrise") List<String> sunrise,
            @JsonProperty("sunset") List<String> sunset) {
    }
}
