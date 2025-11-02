package com.example.clinica.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "prontuarios")
public class Prontuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prontuario")
    private Integer idProntuario;

    // Relacionamento com consulta (lazy)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_consulta")
    private Consulta consulta;

    @Column(columnDefinition = "text")
    private String anamnese;

    @Column(columnDefinition = "text")
    private String diagnostico;

    @Column(columnDefinition = "text")
    private String prescricao;

    @Column(name = "data_registro")
    private LocalDate dataRegistro;

    public Prontuario() {
    }

    public Integer getIdProntuario() {
        return idProntuario;
    }

    public void setIdProntuario(Integer idProntuario) {
        this.idProntuario = idProntuario;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public String getAnamnese() {
        return anamnese;
    }

    public void setAnamnese(String anamnese) {
        this.anamnese = anamnese;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getPrescricao() {
        return prescricao;
    }

    public void setPrescricao(String prescricao) {
        this.prescricao = prescricao;
    }

    public LocalDate getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDate dataRegistro) {
        this.dataRegistro = dataRegistro;
    }
}
