package com.example.clinica.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.clinica.models.Appointment;

public class AppointmentDTO {
    private Integer id;
    private String nomePaciente;
    private String nomeMedico;
    private LocalDate dataConsulta;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String status;

    public AppointmentDTO(Integer id, String nomePaciente, String nomeMedico,
            LocalDate dataConsulta, LocalTime horaInicio,
            LocalTime horaFim, String status) {
        this.id = id;
        this.nomePaciente = nomePaciente;
        this.nomeMedico = nomeMedico;
        this.dataConsulta = dataConsulta;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.status = status;
    }

    public AppointmentDTO() {
    }

    public static AppointmentDTO fromEntity(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getAppointmentId());
        dto.setNomePaciente(appointment.getPatient().getName());
        dto.setNomeMedico(appointment.getDoctor().getName());
        dto.setDataConsulta(appointment.getAppointmentDate());
        dto.setHoraInicio(appointment.getStartTime());
        dto.setHoraFim(appointment.getEndTime());
        dto.setStatus(appointment.getStatus().name());
        return dto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public void setNomePaciente(String nomePaciente) {
        this.nomePaciente = nomePaciente;
    }

    public String getNomeMedico() {
        return nomeMedico;
    }

    public void setNomeMedico(String nomeMedico) {
        this.nomeMedico = nomeMedico;
    }

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
