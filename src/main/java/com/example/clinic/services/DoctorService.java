package com.example.clinic.services;

import com.example.clinic.exceptions.BusinessRuleException;
import com.example.clinic.exceptions.DuplicateResourceException;
import com.example.clinic.models.Doctor;
import com.example.clinic.dto.AppointmentDTO;
import com.example.clinic.models.Appointment;
import com.example.clinic.models.AppointmentStatus;
import com.example.clinic.models.Specialty;
import com.example.clinic.repositories.DoctorRepository;
import com.example.clinic.repositories.AppointmentRepository;
import com.example.clinic.repositories.SpecialtyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DoctorService {

    private static final String DOCTOR_NOT_FOUND = "Doctor not found";
    private static final String SPECIALTY_NOT_FOUND = "Specialty not found.";
    private static final String START_TIME_KEY = "startTime";
    private static final String END_TIME_KEY = "endTime";
    private static final String APPOINTMENT_DATE_KEY = "appointmentDate";
    private static final String STATUS_KEY = "status";
    private static final String DOCTOR_NAME_KEY = "doctorName";
    private static final String PATIENT_NAME_KEY = "patientName";
    private static final String APPOINTMENT_ID_KEY = "appointmentId";

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorService(DoctorRepository doctorRepository, SpecialtyRepository specialtyRepository,
            AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<Doctor> listDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor findDoctorById(Integer id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(DOCTOR_NOT_FOUND));
    }

    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        if (doctorRepository.existsByMedicalLicense(doctor.getMedicalLicense())) {
            throw new DuplicateResourceException("Medical license already registered for another doctor.");
        }

        Integer specialtyId = doctor.getSpecialty().getSpecialtyId();

        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new BusinessRuleException(SPECIALTY_NOT_FOUND));

        doctor.setSpecialty(specialty);

        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor updateDoctor(Integer id, Doctor updatedDoctor) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(DOCTOR_NOT_FOUND));

        if (!existingDoctor.getMedicalLicense().equals(updatedDoctor.getMedicalLicense()) &&
                doctorRepository.existsByMedicalLicense(updatedDoctor.getMedicalLicense())) {
            throw new DuplicateResourceException("Medical license already registered for another doctor.");
        }

        if (updatedDoctor.getSpecialty() != null) {
            Specialty specialty = specialtyRepository.findById(
                    updatedDoctor.getSpecialty().getSpecialtyId())
                    .orElseThrow(() -> new BusinessRuleException(SPECIALTY_NOT_FOUND));

            existingDoctor.setSpecialty(specialty);
        }

        existingDoctor.setName(updatedDoctor.getName());
        existingDoctor.setMedicalLicense(updatedDoctor.getMedicalLicense());
        existingDoctor.setBirthDate(updatedDoctor.getBirthDate());
        existingDoctor.setPhone(updatedDoctor.getPhone());
        existingDoctor.setActive(updatedDoctor.getActive());

        return doctorRepository.save(existingDoctor);
    }

    @Transactional
    public void deleteDoctor(Integer id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(DOCTOR_NOT_FOUND));

        boolean hasScheduledAppointments = appointmentRepository.existsByDoctorIdAndStatus(id,
                AppointmentStatus.SCHEDULED);

        if (hasScheduledAppointments) {
            throw new BusinessRuleException("Cannot delete doctor because he has scheduled appointments.");
        }

        doctorRepository.delete(doctor);
    }

    public List<Doctor> listBySpecialty(Integer specialtyId) {
        return doctorRepository.findBySpecialty_SpecialtyId(specialtyId);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> appointmentReportByDoctor(Integer doctorId) {
        List<Map<String, Object>> appointments = appointmentRepository.appointmentReportByDoctor(doctorId);

        return appointments.stream().map(c -> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setId((Integer) c.get(APPOINTMENT_ID_KEY));
            dto.setAppointmentDate(((java.sql.Date) c.get(APPOINTMENT_DATE_KEY)).toLocalDate());
            dto.setStartTime(((java.sql.Time) c.get(START_TIME_KEY)).toLocalTime());
            dto.setEndTime(((java.sql.Time) c.get(END_TIME_KEY)).toLocalTime());
            dto.setStatus((String) c.get(STATUS_KEY));
            dto.setDoctorName((String) c.get(DOCTOR_NAME_KEY));
            dto.setPatientName((String) c.get(PATIENT_NAME_KEY));
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> upcomingAppointmentsReport(Integer doctorId) {
        List<Map<String, Object>> appointments = appointmentRepository.upcomingAppointmentsReport(doctorId);

        return appointments.stream().map(c -> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setId((Integer) c.get(APPOINTMENT_ID_KEY));
            dto.setAppointmentDate(((java.sql.Date) c.get(APPOINTMENT_DATE_KEY)).toLocalDate());
            dto.setStartTime(((java.sql.Time) c.get(START_TIME_KEY)).toLocalTime());
            dto.setEndTime(((java.sql.Time) c.get(END_TIME_KEY)).toLocalTime());
            dto.setStatus((String) c.get(STATUS_KEY));
            dto.setDoctorName((String) c.get(DOCTOR_NAME_KEY));
            dto.setPatientName((String) c.get(PATIENT_NAME_KEY));
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, LocalTime>> findAvailableTimeSlots(Integer doctorId, LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);

        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(18, 0);

        List<Map<String, LocalTime>> availableSlots = new ArrayList<>();
        LocalTime blockStart = null;

        while (startTime.isBefore(endTime)) {
            LocalTime slotStart = startTime;
            LocalTime slotEnd = slotStart.plusMinutes(30);

            boolean booked = appointments.stream()
                    .anyMatch(c -> slotStart.isBefore(c.getEndTime()) && slotEnd.isAfter(c.getStartTime()));

            if (!booked) {
                if (blockStart == null) {
                    blockStart = slotStart;
                }
            } else {
                if (blockStart != null) {
                    availableSlots.add(Map.of(
                            "startTime", blockStart,
                            "endTime", slotStart));
                    blockStart = null;
                }
            }

            startTime = slotEnd;
        }

        if (blockStart != null) {
            availableSlots.add(Map.of(
                    "startTime", blockStart,
                    "endTime", endTime));
        }

        return availableSlots;
    }

}
