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
      @Param("p_id_paciente") Integer idPaciente,
      @Param("p_id_medico") Integer idMedico,
      @Param("p_data_consulta") java.time.LocalDate data,
      @Param("p_hora_inicio") java.time.LocalTime horaInicio,
      @Param("p_hora_fim") java.time.LocalTime horaFim);

  @Query("SELECT c FROM Consulta c " +
      "WHERE c.fkIdMedico = :idMedico " +
      "  AND c.paciente.id_paciente = :idPaciente " +
      "  AND c.data_consulta = :data " +
      "  AND c.hora_inicio = :horaInicio " +
      "  AND c.hora_fim = :horaFim")
  Optional<Consulta> findInserted(
      @Param("idPaciente") Integer idPaciente,
      @Param("idMedico") Integer idMedico,
      @Param("data") java.time.LocalDate data,
      @Param("horaInicio") java.time.LocalTime horaInicio,
      @Param("horaFim") java.time.LocalTime horaFim);

  @Query(value = "SELECT c.* FROM consultas c " +
      "WHERE c.fk_id_medico = :idMedico " +
      "  AND c.data_consulta = :data " +
      "  AND ( (c.hora_inicio <= :novoInicio AND c.hora_fim > :novoInicio) " +
      "     OR (c.hora_inicio < :novoFim AND c.hora_fim >= :novoFim) " +
      "     OR (c.hora_inicio >= :novoInicio AND c.hora_fim <= :novoFim) ) " +
      "  AND c.id_consulta <> :excludeId", nativeQuery = true)
  List<Consulta> findByMedicoDataHora(@Param("idMedico") Integer idMedico,
      @Param("data") LocalDate data,
      @Param("novoInicio") LocalTime novoInicio,
      @Param("novoFim") LocalTime novoFim,
      @Param("excludeId") Integer excludeId);

  @Query(value = "SELECT c.id_consulta AS id, c.data_consulta, c.hora_inicio, c.hora_fim, c.status, " +
      "p.nome AS nome_paciente, m.nome AS nome_medico " +
      "FROM consultas c " +
      "INNER JOIN pacientes p ON p.id_paciente = c.fk_id_paciente " +
      "INNER JOIN medicos m ON m.id_medico = c.fk_id_medico " +
      "WHERE c.id_consulta = :id", nativeQuery = true)
  ConsultaDetalhadaProjection buscarConsultaDetalhada(@Param("id") Integer id);

  @Query(value = "SELECT c.id_consulta AS id, c.data_consulta, c.hora_inicio, c.hora_fim, c.status, " +
      "p.nome AS nome_paciente, m.nome AS nome_medico " +
      "FROM consultas c " +
      "INNER JOIN pacientes p ON p.id_paciente = c.fk_id_paciente " +
      "INNER JOIN medicos m ON m.id_medico = c.fk_id_medico " +
      "WHERE c.fk_id_medico = :idMedico", nativeQuery = true)
  List<Map<String, Object>> buscarConsultasPorMedico(@Param("idMedico") Integer idMedico);

  @Query(value = "SELECT c.id_consulta AS id, c.data_consulta, c.hora_inicio, c.hora_fim, c.status, " +
      "p.nome AS nome_paciente, m.nome AS nome_medico " +
      "FROM consultas c " +
      "INNER JOIN pacientes p ON p.id_paciente = c.fk_id_paciente " +
      "INNER JOIN medicos m ON m.id_medico = c.fk_id_medico " +
      "WHERE c.data_consulta = :data", nativeQuery = true)
  List<Map<String, Object>> buscarConsultasPorData(@Param("data") LocalDate data);

}
