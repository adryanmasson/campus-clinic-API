package com.example.clinica.dto;

import java.time.LocalDate;
import java.util.Map;

import com.example.clinica.models.MedicalRecord;

public class MedicalRecordDTO {

        private Integer recordId;
        private Integer appointmentId;
        private String patientName;
        private String doctorName;
        private String anamnesis;
        private String diagnosis;
        private String prescription;
        private LocalDate recordDate;

        public MedicalRecordDTO() {
        }

        public MedicalRecordDTO(Integer recordId, Integer appointmentId, String patientName, String doctorName,
                        String anamnesis, String diagnosis, String prescription, LocalDate recordDate) {
                this.recordId = recordId;
                this.appointmentId = appointmentId;
                this.patientName = patientName;
                this.doctorName = doctorName;
                this.anamnesis = anamnesis;
                this.diagnosis = diagnosis;
                this.prescription = prescription;
                this.recordDate = recordDate;
        }

        public Integer getRecordId() {
                return recordId;
        }

        public void setRecordId(Integer recordId) {
                this.recordId = recordId;
        }

        public Integer getAppointmentId() {
                return appointmentId;
        }

        public void setAppointmentId(Integer appointmentId) {
                this.appointmentId = appointmentId;
        }

        public String getPatientName() {
                return patientName;
        }

        public void setPatientName(String patientName) {
                this.patientName = patientName;
        }

        public String getDoctorName() {
                return doctorName;
        }

        public void setDoctorName(String doctorName) {
                this.doctorName = doctorName;
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

        public LocalDate getRecordDate() {
                return recordDate;
        }

        public void setRecordDate(LocalDate recordDate) {
                this.recordDate = recordDate;
        }

        public static MedicalRecordDTO fromMap(Map<String, Object> m) {
                if (m == null) {
                        return null;
                }

                Integer recordId = m.get("recordId") != null ? ((Number) m.get("recordId")).intValue()
                                : null;
                Integer appointmentId = m.get("appointmentId") != null ? ((Number) m.get("appointmentId")).intValue() : null;
                String patientName = (String) m.get("patientName");
                String doctorName = (String) m.get("doctorName");
                String anamnesis = (String) m.get("anamnesis");
                String diagnosis = (String) m.get("diagnosis");
                String prescription = (String) m.get("prescription");

                LocalDate recordDate = null;
                Object d = m.get("data_registro");
                if (d != null) {
                        if (d instanceof java.sql.Date) {
                                recordDate = ((java.sql.Date) d).toLocalDate();
                        } else if (d instanceof java.sql.Timestamp) {
                                recordDate = ((java.sql.Timestamp) d).toLocalDateTime().toLocalDate();
                        } else if (d instanceof java.time.LocalDate) {
                                recordDate = (java.time.LocalDate) d;
                        }
                }

                return new MedicalRecordDTO(recordId, appointmentId, patientName, doctorName, anamnesis, diagnosis,
                                prescription,
                                recordDate);
        }

        public static MedicalRecordDTO fromEntity(MedicalRecord prontuario) {
                if (prontuario == null)
                        return null;

                return new MedicalRecordDTO(
                                prontuario.getRecordId(),
                                prontuario.getAppointment() != null ? prontuario.getAppointment().getAppointmentId() : null,
                                prontuario.getAppointment() != null && prontuario.getAppointment().getPatient() != null
                                                ? prontuario.getAppointment().getPatient().getName()
                                                : null,
                                prontuario.getAppointment() != null && prontuario.getAppointment().getDoctor() != null
                                                ? prontuario.getAppointment().getDoctor().getName()
                                                : null,
                                prontuario.getAnamnesis(),
                                prontuario.getDiagnosis(),
                                prontuario.getPrescription(),
                                prontuario.getRecordDate());
        }
}
