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
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + id));
    }

    public Patient createPatient(Patient patient) {
        Optional<Patient> existingPatient = patientRepository.findByCpf(patient.getCpf());
        if (existingPatient.isPresent()) {
            throw new RuntimeException("Patient already exists with this CPF: " + patient.getCpf());
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
                .orElseThrow(() -> new RuntimeException("Patient not found."));

        List<Appointment> appointments = appointmentRepository.findByPatientId(id);
        if (!appointments.isEmpty()) {
            throw new RuntimeException("Patient has associated appointments and cannot be deleted.");
        }

        patientRepository.delete(patient);
    }

    public Integer calculatePatientAge(Integer patientId) {
        return patientRepository.calculateAge(patientId);
    }

    @Transactional
    public List<PatientHistoryDTO> listPatientHistory(Integer patientId) {
        List<Map<String, Object>> history = patientRepository.listPatientHistory(patientId);

        return history.stream().map(h -> {
            PatientHistoryDTO dto = new PatientHistoryDTO();
            dto.setAppointmentId((Integer) h.get("appointmentId"));
            dto.setAppointmentDate(((java.sql.Date) h.get("appointmentDate")).toLocalDate());
            dto.setStartTime(((java.sql.Time) h.get("startTime")).toLocalTime());
            dto.setEndTime(((java.sql.Time) h.get("endTime")).toLocalTime());
            dto.setAppointmentStatus((String) h.get("status"));
            dto.setRecordId((Integer) h.get("recordId"));
            dto.setAnamnesis((String) h.get("anamnesis"));
            dto.setDiagnosis((String) h.get("diagnosis"));
            dto.setPrescription((String) h.get("prescription"));
            dto.setDoctorName((String) h.get("doctorName"));
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> appointmentsReportLastMonths(Integer patientId, Integer months) {
        List<Map<String, Object>> appointments = appointmentRepository.appointmentsReportLastMonths(patientId, months);

        return appointments.stream().map(c -> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setId((Integer) c.get("appointmentId"));
            dto.setAppointmentDate(((java.sql.Date) c.get("appointmentDate")).toLocalDate());
            dto.setStartTime(((java.sql.Time) c.get("startTime")).toLocalTime());
            dto.setEndTime(((java.sql.Time) c.get("endTime")).toLocalTime());
            dto.setStatus((String) c.get("status"));
            dto.setDoctorName((String) c.get("doctorName"));
            dto.setPatientName((String) c.get("patientName"));
            return dto;
        }).collect(Collectors.toList());

    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> countPatientsBySpecialty() {
        return patientRepository.countPatientsBySpecialty();
    }

}
