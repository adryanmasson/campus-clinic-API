package com.example.clinica.services;

import com.example.clinica.models.Specialty;
import com.example.clinica.repositories.SpecialtyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecialtyService {

    private final SpecialtyRepository especialidadeRepository;

    public SpecialtyService(SpecialtyRepository especialidadeRepository) {
        this.especialidadeRepository = especialidadeRepository;
    }

    public List<Specialty> listSpecialties() {
        return especialidadeRepository.findAll();
    }

    public Specialty findSpecialtyById(Integer id) {
        return especialidadeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Specialty não encontrada com id " + id));
    }

    public Specialty createSpecialty(Specialty specialty) {
        return especialidadeRepository.save(specialty);
    }

    public Specialty updateSpecialty(Integer id, Specialty updatedSpecialty) {
        Specialty existente = findSpecialtyById(id);
        existente.setName(updatedSpecialty.getName());
        existente.setDescription(updatedSpecialty.getDescription());
        return especialidadeRepository.save(existente);
    }

    public void deleteSpecialty(Integer id) {
        Specialty existente = findSpecialtyById(id);
        especialidadeRepository.delete(existente);
    }

}
