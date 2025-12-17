package com.example.clinica.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleAppointmentDTO {

    private Integer patientId;
    private Integer doctorId;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;

    public ScheduleAppointmentDTO() {
    }

    public ScheduleAppointmentDTO(Integer patientId, Integer doctorId, LocalDate data, LocalTime horaInicio,
            LocalTime horaFim) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public Integer getIdPaciente() {
        return patientId;
    }

    public void setIdPaciente(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getIdMedico() {
        return doctorId;
    }

    public void setIdMedico(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }
}
