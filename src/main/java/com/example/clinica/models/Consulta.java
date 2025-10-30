package com.example.clinica.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_paciente")
    private Paciente paciente;

    @Column(name = "fk_id_medico")
    private Integer fkIdMedico;

    private LocalDate data_consulta;

    private LocalTime hora_inicio;

    private LocalTime hora_fim;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('AGENDADA','REALIZADA','CANCELADA')", nullable = false)
    private ConsultaStatus status = ConsultaStatus.AGENDADA;

    public Integer getId_consulta() {
        return id_consulta;
    }

    public void setId_consulta(Integer id_consulta) {
        this.id_consulta = id_consulta;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Integer getFkIdMedico() {
        return fkIdMedico;
    }

    public void setFkIdMedico(Integer fkIdMedico) {
        this.fkIdMedico = fkIdMedico;
    }

    public LocalDate getData_consulta() {
        return data_consulta;
    }

    public void setData_consulta(LocalDate data_consulta) {
        this.data_consulta = data_consulta;
    }

    public LocalTime getHora_inicio() {
        return hora_inicio;
    }

    public void setHora_inicio(LocalTime hora_inicio) {
        this.hora_inicio = hora_inicio;
    }

    public LocalTime getHora_fim() {
        return hora_fim;
    }

    public void setHora_fim(LocalTime hora_fim) {
        this.hora_fim = hora_fim;
    }

    public ConsultaStatus getStatus() {
        return status;
    }

    public void setStatus(ConsultaStatus status) {
        this.status = status;
    }
}
