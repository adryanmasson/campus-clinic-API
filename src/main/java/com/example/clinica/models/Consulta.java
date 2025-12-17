package com.example.clinica.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@NamedStoredProcedureQuery(name = "Consulta.criar_consulta", procedureName = "create_appointment", parameters = {
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_patient_id", type = Integer.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_doctor_id", type = Integer.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_appointment_date", type = java.time.LocalDate.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_start_time", type = java.time.LocalTime.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_end_time", type = java.time.LocalTime.class)
})

@Entity
@Table(name = "appointments")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Integer id_consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Paciente paciente;

    @Column(name = "doctor_id")
    private Integer fkIdMedico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private Medico medico;

    @Column(name = "appointment_date")
    private LocalDate data_consulta;

    @Column(name = "start_time")
    private LocalTime hora_inicio;

    @Column(name = "end_time")
    private LocalTime hora_fim;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
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

    public Integer getId() {
        return this.id_consulta;
    }

    public void setId(Integer id) {
        this.id_consulta = id;
    }

    public Medico getMedico() {
        return this.medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
        if (medico != null) {
            try {
                Integer mid = null;
                mid = (Integer) medico.getClass().getMethod("getId_medico").invoke(medico);
                if (mid != null)
                    this.fkIdMedico = mid;
            } catch (Exception ignored) {
            }
        }
    }
}
