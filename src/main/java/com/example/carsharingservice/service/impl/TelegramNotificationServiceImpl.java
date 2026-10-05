package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class TelegramNotificationServiceImpl implements NotificationService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    @Override
    public void sendNotification(String message) {
        try {
            String url = UriComponentsBuilder.fromUriString("https://api.telegram.org")
                    .path("/bot" + botToken + "/sendMessage")
                    .queryParam("chat_id", chatId)
                    .queryParam("text", message)
                    .build()
                    .toUriString();

            System.out.println("[TELEGRAM DEBUG] Correct Target URL: " + url);

            String response = restTemplate.getForObject(url, String.class);
            System.out.println("[TELEGRAM DEBUG] Success Response from Telegram: " + response);

        } catch (Exception e) {
            System.err.println("[TELEGRAM ERROR] Failed to send notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
