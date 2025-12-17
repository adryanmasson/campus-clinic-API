package com.example.clinic.h2;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.Period;

/**
 * H2-compatible implementations for SQL Server-specific functions/procedures
 * used by the application. These are registered as ALIAS entries during
 * startup when the H2 driver is present (local/test runs).
 */
public final class H2Functions {

    private H2Functions() {
    }

    public static Integer calculateAge(Date birthDate) {
        if (birthDate == null) {
            return null;
        }
        LocalDate dob = birthDate.toLocalDate();
        return Period.between(dob, LocalDate.now()).getYears();
    }

    public static void createAppointment(Connection conn,
                                         Integer patientId,
                                         Integer doctorId,
                                         Date appointmentDate,
                                         Time startTime,
                                         Time endTime) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection is required");
        }
        if (patientId == null || doctorId == null || appointmentDate == null || startTime == null || endTime == null) {
            throw new SQLException("Invalid appointment parameters");
        }

        // Check for overlapping appointments for the doctor on the same date.
        String conflictSql = """
                SELECT COUNT(*) FROM appointments
                WHERE doctor_id = ? AND appointment_date = ? AND status = 'SCHEDULED'
                  AND ((start_time < ? AND end_time > ?)
                    OR (start_time < ? AND end_time > ?)
                    OR (start_time >= ? AND end_time <= ?))
                """;

        try (PreparedStatement ps = conn.prepareStatement(conflictSql)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, appointmentDate);
            ps.setTime(3, startTime);
            ps.setTime(4, startTime);
            ps.setTime(5, endTime);
            ps.setTime(6, endTime);
            ps.setTime(7, startTime);
            ps.setTime(8, endTime);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Doctor already has an appointment scheduled at this time.", "45000");
                }
            }
        }

        String insertSql = """
                INSERT INTO appointments (patient_id, doctor_id, appointment_date, start_time, end_time, status)
                VALUES (?, ?, ?, ?, ?, 'SCHEDULED')
                """;

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setDate(3, appointmentDate);
            ps.setTime(4, startTime);
            ps.setTime(5, endTime);
            ps.executeUpdate();
        }
    }
}
