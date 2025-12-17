package com.example.clinica.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class EndToEndIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void fullFlow_createEntitiesAndScheduleConsultaAndCreateProntuario() throws Exception {
        // Create Especialidade
        String especialidadeJson = "{ \"nome\": \"Cardiologia\", \"descricao\": \"Teste\" }";
        String espRes = mockMvc.perform(MockMvcRequestBuilders.post("/especialidades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(especialidadeJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode espNode = objectMapper.readTree(espRes).get("data");
        int idEspecialidade = espNode.get("id_especialidade").asInt();

        // Create Medico
        String medicoJson = String.format("{ \"nome\": \"Dr Teste\", \"crm\": \"CRM123\", \"especialidade\": { \"id_especialidade\": %d }, \"data_nascimento\": \"1980-01-01\" }", idEspecialidade);
        String medRes = mockMvc.perform(MockMvcRequestBuilders.post("/medicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(medicoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode medNode = objectMapper.readTree(medRes).get("data");
        int idMedico = medNode.get("id_medico").asInt();

        // Create Paciente
        String pacienteJson = "{ \"nome\": \"Paciente Teste\", \"sexo\": \"M\", \"cpf\": \"11122233344\", \"data_nascimento\": \"1990-01-01\", \"email\": \"p@mail.test\" }";
        String pacRes = mockMvc.perform(MockMvcRequestBuilders.post("/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pacienteJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode pacNode = objectMapper.readTree(pacRes).get("data");
        int idPaciente = pacNode.get("id_paciente").asInt();

        // Agendar consulta
        LocalDate data = LocalDate.now().plusDays(1);
        String agendarJson = String.format("{ \"idPaciente\": %d, \"idMedico\": %d, \"data\": \"%s\", \"horaInicio\": \"09:00:00\", \"horaFim\": \"09:30:00\" }", idPaciente, idMedico, data.toString());
        String agRes = mockMvc.perform(MockMvcRequestBuilders.post("/consultas/agendar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(agendarJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode agNode = objectMapper.readTree(agRes).get("data");
        int idConsulta = agNode.get("id_consulta").asInt();

        // Create Prontuario
        String prontuarioJson = String.format("{ \"idConsulta\": %d, \"anamnese\": \"Paciente em bom estado\", \"diagnostico\": \"Check\", \"prescricao\": \"Nenhuma\" }", idConsulta);
        String prRes = mockMvc.perform(MockMvcRequestBuilders.post("/prontuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(prontuarioJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode prNode = objectMapper.readTree(prRes).get("data");
        Assertions.assertEquals(idConsulta, prNode.get("idConsulta").asInt());

        // Fetch prontuario by consulta
        String fetch = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/prontuarios/consulta/%d", idConsulta)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode fetched = objectMapper.readTree(fetch).get("data");
        Assertions.assertEquals(idConsulta, fetched.get("idConsulta").asInt());

        // Check idade endpoint
        String idadeRes = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/pacientes/%d/idade", idPaciente)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode idadeNode = objectMapper.readTree(idadeRes).get("data");
        Assertions.assertTrue(idadeNode.isNumber());

    }
}
