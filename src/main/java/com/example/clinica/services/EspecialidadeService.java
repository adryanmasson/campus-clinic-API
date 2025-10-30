package com.example.clinica.services;

import com.example.clinica.models.Especialidade;
import com.example.clinica.repositories.EspecialidadeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EspecialidadeService {

    private final EspecialidadeRepository especialidadeRepository;

    public EspecialidadeService(EspecialidadeRepository especialidadeRepository) {
        this.especialidadeRepository = especialidadeRepository;
    }

    public List<Especialidade> listarEspecialidades() {
        return especialidadeRepository.findAll();
    }

    public Especialidade buscarEspecialidadePorId(Integer id) {
        return especialidadeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Especialidade não encontrada com id " + id));
    }

    public Especialidade criarEspecialidade(Especialidade especialidade) {
        return especialidadeRepository.save(especialidade);
    }

    public Especialidade atualizarEspecialidade(Integer id, Especialidade especialidadeAtualizada) {
        Especialidade existente = buscarEspecialidadePorId(id);
        existente.setNome(especialidadeAtualizada.getNome());
        existente.setDescricao(especialidadeAtualizada.getDescricao());
        return especialidadeRepository.save(existente);
    }

    public void excluirEspecialidade(Integer id) {
        Especialidade existente = buscarEspecialidadePorId(id);
        especialidadeRepository.delete(existente);
    }

}
