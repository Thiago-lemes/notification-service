package dev.thiago.notification_service.infrastructure.persistence

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class RepositoryIntegrationTest {

    companion object {
        val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("notification_service_test")
            withUsername("postgres")
            withPassword("postgres")
            start()
        }
    }
}