package com.example.clinica.repositories;

import com.example.clinica.models.Doctor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    boolean existsByCrm(String medicalLicense);

    List<Doctor> findByEspecialidadeIdEspecialidade(Integer specialtyId);
}
