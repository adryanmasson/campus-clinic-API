package com.example.clinica.services;

import com.example.clinica.models.Medico;
import com.example.clinica.dto.ConsultaDTO;
import com.example.clinica.models.ConsultaStatus;
import com.example.clinica.models.Especialidade;
import com.example.clinica.repositories.MedicoRepository;
import com.example.clinica.repositories.ConsultaRepository;
import com.example.clinica.repositories.EspecialidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final ConsultaRepository consultaRepository;

    public MedicoService(MedicoRepository medicoRepository, EspecialidadeRepository especialidadeRepository,
            ConsultaRepository consultaRepository) {
        this.medicoRepository = medicoRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.consultaRepository = consultaRepository;
    }

    public List<Medico> listarMedicos() {
        return medicoRepository.findAll();
    }

    public Medico buscarMedicoPorId(Integer id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));
    }

    @Transactional
    public Medico criarMedico(Medico medico) {
        if (medicoRepository.existsByCrm(medico.getCrm())) {
            throw new RuntimeException("CRM já cadastrado para outro médico.");
        }

        Integer especialidadeId = medico.getEspecialidade().getId_especialidade();

        Especialidade especialidade = especialidadeRepository.findById(especialidadeId)
                .orElseThrow(() -> new RuntimeException("Especialidade não encontrada."));

        medico.setEspecialidade(especialidade);

        return medicoRepository.save(medico);
    }

    @Transactional
    public Medico atualizarMedico(Integer id, Medico medicoAtualizado) {
        Medico medicoExistente = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));

        if (!medicoExistente.getCrm().equals(medicoAtualizado.getCrm()) &&
                medicoRepository.existsByCrm(medicoAtualizado.getCrm())) {
            throw new RuntimeException("CRM já cadastrado para outro médico.");
        }

        if (medicoAtualizado.getEspecialidade() != null) {
            Especialidade especialidade = especialidadeRepository.findById(
                    medicoAtualizado.getEspecialidade().getId_especialidade())
                    .orElseThrow(() -> new RuntimeException("Especialidade não encontrada."));

            medicoExistente.setEspecialidade(especialidade);
        }

        medicoExistente.setNome(medicoAtualizado.getNome());
        medicoExistente.setCrm(medicoAtualizado.getCrm());
        medicoExistente.setData_nascimento(medicoAtualizado.getData_nascimento());
        medicoExistente.setTelefone(medicoAtualizado.getTelefone());
        medicoExistente.setAtivo(medicoAtualizado.getAtivo());

        return medicoRepository.save(medicoExistente);
    }

    @Transactional
    public void excluirMedico(Integer id) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));

        boolean temConsultasAgendadas = consultaRepository.existsByFkIdMedicoAndStatus(id, ConsultaStatus.AGENDADA);

        if (temConsultasAgendadas) {
            throw new RuntimeException("Não é possível excluir o médico, pois ele possui consultas agendadas.");
        }

        medicoRepository.delete(medico);
    }

    public List<Medico> listarPorEspecialidade(Integer idEspecialidade) {
        return medicoRepository.findByEspecialidadeIdEspecialidade(idEspecialidade);
    }

    @Transactional(readOnly = true)
    public List<ConsultaDTO> relatorioConsultasPorMedico(Integer idMedico) {
        List<Map<String, Object>> consultas = consultaRepository.relatorioConsultasPorMedico(idMedico);

        return consultas.stream().map(c -> {
            ConsultaDTO dto = new ConsultaDTO();
            dto.setId((Integer) c.get("idConsulta"));
            dto.setDataConsulta(((java.sql.Date) c.get("dataConsulta")).toLocalDate());
            dto.setHoraInicio(((java.sql.Time) c.get("horaInicio")).toLocalTime());
            dto.setHoraFim(((java.sql.Time) c.get("horaFim")).toLocalTime());
            dto.setStatus((String) c.get("status"));
            dto.setNomeMedico((String) c.get("nomeMedico"));
            dto.setNomePaciente((String) c.get("nomePaciente"));
            return dto;
        }).collect(Collectors.toList());
    }

}
