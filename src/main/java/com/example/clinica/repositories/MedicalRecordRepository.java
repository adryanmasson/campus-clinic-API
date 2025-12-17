package com.example.clinica.repositories;

import com.example.clinica.models.MedicalRecord;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {

        @Query(value = "SELECT p.record_id AS recordId, " +
                        "p.appointment_id AS appointmentId, " +
                        "pac.name AS nomePaciente, " +
                        "m.name AS nomeMedico, " +
                        "p.anamnesis AS anamnesis, " +
                        "p.diagnosis AS diagnosis, " +
                        "p.prescription AS prescription, " +
                        "p.record_date AS data_registro " +
                        "FROM medical_records p " +
                        "JOIN appointments c ON c.appointment_id = p.appointment_id " +
                        "JOIN patients pac ON pac.patient_id = c.patient_id " +
                        "JOIN doctors m ON m.doctor_id = c.doctor_id", nativeQuery = true)
        List<Map<String, Object>> listMedicalRecords();

        @Query(value = "SELECT p.record_id AS recordId, " +
                        "p.appointment_id AS appointmentId, " +
                        "pac.name AS nomePaciente, " +
                        "m.name AS nomeMedico, " +
                        "p.anamnesis AS anamnesis, " +
                        "p.diagnosis AS diagnosis, " +
                        "p.prescription AS prescription, " +
                        "p.record_date AS data_registro " +
                        "FROM medical_records p " +
                        "JOIN appointments c ON c.appointment_id = p.appointment_id " +
                        "JOIN patients pac ON pac.patient_id = c.patient_id " +
                        "JOIN doctors m ON m.doctor_id = c.doctor_id " +
                        "WHERE p.appointment_id = :appointmentId", nativeQuery = true)
        Map<String, Object> findDetalhadoByConsultaId(@Param("appointmentId") Integer appointmentId);

        @Query("SELECT pr FROM MedicalRecord pr WHERE pr.appointment.appointmentId = :appointmentId")
        MedicalRecord findByConsultaId(@Param("appointmentId") Integer appointmentId);
}
