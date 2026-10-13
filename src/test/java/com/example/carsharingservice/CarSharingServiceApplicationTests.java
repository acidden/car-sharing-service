package com.example.carsharingservice;

import com.stripe.StripeClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CarSharingServiceApplicationTests {

    @MockitoBean
    private StripeClient stripeClient;

	@Test
	void contextLoads() {
	}
}
