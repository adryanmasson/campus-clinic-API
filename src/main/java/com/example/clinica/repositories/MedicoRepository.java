package com.example.clinica.repositories;

import com.example.clinica.models.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Integer> {

    boolean existsByCrm(String crm);
}
