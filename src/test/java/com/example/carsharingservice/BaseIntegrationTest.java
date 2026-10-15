package com.example.carsharingservice;


import com.stripe.StripeClient;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

@AutoConfigureMockMvc(addFilters = false)
public abstract class BaseIntegrationTest {

    @MockitoBean
    protected StripeClient stripeClient;

    @Container
    @ServiceConnection
    protected static final MySQLContainer<?> mySqlContainer = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    static {
        mySqlContainer.start();
    }
}
