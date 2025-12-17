package com.example.clinica.repositories;

import com.example.clinica.models.Consulta;
import com.example.clinica.models.ConsultaStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ConsultaRepository extends JpaRepository<Consulta, Integer> {

  @Query("select c from Consulta c where c.paciente.id_paciente = :id")
  List<Consulta> findByPacienteId(@Param("id") Integer id);

  boolean existsByFkIdMedicoAndStatus(Integer fkIdMedico, ConsultaStatus status);

  @Procedure(name = "Consulta.criar_consulta")
  void criarConsulta(
      @Param("id_paciente") Integer idPaciente,
      @Param("id_medico") Integer idMedico,
      @Param("data_consulta") java.time.LocalDate data,
      @Param("hora_inicio") java.time.LocalTime horaInicio,
      @Param("hora_fim") java.time.LocalTime horaFim);

  @Query(value = "SELECT TOP 1 * FROM appointments " +
      "WHERE patient_id = :idPaciente " +
      "  AND doctor_id = :idMedico " +
      "  AND appointment_date = :data " +
      "  AND start_time = :horaInicio " +
      "  AND end_time = :horaFim ", nativeQuery = true)
  Optional<Consulta> findInserted(
      @Param("idPaciente") Integer idPaciente,
      @Param("idMedico") Integer idMedico,
      @Param("data") java.time.LocalDate data,
      @Param("horaInicio") java.time.LocalTime horaInicio,
      @Param("horaFim") java.time.LocalTime horaFim);

  @Query(value = "SELECT c.* FROM appointments c " +
      "WHERE c.doctor_id = :idMedico " +
      "  AND c.appointment_date = :data " +
      "  AND ( (c.start_time <= :novoInicio AND c.end_time > :novoInicio) " +
      "     OR (c.start_time < :novoFim AND c.end_time >= :novoFim) " +
      "     OR (c.start_time >= :novoInicio AND c.end_time <= :novoFim) ) " +
      "  AND c.appointment_id <> :excludeId", nativeQuery = true)
  List<Consulta> findByMedicoDataHora(@Param("idMedico") Integer idMedico,
      @Param("data") LocalDate data,
      @Param("novoInicio") LocalTime novoInicio,
      @Param("novoFim") LocalTime novoFim,
      @Param("excludeId") Integer excludeId);

  @Query(value = "SELECT c.appointment_id AS id, c.appointment_date, c.start_time, c.end_time, c.status, " +
      "p.name AS nome_paciente, m.name AS nome_medico " +
      "FROM appointments c " +
      "INNER JOIN patients p ON p.patient_id = c.patient_id " +
      "INNER JOIN doctors m ON m.doctor_id = c.doctor_id " +
      "WHERE c.appointment_id = :id", nativeQuery = true)
  ConsultaDetalhadaProjection buscarConsultaDetalhada(@Param("id") Integer id);

  @Query(value = "SELECT c.appointment_id AS id, c.appointment_date, c.start_time, c.end_time, c.status, " +
      "p.name AS nome_paciente, m.name AS nome_medico " +
      "FROM appointments c " +
      "INNER JOIN patients p ON p.patient_id = c.patient_id " +
      "INNER JOIN doctors m ON m.doctor_id = c.doctor_id " +
      "WHERE c.doctor_id = :idMedico", nativeQuery = true)
  List<Map<String, Object>> buscarConsultasPorMedico(@Param("idMedico") Integer idMedico);

  @Query(value = "SELECT c.appointment_id AS id, c.appointment_date, c.start_time, c.end_time, c.status, " +
      "p.name AS nome_paciente, m.name AS nome_medico " +
      "FROM appointments c " +
      "INNER JOIN patients p ON p.patient_id = c.patient_id " +
      "INNER JOIN doctors m ON m.doctor_id = c.doctor_id " +
      "WHERE c.appointment_date = :data", nativeQuery = true)
  List<Map<String, Object>> buscarConsultasPorData(@Param("data") LocalDate data);

  @Query(value = """
      SELECT
          c.appointment_id AS idConsulta,
          c.appointment_date AS dataConsulta,
          c.start_time AS horaInicio,
          c.end_time AS horaFim,
          c.status AS status,
          m.name AS nomeMedico,
          p.name AS nomePaciente
      FROM appointments c
      INNER JOIN doctors m ON m.doctor_id = c.doctor_id
      INNER JOIN patients p ON p.patient_id = c.patient_id
      WHERE c.patient_id = :idPaciente
        AND c.appointment_date >= DATEADD(MONTH, -:meses, CAST(GETDATE() AS DATE))
      ORDER BY c.appointment_date DESC
      """, nativeQuery = true)
  List<Map<String, Object>> relatorioConsultasUltimosMeses(
      @Param("idPaciente") Integer idPaciente,
      @Param("meses") Integer meses);

  @Query(value = """
      SELECT
          c.appointment_id AS idConsulta,
          c.appointment_date AS dataConsulta,
          c.start_time AS horaInicio,
          c.end_time AS horaFim,
          c.status AS status,
          m.name AS nomeMedico,
          p.name AS nomePaciente
      FROM appointments c
      INNER JOIN doctors m ON m.doctor_id = c.doctor_id
      INNER JOIN patients p ON p.patient_id = c.patient_id
      WHERE c.doctor_id = :idMedico
      ORDER BY c.appointment_date DESC
      """, nativeQuery = true)
  List<Map<String, Object>> relatorioConsultasPorMedico(@Param("idMedico") Integer idMedico);

  @Query(value = """
      SELECT TOP 50
          c.appointment_id AS idConsulta,
          c.appointment_date AS dataConsulta,
          c.start_time AS horaInicio,
          c.end_time AS horaFim,
          c.status AS status,
          p.name AS nomePaciente,
          m.name AS nomeMedico
      FROM appointments c
      INNER JOIN patients p ON p.patient_id = c.patient_id
      INNER JOIN doctors m ON m.doctor_id = c.doctor_id
      WHERE c.doctor_id = :idMedico
        AND (c.appointment_date > CAST(GETDATE() AS DATE) OR (c.appointment_date = CAST(GETDATE() AS DATE) AND c.end_time >= CAST(GETDATE() AS time)))
      ORDER BY c.appointment_date ASC, c.start_time ASC

      """, nativeQuery = true)
  List<Map<String, Object>> relatorioProximasConsultas(@Param("idMedico") Integer idMedico);

  @Query("SELECT c FROM Consulta c WHERE c.fkIdMedico = :idMedico AND c.data_consulta = :data")
  List<Consulta> findByMedicoIdAndDataConsulta(@Param("idMedico") Integer idMedico,
      @Param("data") LocalDate data);

}
