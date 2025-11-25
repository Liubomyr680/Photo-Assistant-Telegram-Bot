package com.example.telegrambot.service.handler;

import com.example.telegrambot.dto.WeatherResponse;
import com.example.telegrambot.interfaces.InputHandler;
import com.example.telegrambot.keyboard.KeyboardFactory;
import com.example.telegrambot.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class LocationHandler implements InputHandler {

    private static final Logger logger = LoggerFactory.getLogger(LocationHandler.class);
    private final WeatherService weatherService;

    public LocationHandler(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public boolean supports(String state) {
        return false;
    }

    @Override
    public SendMessage handle(String chatId, Message message) {
        if (!message.hasLocation()) {
            return new SendMessage(chatId,
                    "📍 Будь ласка, надішліть вашу локацію (через скріпку 📎 -> Location), щоб я міг розрахувати час сонця ☀️");
        }

        Location location = message.getLocation();
        logger.info("Handling location: lat={}, lon={}", location.getLatitude(), location.getLongitude());

        WeatherResponse weatherData = weatherService.getWeatherAndSun(location.getLatitude(), location.getLongitude());

        if (weatherData == null || weatherData.daily() == null) {
            return new SendMessage(chatId, "❌ Не вдалося отримати дані про погоду та сонце. Спробуйте пізніше.");
        }

        String report = formatReport(weatherData);
        SendMessage response = new SendMessage(chatId, report);
        response.setReplyMarkup(KeyboardFactory.mainKeyboard());
        return response;
    }

    private String formatReport(WeatherResponse data) {
        WeatherResponse.CurrentWeather current = data.currentWeather();
        String sunriseStr = data.daily().sunrise().get(0);
        String sunsetStr = data.daily().sunset().get(0);

        LocalDateTime sunrise = LocalDateTime.parse(sunriseStr, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime sunset = LocalDateTime.parse(sunsetStr, DateTimeFormatter.ISO_DATE_TIME);

        // Calculate Golden Hour (approx 1 hour after sunrise, 1 hour before sunset)
        LocalDateTime goldenMorningEnd = sunrise.plusHours(1);
        LocalDateTime goldenEveningStart = sunset.minusHours(1);

        // Calculate Blue Hour (approx 30 mins before sunrise, 30 mins after sunset)
        LocalDateTime blueMorningStart = sunrise.minusMinutes(30);
        LocalDateTime blueEveningEnd = sunset.plusMinutes(30);

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        String weatherDesc = getWeatherDescription(current.weathercode());

        return String.format("""
                📍 **Звіт по Погоді та Сонцю**

                🌤 **Поточні умови**
                • Погода: %s
                • Температура: %.1f°C
                • Вітер: %.1f м/с

                ☀️ **Розклад Сонця**
                • 🔵 Блакитна година (ранок): %s - %s
                • 🌅 Світанок: %s
                • 🌤 Золота година (ранок): %s - %s
                • 🌇 Захід: %s
                • 🌤 Золота година (вечір): %s - %s
                • 🔵 Блакитна година (вечір): %s - %s
                """,
                weatherDesc,
                current.temperature(),
                current.windspeed(),
                blueMorningStart.format(timeFmt), sunrise.format(timeFmt),
                sunrise.format(timeFmt),
                sunrise.format(timeFmt), goldenMorningEnd.format(timeFmt),
                sunset.format(timeFmt),
                goldenEveningStart.format(timeFmt), sunset.format(timeFmt),
                sunset.format(timeFmt), blueEveningEnd.format(timeFmt));
    }

    private String getWeatherDescription(int code) {
        return switch (code) {
            case 0 -> "Чисте небо ☀️";
            case 1, 2, 3 -> "Хмарно ⛅";
            case 45, 48 -> "Туман 🌫";
            case 51, 53, 55 -> "Мряка 🌧";
            case 61, 63, 65 -> "Дощ ☔";
            case 71, 73, 75 -> "Сніг ❄️";
            case 95, 96, 99 -> "Гроза ⛈";
            default -> "Невідомо";
        };
    }
}
