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
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        if (doctorRepository.existsByCrm(doctor.getMedicalLicense())) {
            throw new RuntimeException("Medical license already registered for another doctor.");
        }

        Integer specialtyId = doctor.getSpecialty().getSpecialtyId();

        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new RuntimeException("Specialty not found."));

        doctor.setSpecialty(specialty);

        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor updateDoctor(Integer id, Doctor updatedDoctor) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!existingDoctor.getMedicalLicense().equals(updatedDoctor.getMedicalLicense()) &&
                doctorRepository.existsByCrm(updatedDoctor.getMedicalLicense())) {
            throw new RuntimeException("Medical license already registered for another doctor.");
        }

        if (updatedDoctor.getSpecialty() != null) {
            Specialty specialty = specialtyRepository.findById(
                    updatedDoctor.getSpecialty().getSpecialtyId())
                    .orElseThrow(() -> new RuntimeException("Specialty not found."));

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
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        boolean hasScheduledAppointments = appointmentRepository.existsByDoctorIdAndStatus(id,
                AppointmentStatus.SCHEDULED);

        if (hasScheduledAppointments) {
            throw new RuntimeException("Cannot delete doctor because he has scheduled appointments.");
        }

        doctorRepository.delete(doctor);
    }

    public List<Doctor> listBySpecialty(Integer specialtyId) {
        return doctorRepository.findBySpecialtyId(specialtyId);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> appointmentReportByDoctor(Integer doctorId) {
        List<Map<String, Object>> appointments = appointmentRepository.appointmentReportByDoctor(doctorId);

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
    public List<AppointmentDTO> upcomingAppointmentsReport(Integer doctorId) {
        List<Map<String, Object>> appointments = appointmentRepository.upcomingAppointmentsReport(doctorId);

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
