package com.example.clinica.dto;

import java.time.LocalDate;
import java.util.Map;

public class ProntuarioDTO {

        private Integer idProntuario;
        private Integer idConsulta;
        private String nomePaciente;
        private String nomeMedico;
        private String anamnese;
        private String diagnostico;
        private String prescricao;
        private LocalDate dataRegistro;

        public ProntuarioDTO() {
        }

        public ProntuarioDTO(Integer idProntuario, Integer idConsulta, String nomePaciente, String nomeMedico,
                        String anamnese, String diagnostico, String prescricao, LocalDate dataRegistro) {
                this.idProntuario = idProntuario;
                this.idConsulta = idConsulta;
                this.nomePaciente = nomePaciente;
                this.nomeMedico = nomeMedico;
                this.anamnese = anamnese;
                this.diagnostico = diagnostico;
                this.prescricao = prescricao;
                this.dataRegistro = dataRegistro;
        }

        // getters / setters
        public Integer getIdProntuario() {
                return idProntuario;
        }

        public void setIdProntuario(Integer idProntuario) {
                this.idProntuario = idProntuario;
        }

        public Integer getIdConsulta() {
                return idConsulta;
        }

        public void setIdConsulta(Integer idConsulta) {
                this.idConsulta = idConsulta;
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

        /**
         * Helper para criar DTO a partir do Map que vem da query nativa.
         * Trata tipos de data (java.sql.Date / java.sql.Timestamp).
         */
        public static ProntuarioDTO fromMap(Map<String, Object> m) {
                if (m == null) {
                        return null;
                }

                Integer idProntuario = (m.get("idProntuario") != null) ? ((Number) m.get("idProntuario")).intValue()
                                : null;
                Integer idConsulta = (m.get("idConsulta") != null) ? ((Number) m.get("idConsulta")).intValue() : null;
                String nomePaciente = (String) m.get("nomePaciente");
                String nomeMedico = (String) m.get("nomeMedico");
                String anamnese = (String) m.get("anamnese");
                String diagnostico = (String) m.get("diagnostico");
                String prescricao = (String) m.get("prescricao");

                LocalDate dataRegistro = null;
                Object dateObj = m.get("data_registro");
                if (dateObj != null) {
                        if (dateObj instanceof java.sql.Date) {
                                dataRegistro = ((java.sql.Date) dateObj).toLocalDate();
                        } else if (dateObj instanceof java.sql.Timestamp) {
                                dataRegistro = ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate();
                        } else if (dateObj instanceof java.time.LocalDate) {
                                dataRegistro = (java.time.LocalDate) dateObj;
                        }
                }

                return new ProntuarioDTO(idProntuario, idConsulta, nomePaciente, nomeMedico, anamnese, diagnostico,
                                prescricao,
                                dataRegistro);
        }
}
