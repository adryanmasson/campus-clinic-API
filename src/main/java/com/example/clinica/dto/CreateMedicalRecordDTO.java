package com.example.clinica.dto;

import java.io.Serializable;

public class CreateMedicalRecordDTO implements Serializable {

    private Integer appointmentId;
    private String anamnesis;
    private String diagnosis;
    private String prescription;

    public CreateMedicalRecordDTO() {
    }

    public CreateMedicalRecordDTO(Integer appointmentId, String anamnesis, String diagnosis, String prescription) {
        this.appointmentId = appointmentId;
        this.anamnesis = anamnesis;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
    }

    public Integer getIdConsulta() {
        return appointmentId;
    }

    public void setIdConsulta(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAnamnesis() {
        return anamnesis;
    }

    public void setAnamnesis(String anamnesis) {
        this.anamnesis = anamnesis;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }
}
