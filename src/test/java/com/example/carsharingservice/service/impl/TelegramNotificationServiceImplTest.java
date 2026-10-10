package com.example.carsharingservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TelegramNotificationServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TelegramNotificationServiceImpl notificationService;

    private final String testToken = "123456:ABC-DEF1234ghIkl-qwertyuiop12345";
    private final String testChatId = "-987654321";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "botToken", testToken);
        ReflectionTestUtils.setField(notificationService, "chatId", testChatId);
    }

    @Test
    @DisplayName("Send Notification - Should successfully construct URL and call Telegram API")
    void sendNotification_ValidMessage_CallsRestTemplate() {
        String message = "Hello from Test!";
        String expectedUrl = "https://api.telegram.org/bot" + testToken + "/sendMessage?chat_id="
                + testChatId + "&text=" + message;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{\"ok\":true}");

        notificationService.sendNotification(message);

        verify(restTemplate).getForObject(expectedUrl, String.class);
    }

    @Test
    @DisplayName("Send Notification - Should catch exception and not crash application "
            + "when RestTemplate fails")
    void sendNotification_RestTemplateThrowsException_ExceptionCaught() {
        String message = "Error Test";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection timed out"));

        try {
            notificationService.sendNotification(message);
        } catch (Exception e) {
            fail("The method should have caught the exception internally, but it leaked!");
        }

        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }
}
