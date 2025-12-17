package com.example.clinica.repositories;

import com.example.clinica.models.Prontuario;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, Integer> {

    @Query(value = "SELECT p.record_id AS idProntuario, " +
            "p.appointment_id AS idConsulta, " +
            "pac.name AS nomePaciente, " +
            "m.name AS nomeMedico, " +
            "p.anamnesis AS anamnese, " +
            "p.diagnosis AS diagnostico, " +
            "p.prescription AS prescricao, " +
            "p.record_date AS data_registro " +
            "FROM medical_records p " +
            "JOIN appointments c ON c.appointment_id = p.appointment_id " +
            "JOIN patients pac ON pac.patient_id = c.patient_id " +
            "JOIN doctors m ON m.doctor_id = c.doctor_id", nativeQuery = true)
    List<Map<String, Object>> listarProntuarios();

    @Query(value = "SELECT p.record_id AS idProntuario, " +
            "p.appointment_id AS idConsulta, " +
            "pac.name AS nomePaciente, " +
            "m.name AS nomeMedico, " +
            "p.anamnesis AS anamnese, " +
            "p.diagnosis AS diagnostico, " +
            "p.prescription AS prescricao, " +
            "p.record_date AS data_registro " +
            "FROM medical_records p " +
            "JOIN appointments c ON c.appointment_id = p.appointment_id " +
            "JOIN patients pac ON pac.patient_id = c.patient_id " +
            "JOIN doctors m ON m.doctor_id = c.doctor_id " +
            "WHERE p.appointment_id = :idConsulta", nativeQuery = true)
    Map<String, Object> findDetalhadoByConsultaId(@Param("idConsulta") Integer idConsulta);

    @Query("SELECT pr FROM Prontuario pr WHERE pr.consulta.id_consulta = :idConsulta")
    Prontuario findByConsultaId(@Param("idConsulta") Integer idConsulta);
}
