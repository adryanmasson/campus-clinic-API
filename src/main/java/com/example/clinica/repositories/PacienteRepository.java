package com.example.clinica.repositories;

import com.example.clinica.models.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    boolean existsByCpf(String cpf);

    Optional<Paciente> findByCpf(String cpf);
}
