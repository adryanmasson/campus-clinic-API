package com.example.clinica.repositories;

import com.example.clinica.models.Consulta;
import com.example.clinica.models.ConsultaStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Integer> {

    @Query("select c from Consulta c where c.paciente.id_paciente = :id")
    List<Consulta> findByPacienteId(@Param("id") Integer id);

    boolean existsByFkIdMedicoAndStatus(Integer fkIdMedico, ConsultaStatus status);

}
