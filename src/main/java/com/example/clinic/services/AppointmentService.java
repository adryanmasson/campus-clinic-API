package com.example.clinic.services;

import com.example.clinic.exceptions.AppointmentConflictException;
import com.example.clinic.exceptions.BusinessRuleException;
import com.example.clinic.dto.UpdateAppointmentDTO;
import com.example.clinic.dto.AppointmentDTO;
import com.example.clinic.models.Appointment;
import com.example.clinic.models.AppointmentStatus;
import com.example.clinic.models.Doctor;
import com.example.clinic.repositories.AppointmentDetailProjection;
import com.example.clinic.repositories.AppointmentRepository;
import com.example.clinic.repositories.DoctorRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

        private final AppointmentRepository appointmentRepository;
        private final DoctorRepository doctorRepository;

        public AppointmentService(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository) {
                this.appointmentRepository = appointmentRepository;
                this.doctorRepository = doctorRepository;
        }

        public List<AppointmentDTO> listAppointments() {
                return appointmentRepository.findAll().stream()
                                .map(c -> new AppointmentDTO(
                                                c.getAppointmentId(),
                                                c.getPatient().getName(),
                                                doctorRepository.findById(c.getDoctorId())
                                                                .map(Doctor::getName)
                                                                .orElse("Doctor not found"),
                                                c.getAppointmentDate(),
                                                c.getStartTime(),
                                                c.getEndTime(),
                                                c.getStatus().name()))
                                .toList();
        }

        public AppointmentDTO findAppointmentById(Integer id) {
                Appointment appointment = appointmentRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Appointment not found."));

                String patientName = appointment.getPatient().getName();
                String doctorName = doctorRepository.findById(appointment.getDoctorId())
                                .map(Doctor::getName)
                                .orElse("Doctor not found");

                return new AppointmentDTO(
                                appointment.getAppointmentId(),
                                patientName,
                                doctorName,
                                appointment.getAppointmentDate(),
                                appointment.getStartTime(),
                                appointment.getEndTime(),
                                appointment.getStatus().name());
        }

        @Transactional
        public AppointmentDTO scheduleAppointment(Integer patientId, Integer doctorId,
                        LocalDate appointmentDate, LocalTime startTime, LocalTime endTime) {

                try {
                        appointmentRepository.createAppointment(patientId, doctorId, appointmentDate, startTime,
                                        endTime);
                } catch (DataAccessException ex) {
                        Throwable cause = ex.getMostSpecificCause();
                        String message = cause != null ? cause.getMessage() : ex.getMessage();

                        throw new AppointmentConflictException(message);
                }

                Appointment appointment = appointmentRepository
                                .findInserted(patientId, doctorId, appointmentDate, startTime, endTime)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Failed to retrieve the created appointment."));

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
                                .orElseThrow(() -> new EntityNotFoundException("Appointment not found."));

                if (dto.getAppointmentDate() != null || dto.getStartTime() != null || dto.getEndTime() != null) {
                        LocalDate newDate = dto.getAppointmentDate() != null ? dto.getAppointmentDate()
                                        : appointment.getAppointmentDate();
                        LocalTime newStartTime = dto.getStartTime() != null ? dto.getStartTime()
                                        : appointment.getStartTime();
                        LocalTime newEndTime = dto.getEndTime() != null ? dto.getEndTime() : appointment.getEndTime();

                        boolean conflict = !appointmentRepository
                                        .findByDoctorDateAndTime(appointment.getDoctorId(), newDate, newStartTime,
                                                        newEndTime,
                                                        appointment.getAppointmentId())
                                        .isEmpty();

                        if (conflict) {
                                throw new AppointmentConflictException(
                                                "Doctor already has an appointment scheduled at this time.");
                        }

                        appointment.setAppointmentDate(newDate);
                        appointment.setStartTime(newStartTime);
                        appointment.setEndTime(newEndTime);
                }

                if (dto.getStatus() != null) {
                        try {
                                appointment.setStatus(AppointmentStatus.valueOf(dto.getStatus()));
                        } catch (IllegalArgumentException ex) {
                                throw new BusinessRuleException(
                                                "Invalid status. Valid values: SCHEDULED, COMPLETED, CANCELLED.");
                        }
                }

                appointmentRepository.save(appointment);

                AppointmentDetailProjection proj = appointmentRepository
                                .findDetailedAppointment(appointment.getAppointmentId());
                if (proj == null) {
                        throw new EntityNotFoundException("Failed to retrieve detailed appointment after update.");
                }

                java.sql.Date sqlDate = proj.getAppointmentDate();
                java.sql.Time sqlStartTime = proj.getStartTime();
                java.sql.Time sqlEndTime = proj.getEndTime();

                LocalDate appointmentDate = sqlDate != null ? sqlDate.toLocalDate() : null;
                LocalTime startTime = sqlStartTime != null ? sqlStartTime.toLocalTime() : null;
                LocalTime endTime = sqlEndTime != null ? sqlEndTime.toLocalTime() : null;

                return new AppointmentDTO(
                                proj.getId(),
                                proj.getPatientName(),
                                proj.getDoctorName(),
                                appointmentDate,
                                startTime,
                                endTime,
                                proj.getStatus());
        }

        @Transactional
        public AppointmentDTO cancelAppointment(Integer appointmentId) {
                Appointment appointment = appointmentRepository.findById(appointmentId)
                                .orElseThrow(() -> new EntityNotFoundException("Appointment not found."));

                if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                        throw new BusinessRuleException("Appointment is already cancelled.");
                }

                appointment.setStatus(AppointmentStatus.CANCELLED);
                appointmentRepository.save(appointment);

                AppointmentDetailProjection proj = appointmentRepository
                                .findDetailedAppointment(appointment.getAppointmentId());
                if (proj == null) {
                        throw new EntityNotFoundException(
                                        "Failed to retrieve detailed appointment after cancellation.");
                }

                java.sql.Date sqlDate = proj.getAppointmentDate();
                java.sql.Time sqlStartTime = proj.getStartTime();
                java.sql.Time sqlEndTime = proj.getEndTime();

                LocalDate appointmentDate = sqlDate != null ? sqlDate.toLocalDate() : null;
                LocalTime startTime = sqlStartTime != null ? sqlStartTime.toLocalTime() : null;
                LocalTime endTime = sqlEndTime != null ? sqlEndTime.toLocalTime() : null;

                return new AppointmentDTO(
                                proj.getId(),
                                proj.getPatientName(),
                                proj.getDoctorName(),
                                appointmentDate,
                                startTime,
                                endTime,
                                proj.getStatus());
        }

        @Transactional
        public List<AppointmentDTO> listAppointmentsByPatient(Integer patientId) {
                List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);

                if (appointments.isEmpty()) {
                        return Collections.emptyList();
                }

                return appointments.stream().map(c -> {
                        String patientName = null;
                        if (c.getPatient() != null) {
                                patientName = c.getPatient().getName();
                        }

                        String doctorName = null;
                        if (c.getDoctorId() != null) {
                                doctorName = doctorRepository.findById(c.getDoctorId())
                                                .map(Doctor::getName)
                                                .orElse(null);
                        }

                        return new AppointmentDTO(
                                        c.getAppointmentId(),
                                        patientName,
                                        doctorName,
                                        c.getAppointmentDate(),
                                        c.getStartTime(),
                                        c.getEndTime(),
                                        c.getStatus() != null ? c.getStatus().name() : null);
                }).toList();
        }

        @Transactional
        public List<AppointmentDTO> listAppointmentsByDoctor(Integer doctorId) {
                List<Map<String, Object>> appointments = appointmentRepository.findAppointmentsByDoctor(doctorId);

                return appointments.stream().map(c -> new AppointmentDTO(
                                (Integer) c.get("id"),
                                (String) c.get("patientName"),
                                (String) c.get("doctorName"),
                                ((java.sql.Date) c.get("appointmentDate")).toLocalDate(),
                                ((java.sql.Time) c.get("startTime")).toLocalTime(),
                                ((java.sql.Time) c.get("endTime")).toLocalTime(),
                                String.valueOf(c.get("status")))).toList();
        }

        @Transactional
        public List<AppointmentDTO> listAppointmentsByDate(LocalDate date) {
                List<Map<String, Object>> appointments = appointmentRepository.findAppointmentsByDate(date);

                return appointments.stream().map(c -> new AppointmentDTO(
                                (Integer) c.get("id"),
                                (String) c.get("patientName"),
                                (String) c.get("doctorName"),
                                ((java.sql.Date) c.get("appointmentDate")).toLocalDate(),
                                ((java.sql.Time) c.get("startTime")).toLocalTime(),
                                ((java.sql.Time) c.get("endTime")).toLocalTime(),
                                String.valueOf(c.get("status")))).toList();
        }

}
