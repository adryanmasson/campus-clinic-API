package com.example.clinica.services;

import com.example.clinica.models.Doctor;
import com.example.clinica.dto.AppointmentDTO;
import com.example.clinica.models.Appointment;
import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.models.Specialty;
import com.example.clinica.repositories.DoctorRepository;
import com.example.clinica.repositories.AppointmentRepository;
import com.example.clinica.repositories.SpecialtyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository medicoRepository;
    private final SpecialtyRepository especialidadeRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorService(DoctorRepository medicoRepository, SpecialtyRepository especialidadeRepository,
            AppointmentRepository appointmentRepository) {
        this.medicoRepository = medicoRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<Doctor> listDoctors() {
        return medicoRepository.findAll();
    }

    public Doctor findDoctorById(Integer id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));
    }

    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        if (medicoRepository.existsByCrm(doctor.getMedicalLicense())) {
            throw new RuntimeException("CRM já cadastrado para outro médico.");
        }

        Integer especialidadeId = doctor.getSpecialty().getSpecialtyId();

        Specialty specialty = especialidadeRepository.findById(especialidadeId)
                .orElseThrow(() -> new RuntimeException("Specialty não encontrada."));

        doctor.setSpecialty(specialty);

        return medicoRepository.save(doctor);
    }

    @Transactional
    public Doctor updateDoctor(Integer id, Doctor medicoAtualizado) {
        Doctor medicoExistente = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));

        if (!medicoExistente.getMedicalLicense().equals(medicoAtualizado.getMedicalLicense()) &&
                medicoRepository.existsByCrm(medicoAtualizado.getMedicalLicense())) {
            throw new RuntimeException("CRM já cadastrado para outro médico.");
        }

        if (medicoAtualizado.getSpecialty() != null) {
            Specialty specialty = especialidadeRepository.findById(
                    medicoAtualizado.getSpecialty().getSpecialtyId())
                    .orElseThrow(() -> new RuntimeException("Specialty não encontrada."));

            medicoExistente.setSpecialty(specialty);
        }

        medicoExistente.setName(medicoAtualizado.getName());
        medicoExistente.setMedicalLicense(medicoAtualizado.getMedicalLicense());
        medicoExistente.setBirthDate(medicoAtualizado.getBirthDate());
        medicoExistente.setPhone(medicoAtualizado.getPhone());
        medicoExistente.setActive(medicoAtualizado.getActive());

        return medicoRepository.save(medicoExistente);
    }

    @Transactional
    public void deleteDoctor(Integer id) {
        Doctor doctor = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));

        boolean temConsultasAgendadas = appointmentRepository.existsByFkIdMedicoAndStatus(id, AppointmentStatus.SCHEDULED);

        if (temConsultasAgendadas) {
            throw new RuntimeException("Não é possível excluir o médico, pois ele possui consultas agendadas.");
        }

        medicoRepository.delete(doctor);
    }

    public List<Doctor> listBySpecialty(Integer specialtyId) {
        return medicoRepository.findByEspecialidadeIdEspecialidade(specialtyId);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> appointmentReportByDoctor(Integer doctorId) {
        List<Map<String, Object>> consultas = appointmentRepository.appointmentReportByDoctor(doctorId);

        return consultas.stream().map(c -> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setId((Integer) c.get("appointmentId"));
            dto.setDataConsulta(((java.sql.Date) c.get("dataConsulta")).toLocalDate());
            dto.setHoraInicio(((java.sql.Time) c.get("horaInicio")).toLocalTime());
            dto.setHoraFim(((java.sql.Time) c.get("horaFim")).toLocalTime());
            dto.setStatus((String) c.get("status"));
            dto.setNomeMedico((String) c.get("nomeMedico"));
            dto.setNomePaciente((String) c.get("nomePaciente"));
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> upcomingAppointmentsReport(Integer doctorId) {
        List<Map<String, Object>> consultas = appointmentRepository.upcomingAppointmentsReport(doctorId);

        return consultas.stream().map(c -> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setId((Integer) c.get("appointmentId"));
            dto.setDataConsulta(((java.sql.Date) c.get("dataConsulta")).toLocalDate());
            dto.setHoraInicio(((java.sql.Time) c.get("horaInicio")).toLocalTime());
            dto.setHoraFim(((java.sql.Time) c.get("horaFim")).toLocalTime());
            dto.setStatus((String) c.get("status"));
            dto.setNomeMedico((String) c.get("nomeMedico"));
            dto.setNomePaciente((String) c.get("nomePaciente"));
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, LocalTime>> findAvailableTimeSlots(Integer doctorId, LocalDate data) {
        List<Appointment> consultas = appointmentRepository.findByMedicoIdAndDataConsulta(doctorId, data);

        LocalTime horaAtual = LocalTime.of(8, 0);
        LocalTime horaFinal = LocalTime.of(18, 0);

        List<Map<String, LocalTime>> horariosDisponiveis = new ArrayList<>();
        LocalTime blocoInicio = null;

        while (horaAtual.isBefore(horaFinal)) {
            LocalTime slotInicio = horaAtual;
            LocalTime slotFim = slotInicio.plusMinutes(30);

            boolean ocupado = consultas.stream()
                    .anyMatch(c -> slotInicio.isBefore(c.getEndTime()) && slotFim.isAfter(c.getStartTime()));

            if (!ocupado) {
                if (blocoInicio == null) {
                    blocoInicio = slotInicio;
                }
            } else {
                if (blocoInicio != null) {
                    horariosDisponiveis.add(Map.of(
                            "horaInicio", blocoInicio,
                            "horaFim", slotInicio));
                    blocoInicio = null;
                }
            }

            horaAtual = slotFim;
        }

        if (blocoInicio != null) {
            horariosDisponiveis.add(Map.of(
                    "horaInicio", blocoInicio,
                    "horaFim", horaFinal));
        }

        return horariosDisponiveis;
    }

}
