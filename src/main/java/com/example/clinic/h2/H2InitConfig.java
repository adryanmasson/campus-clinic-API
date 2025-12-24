package com.example.clinic.h2;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Registers H2 aliases to mimic SQL Server-specific functions/procedures when
 * running with the in-memory H2 database (local/test). In production, this
 * runner is disabled via property.
 */
@Configuration
@Profile("h2")
@ConditionalOnProperty(name = "app.h2.aliases.enabled", havingValue = "true", matchIfMissing = false)
public class H2InitConfig {

    private final DataSource dataSource;

    public H2InitConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    CommandLineRunner registerH2Aliases() {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                // Only register aliases when connection is H2.
                String url = conn.getMetaData() != null ? conn.getMetaData().getURL() : null;
                if (url == null || !url.startsWith("jdbc:h2")) {
                    return;
                }

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE SCHEMA IF NOT EXISTS dbo");
                    stmt.execute(
                            "CREATE ALIAS IF NOT EXISTS CALCULATE_AGE FOR \"com.example.clinic.h2.H2Functions.calculateAge\"");
                    stmt.execute(
                            "CREATE ALIAS IF NOT EXISTS DBO.CALCULATE_AGE FOR \"com.example.clinic.h2.H2Functions.calculateAge\"");
                    stmt.execute(
                            "CREATE ALIAS IF NOT EXISTS CREATEAPPOINTMENT FOR \"com.example.clinic.h2.H2Functions.createAppointment\"");
                    stmt.execute(
                            "CREATE ALIAS IF NOT EXISTS createAppointment FOR \"com.example.clinic.h2.H2Functions.createAppointment\"");
                    stmt.execute(
                            "CREATE ALIAS IF NOT EXISTS create_appointment FOR \"com.example.clinic.h2.H2Functions.createAppointment\"");
                }
            }
        };
    }
}
