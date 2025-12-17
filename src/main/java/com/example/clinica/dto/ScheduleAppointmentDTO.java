package com.example.clinica.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleAppointmentDTO {

    private Integer patientId;
    private Integer doctorId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;

    public ScheduleAppointmentDTO() {
    }

    public ScheduleAppointmentDTO(Integer patientId, Integer doctorId, LocalDate appointmentDate, LocalTime startTime,
            LocalTime endTime) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
