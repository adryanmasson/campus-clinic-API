package com.example.clinica.services;

import com.example.clinica.dto.UpdateAppointmentDTO;
import com.example.clinica.dto.AppointmentDTO;
import com.example.clinica.models.Appointment;
import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.models.Doctor;
import com.example.clinica.repositories.AppointmentDetailProjection;
import com.example.clinica.repositories.AppointmentRepository;
import com.example.clinica.repositories.DoctorRepository;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

        private final AppointmentRepository appointmentRepository;
        private final DoctorRepository medicoRepository;

        public AppointmentService(AppointmentRepository appointmentRepository, DoctorRepository medicoRepository) {
                this.appointmentRepository = appointmentRepository;
                this.medicoRepository = medicoRepository;
        }

        public List<AppointmentDTO> listAppointments() {
                return appointmentRepository.findAll().stream()
                                .map(c -> new AppointmentDTO(
                                                c.getAppointmentId(),
                                                c.getPatient().getName(),
                                                medicoRepository.findById(c.getDoctorId())
                                                                .map(Doctor::getName)
                                                                .orElse("Médico não encontrado"),
                                                c.getAppointmentDate(),
                                                c.getStartTime(),
                                                c.getEndTime(),
                                                c.getStatus().name()))
                                .collect(Collectors.toList());
        }

        public AppointmentDTO buscarConsultaPorId(Integer id) {
                Appointment appointment = appointmentRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Appointment não encontrada."));

                String nomePaciente = appointment.getPatient().getName();
                String nomeMedico = medicoRepository.findById(appointment.getDoctorId())
                                .map(Doctor::getName)
                                .orElse("Médico não encontrado");

                return new AppointmentDTO(
                                appointment.getAppointmentId(),
                                nomePaciente,
                                nomeMedico,
                                appointment.getAppointmentDate(),
                                appointment.getStartTime(),
                                appointment.getEndTime(),
                                appointment.getStatus().name());
        }

        @Transactional
        public AppointmentDTO scheduleAppointment(Integer patientId, Integer doctorId,
                        LocalDate data, LocalTime horaInicio, LocalTime horaFim) {

                try {
                        appointmentRepository.criarConsulta(patientId, doctorId, data, horaInicio, horaFim);
                } catch (DataAccessException ex) {
                        Throwable cause = ex.getMostSpecificCause();
                        String mensagem = cause != null ? cause.getMessage() : ex.getMessage();

                        throw new RuntimeException(mensagem);
                }

                Appointment appointment = appointmentRepository.findInserted(patientId, doctorId, data, horaInicio, horaFim)
                                .orElseThrow(() -> new RuntimeException("Falha ao recuperar a appointment criada."));

                return new AppointmentDTO(
                                appointment.getId(),
                                appointment.getPatient().getName(),
                                appointment.getDoctor().getName(),
                                appointment.getAppointmentDate(),
                                appointment.getStartTime(),
                                appointment.getEndTime(),
                                appointment.getStatus().name());
        }

        @Transactional
        public AppointmentDTO updateAppointment(Integer appointmentId, UpdateAppointmentDTO dto) {
                Appointment appointment = appointmentRepository.findById(appointmentId)
                                .orElseThrow(() -> new RuntimeException("Appointment não encontrada"));

                if (dto.getDataConsulta() != null || dto.getHoraInicio() != null || dto.getHoraFim() != null) {
                        LocalDate novaData = dto.getDataConsulta() != null ? dto.getDataConsulta()
                                        : appointment.getAppointmentDate();
                        LocalTime novoInicio = dto.getHoraInicio() != null ? dto.getHoraInicio()
                                        : appointment.getStartTime();
                        LocalTime novoFim = dto.getHoraFim() != null ? dto.getHoraFim() : appointment.getEndTime();

                        boolean conflito = !appointmentRepository
                                        .findByMedicoDataHora(appointment.getDoctorId(), novaData, novoInicio, novoFim,
                                                        appointment.getAppointmentId())
                                        .isEmpty();

                        if (conflito) {
                                throw new RuntimeException("Médico já possui appointment agendada neste horário.");
                        }

                        appointment.setAppointmentDate(novaData);
                        appointment.setStartTime(novoInicio);
                        appointment.setEndTime(novoFim);
                }

                if (dto.getStatus() != null) {
                        try {
                                appointment.setStatus(AppointmentStatus.valueOf(dto.getStatus()));
                        } catch (IllegalArgumentException ex) {
                                throw new RuntimeException(
                                                "Status inválido. Valores válidos: AGENDADA, REALIZADA, CANCELADA.");
                        }
                }

                appointmentRepository.save(appointment);

                AppointmentDetailProjection proj = appointmentRepository
                                .buscarConsultaDetalhada(appointment.getAppointmentId());
                if (proj == null) {
                        throw new RuntimeException("Falha ao recuperar appointment detalhada após atualização.");
                }

                java.sql.Date sqlDate = proj.getAppointmentDate();
                java.sql.Time sqlHi = proj.getStartTime();
                java.sql.Time sqlHf = proj.getEndTime();

                LocalDate dataConsulta = sqlDate != null ? sqlDate.toLocalDate() : null;
                LocalTime horaInicio = sqlHi != null ? sqlHi.toLocalTime() : null;
                LocalTime horaFim = sqlHf != null ? sqlHf.toLocalTime() : null;

                return new AppointmentDTO(
                                proj.getId(),
                                proj.getNome_paciente(),
                                proj.getNome_medico(),
                                dataConsulta,
                                horaInicio,
                                horaFim,
                                proj.getStatus());
        }

        @Transactional
        public AppointmentDTO cancelAppointment(Integer appointmentId) {
                Appointment appointment = appointmentRepository.findById(appointmentId)
                                .orElseThrow(() -> new RuntimeException("Appointment não encontrada"));

                if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                        throw new RuntimeException("A appointment já está cancelada.");
                }

                appointment.setStatus(AppointmentStatus.CANCELLED);
                appointmentRepository.save(appointment);

                AppointmentDetailProjection proj = appointmentRepository
                                .buscarConsultaDetalhada(appointment.getAppointmentId());
                if (proj == null) {
                        throw new RuntimeException("Falha ao recuperar appointment detalhada após cancelamento.");
                }

                java.sql.Date sqlDate = proj.getAppointmentDate();
                java.sql.Time sqlHi = proj.getStartTime();
                java.sql.Time sqlHf = proj.getEndTime();

                LocalDate dataConsulta = sqlDate != null ? sqlDate.toLocalDate() : null;
                LocalTime horaInicio = sqlHi != null ? sqlHi.toLocalTime() : null;
                LocalTime horaFim = sqlHf != null ? sqlHf.toLocalTime() : null;

                return new AppointmentDTO(
                                proj.getId(),
                                proj.getNome_paciente(),
                                proj.getNome_medico(),
                                dataConsulta,
                                horaInicio,
                                horaFim,
                                proj.getStatus());
        }

        @Transactional
        public List<AppointmentDTO> listarConsultasPorPaciente(Integer patientId) {
                List<Appointment> consultas = appointmentRepository.findByPacienteId(patientId);

                if (consultas.isEmpty()) {
                        return Collections.emptyList();
                }

                return consultas.stream().map(c -> {
                        String nomePaciente = null;
                        if (c.getPatient() != null) {
                                nomePaciente = c.getPatient().getName();
                        }

                        String nomeMedico = null;
                        if (c.getDoctorId() != null) {
                                nomeMedico = medicoRepository.findById(c.getDoctorId())
                                                .map(Doctor::getName)
                                                .orElse(null);
                        }

                        return new AppointmentDTO(
                                        c.getAppointmentId(),
                                        nomePaciente,
                                        nomeMedico,
                                        c.getAppointmentDate(),
                                        c.getStartTime(),
                                        c.getEndTime(),
                                        c.getStatus() != null ? c.getStatus().name() : null);
                }).collect(Collectors.toList());
        }

        @Transactional
        public List<AppointmentDTO> listarConsultasPorMedico(Integer doctorId) {
                List<Map<String, Object>> consultas = appointmentRepository.buscarConsultasPorMedico(doctorId);

                return consultas.stream().map(c -> new AppointmentDTO(
                                (Integer) c.get("id"),
                                (String) c.get("nome_paciente"),
                                (String) c.get("nome_medico"),
                                ((java.sql.Date) c.get("appointmentDate")).toLocalDate(),
                                ((java.sql.Time) c.get("startTime")).toLocalTime(),
                                ((java.sql.Time) c.get("endTime")).toLocalTime(),
                                String.valueOf(c.get("status")))).toList();
        }

        @Transactional
        public List<AppointmentDTO> listarConsultasPorData(LocalDate data) {
                List<Map<String, Object>> consultas = appointmentRepository.buscarConsultasPorData(data);

                return consultas.stream().map(c -> new AppointmentDTO(
                                (Integer) c.get("id"),
                                (String) c.get("nome_paciente"),
                                (String) c.get("nome_medico"),
                                ((java.sql.Date) c.get("appointmentDate")).toLocalDate(),
                                ((java.sql.Time) c.get("startTime")).toLocalTime(),
                                ((java.sql.Time) c.get("endTime")).toLocalTime(),
                                String.valueOf(c.get("status")))).toList();
        }

}
