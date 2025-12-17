package com.example.clinica.services;

import com.example.clinica.dto.AppointmentDTO;
import com.example.clinica.dto.PatientHistoryDTO;
import com.example.clinica.models.Appointment;
import com.example.clinica.models.Patient;
import com.example.clinica.repositories.PatientRepository;
import com.example.clinica.repositories.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<Patient> listPatients() {
        return patientRepository.findAll();
    }

    public Patient findPatientById(Integer id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient não encontrado com ID: " + id));
    }

    public Patient createPatient(Patient patient) {
        Optional<Patient> pacienteExistente = patientRepository.findByCpf(patient.getCpf());
        if (pacienteExistente.isPresent()) {
            throw new RuntimeException("Já existe patient com esse CPF: " + patient.getCpf());
        }
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Integer id, Patient updatedData) {
        Patient patient = findPatientById(id);

        if (updatedData.getPhone() != null) {
            patient.setPhone(updatedData.getPhone());
        }
        if (updatedData.getEmail() != null) {
            patient.setEmail(updatedData.getEmail());
        }
        if (updatedData.getAddress() != null) {
            patient.setAddress(updatedData.getAddress());
        }

        return patientRepository.save(patient);
    }

    public void deletePatient(Integer id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient não encontrado."));

        List<Appointment> consultas = appointmentRepository.findByPacienteId(id);
        if (!consultas.isEmpty()) {
            throw new RuntimeException("Patient possui consultas associadas e não pode ser excluído.");
        }

        patientRepository.delete(patient);
    }

    public Integer calculatePatientAge(Integer patientId) {
        return patientRepository.calcularIdade(patientId);
    }

    @Transactional
    public List<PatientHistoryDTO> listPatientHistory(Integer patientId) {
        List<Map<String, Object>> historico = patientRepository.listPatientHistory(patientId);

        return historico.stream().map(h -> {
            PatientHistoryDTO dto = new PatientHistoryDTO();
            dto.setIdConsulta((Integer) h.get("appointmentId"));
            dto.setDataConsulta(((java.sql.Date) h.get("dataConsulta")).toLocalDate());
            dto.setHoraInicio(((java.sql.Time) h.get("horaInicio")).toLocalTime());
            dto.setHoraFim(((java.sql.Time) h.get("horaFim")).toLocalTime());
            dto.setStatusConsulta((String) h.get("status"));
            dto.setRecordId((Integer) h.get("recordId"));
            dto.setAnamnesis((String) h.get("anamnesis"));
            dto.setDiagnosis((String) h.get("diagnosis"));
            dto.setPrescription((String) h.get("prescription"));
            dto.setNomeMedico((String) h.get("nomeMedico"));
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> appointmentsReportLastMonths(Integer patientId, Integer meses) {
        List<Map<String, Object>> consultas = appointmentRepository.appointmentsReportLastMonths(patientId, meses);

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
    public List<Map<String, Object>> countPatientsBySpecialty() {
        return patientRepository.countPatientsBySpecialty();
    }

}
