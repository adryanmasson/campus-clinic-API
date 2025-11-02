package com.example.clinica.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.clinica.dto.ProntuarioDTO;
import com.example.clinica.models.Prontuario;
import com.example.clinica.repositories.ProntuarioRepository;

@Service
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;

    public ProntuarioService(ProntuarioRepository prontuarioRepository) {
        this.prontuarioRepository = prontuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ProntuarioDTO> listarProntuarios() {
        List<Map<String, Object>> prontuarios = prontuarioRepository.listarProntuarios();

        return prontuarios.stream()
                .map(ProntuarioDTO::fromMap)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProntuarioDTO buscarPorConsulta(Integer idConsulta) {
        Map<String, Object> m = prontuarioRepository.findDetalhadoByConsultaId(idConsulta);
        return ProntuarioDTO.fromMap(m);
    }
}
