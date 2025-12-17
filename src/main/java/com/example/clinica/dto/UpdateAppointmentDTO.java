package com.example.clinica.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class UpdateAppointmentDTO {
    private LocalDate dataConsulta;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String status; // AGENDADA, REALIZADA, CANCELADA

    // getters e setters
    public LocalDate getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDate dataConsulta) {
        this.dataConsulta = dataConsulta;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
