package com.example.clinic.h2;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers H2 aliases to mimic SQL Server-specific functions/procedures when
 * running with the in-memory H2 database (local/test). Safe to ignore in
 * production because it's conditional on the H2 driver being present.
 */
@Configuration
@ConditionalOnClass(name = "org.h2.Driver")
public class H2InitConfig {

    private final DataSource dataSource;

    public H2InitConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    CommandLineRunner registerH2Aliases() {
        return args -> {
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE SCHEMA IF NOT EXISTS dbo");
                stmt.execute("CREATE ALIAS IF NOT EXISTS CALCULATE_AGE FOR \"com.example.clinic.h2.H2Functions.calculateAge\"");
                stmt.execute("CREATE ALIAS IF NOT EXISTS DBO.CALCULATE_AGE FOR \"com.example.clinic.h2.H2Functions.calculateAge\"");
                stmt.execute("CREATE ALIAS IF NOT EXISTS CREATEAPPOINTMENT FOR \"com.example.clinic.h2.H2Functions.createAppointment\"");
                stmt.execute("CREATE ALIAS IF NOT EXISTS createAppointment FOR \"com.example.clinic.h2.H2Functions.createAppointment\"");
                stmt.execute("CREATE ALIAS IF NOT EXISTS create_appointment FOR \"com.example.clinic.h2.H2Functions.createAppointment\"");
            }
        };
    }
}
