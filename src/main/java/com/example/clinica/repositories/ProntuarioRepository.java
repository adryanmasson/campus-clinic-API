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

    @Query(value = "SELECT p.id_prontuario AS idProntuario, " +
            "p.fk_id_consulta AS idConsulta, " +
            "pac.nome AS nomePaciente, " +
            "m.nome AS nomeMedico, " +
            "p.anamnese AS anamnese, " +
            "p.diagnostico AS diagnostico, " +
            "p.prescricao AS prescricao, " +
            "p.data_registro AS data_registro " +
            "FROM prontuarios p " +
            "JOIN consultas c ON c.id_consulta = p.fk_id_consulta " +
            "JOIN pacientes pac ON pac.id_paciente = c.fk_id_paciente " +
            "JOIN medicos m ON m.id_medico = c.fk_id_medico", nativeQuery = true)
    List<Map<String, Object>> listarProntuarios();

    @Query(value = "SELECT p.id_prontuario AS idProntuario, " +
            "p.fk_id_consulta AS idConsulta, " +
            "pac.nome AS nomePaciente, " +
            "m.nome AS nomeMedico, " +
            "p.anamnese AS anamnese, " +
            "p.diagnostico AS diagnostico, " +
            "p.prescricao AS prescricao, " +
            "p.data_registro AS data_registro " +
            "FROM prontuarios p " +
            "JOIN consultas c ON c.id_consulta = p.fk_id_consulta " +
            "JOIN pacientes pac ON pac.id_paciente = c.fk_id_paciente " +
            "JOIN medicos m ON m.id_medico = c.fk_id_medico " +
            "WHERE p.fk_id_consulta = :idConsulta", nativeQuery = true)
    Map<String, Object> findDetalhadoByConsultaId(@Param("idConsulta") Integer idConsulta);
}
