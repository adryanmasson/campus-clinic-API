package com.example.clinica.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.clinica.dto.MedicalRecordDTO;
import com.example.clinica.dto.UpdateMedicalRecordDTO;
import com.example.clinica.dto.CreateMedicalRecordDTO;
import com.example.clinica.models.MedicalRecord;
import com.example.clinica.models.Appointment;
import com.example.clinica.repositories.MedicalRecordRepository;
import com.example.clinica.repositories.AppointmentRepository;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository, AppointmentRepository appointmentRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public List<MedicalRecordDTO> listMedicalRecords() {
        List<Map<String, Object>> prontuarios = medicalRecordRepository.listMedicalRecords();
        return prontuarios.stream().map(MedicalRecordDTO::fromMap).collect(Collectors.toList());
    }

    @Transactional
    public MedicalRecordDTO findByAppointment(Integer appointmentId) {
        Map<String, Object> m = medicalRecordRepository.findDetalhadoByConsultaId(appointmentId);
        return MedicalRecordDTO.fromMap(m);
    }

    @Transactional
    public MedicalRecordDTO createMedicalRecord(CreateMedicalRecordDTO dto) {
        Integer appointmentId = dto.getAppointmentId();
        if (appointmentId == null) {
            throw new RuntimeException("appointmentId é obrigatório.");
        }

        MedicalRecord existente = medicalRecordRepository.findByConsultaId(appointmentId);
        if (existente != null) {
            throw new RuntimeException("Já existe prontuário para essa appointment.");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment não encontrada."));

        MedicalRecord prontuario = new MedicalRecord();
        prontuario.setAppointment(appointment);
        prontuario.setAnamnesis(dto.getAnamnesis());
        prontuario.setDiagnosis(dto.getDiagnosis());
        prontuario.setPrescription(dto.getPrescription());
        prontuario.setRecordDate(LocalDate.now());

        medicalRecordRepository.save(prontuario);

        Map<String, Object> detalhado = medicalRecordRepository.findDetalhadoByConsultaId(appointmentId);
        return MedicalRecordDTO.fromMap(detalhado);
    }

    @Transactional
    public MedicalRecordDTO updateMedicalRecord(Integer id, UpdateMedicalRecordDTO dados) {
        Optional<MedicalRecord> optionalProntuario = medicalRecordRepository.findById(id);

        if (optionalProntuario.isEmpty()) {
            return null;
        }

        MedicalRecord prontuario = optionalProntuario.get();

        if (dados.getAnamnesis() != null)
            prontuario.setAnamnesis(dados.getAnamnesis());
        if (dados.getDiagnosis() != null)
            prontuario.setDiagnosis(dados.getDiagnosis());
        if (dados.getPrescription() != null)
            prontuario.setPrescription(dados.getPrescription());

        medicalRecordRepository.save(prontuario);

        return MedicalRecordDTO.fromEntity(prontuario);
    }

    @Transactional
    public boolean excluirProntuario(Integer recordId) {
        if (medicalRecordRepository.existsById(recordId)) {
            medicalRecordRepository.deleteById(recordId);
            return true;
        }
        return false;
    }

}
