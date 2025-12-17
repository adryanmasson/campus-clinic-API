package com.example.clinic.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.clinic.exceptions.BusinessRuleException;
import com.example.clinic.exceptions.DuplicateResourceException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.clinic.dto.MedicalRecordDTO;
import com.example.clinic.dto.UpdateMedicalRecordDTO;
import com.example.clinic.dto.CreateMedicalRecordDTO;
import com.example.clinic.models.MedicalRecord;
import com.example.clinic.models.Appointment;
import com.example.clinic.repositories.MedicalRecordRepository;
import com.example.clinic.repositories.AppointmentRepository;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
            AppointmentRepository appointmentRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public List<MedicalRecordDTO> listMedicalRecords() {
        List<Map<String, Object>> medicalRecords = medicalRecordRepository.listMedicalRecords();
        return medicalRecords.stream().map(MedicalRecordDTO::fromMap).toList();
    }

    @Transactional
    public MedicalRecordDTO findByAppointment(Integer appointmentId) {
        Map<String, Object> m = medicalRecordRepository.findDetailedByAppointmentId(appointmentId);
        return MedicalRecordDTO.fromMap(m);
    }

    @Transactional
    public MedicalRecordDTO createMedicalRecord(CreateMedicalRecordDTO dto) {
        Integer appointmentId = dto.getAppointmentId();
        if (appointmentId == null) {
            throw new BusinessRuleException("appointmentId is required.");
        }

        MedicalRecord existing = medicalRecordRepository.findByAppointmentId(appointmentId);
        if (existing != null) {
            throw new DuplicateResourceException("Medical record already exists for this appointment.");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found."));

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setAppointment(appointment);
        medicalRecord.setAnamnesis(dto.getAnamnesis());
        medicalRecord.setDiagnosis(dto.getDiagnosis());
        medicalRecord.setPrescription(dto.getPrescription());
        medicalRecord.setRecordDate(LocalDate.now());

        medicalRecordRepository.save(medicalRecord);

        Map<String, Object> detailed = medicalRecordRepository.findDetailedByAppointmentId(appointmentId);
        return MedicalRecordDTO.fromMap(detailed);
    }

    @Transactional
    public MedicalRecordDTO updateMedicalRecord(Integer id, UpdateMedicalRecordDTO data) {
        Optional<MedicalRecord> optionalMedicalRecord = medicalRecordRepository.findById(id);

        if (optionalMedicalRecord.isEmpty()) {
            return null;
        }

        MedicalRecord medicalRecord = optionalMedicalRecord.get();

        if (data.getAnamnesis() != null)
            medicalRecord.setAnamnesis(data.getAnamnesis());
        if (data.getDiagnosis() != null)
            medicalRecord.setDiagnosis(data.getDiagnosis());
        if (data.getPrescription() != null)
            medicalRecord.setPrescription(data.getPrescription());

        medicalRecordRepository.save(medicalRecord);

        return MedicalRecordDTO.fromEntity(medicalRecord);
    }

    @Transactional
    public boolean deleteMedicalRecord(Integer recordId) {
        if (medicalRecordRepository.existsById(recordId)) {
            medicalRecordRepository.deleteById(recordId);
            return true;
        }
        return false;
    }

}
