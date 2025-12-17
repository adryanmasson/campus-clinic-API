package com.example.clinica.repositories;

import com.example.clinica.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {
    boolean existsByCpf(String cpf);

    Optional<Patient> findByCpf(String cpf);

    @Query(value = "SELECT dbo.calculate_age(birth_date) FROM patients WHERE patient_id = ?1", nativeQuery = true)
    Integer calculateAge(Integer patientId);

    @Query(value = """
                SELECT c.appointment_id AS appointmentId,
                       c.appointment_date AS appointmentDate,
                       c.start_time AS startTime,
                       c.end_time AS endTime,
                       c.status AS status,
                       p.record_id AS recordId,
                       p.anamnesis AS anamnesis,
                       p.diagnosis AS diagnosis,
                       p.prescription AS prescription,
                       m.name AS doctorName
                FROM appointments c
                LEFT JOIN medical_records p ON p.appointment_id = c.appointment_id
                JOIN doctors m ON m.doctor_id = c.doctor_id
                WHERE c.patient_id = :patientId
                ORDER BY c.appointment_date DESC, c.start_time DESC
            """, nativeQuery = true)
    List<Map<String, Object>> listPatientHistory(@Param("patientId") Integer patientId);

    @Query(value = """
                SELECT
                    e.name AS specialty,
                    COUNT(DISTINCT p.patient_id) AS totalPatients
                FROM appointments c
                INNER JOIN patients p ON p.patient_id = c.patient_id
                INNER JOIN doctors m ON m.doctor_id = c.doctor_id
                INNER JOIN specialties e ON e.specialty_id = m.specialty_id
                GROUP BY e.name
                ORDER BY totalPatients DESC
            """, nativeQuery = true)
    List<Map<String, Object>> countPatientsBySpecialty();

}
