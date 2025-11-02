package com.example.clinica.services;

import com.example.clinica.dto.AtualizarConsultaDTO;
import com.example.clinica.dto.ConsultaDTO;
import com.example.clinica.models.Consulta;
import com.example.clinica.models.ConsultaStatus;
import com.example.clinica.models.Medico;
import com.example.clinica.repositories.ConsultaDetalhadaProjection;
import com.example.clinica.repositories.ConsultaRepository;
import com.example.clinica.repositories.MedicoRepository;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultaService {

        private final ConsultaRepository consultaRepository;
        private final MedicoRepository medicoRepository;

        public ConsultaService(ConsultaRepository consultaRepository, MedicoRepository medicoRepository) {
                this.consultaRepository = consultaRepository;
                this.medicoRepository = medicoRepository;
        }

        public List<ConsultaDTO> listarConsultas() {
                return consultaRepository.findAll().stream()
                                .map(c -> new ConsultaDTO(
                                                c.getId_consulta(),
                                                c.getPaciente().getNome(),
                                                medicoRepository.findById(c.getFkIdMedico())
                                                                .map(Medico::getNome)
                                                                .orElse("Médico não encontrado"),
                                                c.getData_consulta(),
                                                c.getHora_inicio(),
                                                c.getHora_fim(),
                                                c.getStatus().name()))
                                .collect(Collectors.toList());
        }

        public ConsultaDTO buscarConsultaPorId(Integer id) {
                Consulta consulta = consultaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Consulta não encontrada."));

                String nomePaciente = consulta.getPaciente().getNome();
                String nomeMedico = medicoRepository.findById(consulta.getFkIdMedico())
                                .map(Medico::getNome)
                                .orElse("Médico não encontrado");

                return new ConsultaDTO(
                                consulta.getId_consulta(),
                                nomePaciente,
                                nomeMedico,
                                consulta.getData_consulta(),
                                consulta.getHora_inicio(),
                                consulta.getHora_fim(),
                                consulta.getStatus().name());
        }

        @Transactional
        public ConsultaDTO agendarConsulta(Integer idPaciente, Integer idMedico,
                        LocalDate data, LocalTime horaInicio, LocalTime horaFim) {

                try {
                        consultaRepository.criarConsulta(idPaciente, idMedico, data, horaInicio, horaFim);
                } catch (DataAccessException ex) {
                        Throwable cause = ex.getMostSpecificCause();
                        String mensagem = cause != null ? cause.getMessage() : ex.getMessage();

                        throw new RuntimeException(mensagem);
                }

                Consulta consulta = consultaRepository.findInserted(idPaciente, idMedico, data, horaInicio, horaFim)
                                .orElseThrow(() -> new RuntimeException("Falha ao recuperar a consulta criada."));

                return new ConsultaDTO(
                                consulta.getId(),
                                consulta.getPaciente().getNome(),
                                consulta.getMedico().getNome(),
                                consulta.getData_consulta(),
                                consulta.getHora_inicio(),
                                consulta.getHora_fim(),
                                consulta.getStatus().name());
        }

        @Transactional
        public ConsultaDTO atualizarConsulta(Integer idConsulta, AtualizarConsultaDTO dto) {
                Consulta consulta = consultaRepository.findById(idConsulta)
                                .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));

                if (dto.getDataConsulta() != null || dto.getHoraInicio() != null || dto.getHoraFim() != null) {
                        LocalDate novaData = dto.getDataConsulta() != null ? dto.getDataConsulta()
                                        : consulta.getData_consulta();
                        LocalTime novoInicio = dto.getHoraInicio() != null ? dto.getHoraInicio()
                                        : consulta.getHora_inicio();
                        LocalTime novoFim = dto.getHoraFim() != null ? dto.getHoraFim() : consulta.getHora_fim();

                        boolean conflito = !consultaRepository
                                        .findByMedicoDataHora(consulta.getFkIdMedico(), novaData, novoInicio, novoFim,
                                                        consulta.getId_consulta())
                                        .isEmpty();

                        if (conflito) {
                                throw new RuntimeException("Médico já possui consulta agendada neste horário.");
                        }

                        consulta.setData_consulta(novaData);
                        consulta.setHora_inicio(novoInicio);
                        consulta.setHora_fim(novoFim);
                }

                if (dto.getStatus() != null) {
                        try {
                                consulta.setStatus(ConsultaStatus.valueOf(dto.getStatus()));
                        } catch (IllegalArgumentException ex) {
                                throw new RuntimeException(
                                                "Status inválido. Valores válidos: AGENDADA, REALIZADA, CANCELADA.");
                        }
                }

                consultaRepository.save(consulta);

                ConsultaDetalhadaProjection proj = consultaRepository
                                .buscarConsultaDetalhada(consulta.getId_consulta());
                if (proj == null) {
                        throw new RuntimeException("Falha ao recuperar consulta detalhada após atualização.");
                }

                java.sql.Date sqlDate = proj.getData_consulta();
                java.sql.Time sqlHi = proj.getHora_inicio();
                java.sql.Time sqlHf = proj.getHora_fim();

                LocalDate dataConsulta = sqlDate != null ? sqlDate.toLocalDate() : null;
                LocalTime horaInicio = sqlHi != null ? sqlHi.toLocalTime() : null;
                LocalTime horaFim = sqlHf != null ? sqlHf.toLocalTime() : null;

                return new ConsultaDTO(
                                proj.getId(),
                                proj.getNome_paciente(),
                                proj.getNome_medico(),
                                dataConsulta,
                                horaInicio,
                                horaFim,
                                proj.getStatus());
        }

        @Transactional
        public ConsultaDTO cancelarConsulta(Integer idConsulta) {
                Consulta consulta = consultaRepository.findById(idConsulta)
                                .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));

                if (consulta.getStatus() == ConsultaStatus.CANCELADA) {
                        throw new RuntimeException("A consulta já está cancelada.");
                }

                consulta.setStatus(ConsultaStatus.CANCELADA);
                consultaRepository.save(consulta);

                ConsultaDetalhadaProjection proj = consultaRepository
                                .buscarConsultaDetalhada(consulta.getId_consulta());
                if (proj == null) {
                        throw new RuntimeException("Falha ao recuperar consulta detalhada após cancelamento.");
                }

                java.sql.Date sqlDate = proj.getData_consulta();
                java.sql.Time sqlHi = proj.getHora_inicio();
                java.sql.Time sqlHf = proj.getHora_fim();

                LocalDate dataConsulta = sqlDate != null ? sqlDate.toLocalDate() : null;
                LocalTime horaInicio = sqlHi != null ? sqlHi.toLocalTime() : null;
                LocalTime horaFim = sqlHf != null ? sqlHf.toLocalTime() : null;

                return new ConsultaDTO(
                                proj.getId(),
                                proj.getNome_paciente(),
                                proj.getNome_medico(),
                                dataConsulta,
                                horaInicio,
                                horaFim,
                                proj.getStatus());
        }

}
