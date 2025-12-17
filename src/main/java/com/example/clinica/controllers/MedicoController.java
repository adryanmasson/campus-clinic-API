package com.example.clinica.controllers;

import com.example.clinica.dto.ApiResponse;
import com.example.clinica.dto.ConsultaDTO;
import com.example.clinica.models.Medico;
import com.example.clinica.services.MedicoService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Medico>>> listarMedicos() {
        List<Medico> medicos = medicoService.listarMedicos();
        String mensagem = medicos.isEmpty()
                ? "No doctors found."
                : "Doctors listed successfully.";
        ApiResponse<List<Medico>> body = ApiResponse.success(mensagem, medicos);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Medico>> buscarMedicoPorId(@PathVariable Integer id) {
        Medico medico = medicoService.buscarMedicoPorId(id);
        ApiResponse<Medico> body = ApiResponse.success("Doctor found successfully.", medico);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Medico>> criarMedico(@RequestBody Medico medico) {
        Medico criado = medicoService.criarMedico(medico);
        ApiResponse<Medico> body = ApiResponse.success("Doctor created successfully.", criado);
        return ResponseEntity.status(201).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Medico>> atualizarMedico(
            @PathVariable Integer id,
            @RequestBody Medico medicoAtualizado) {
        Medico atualizado = medicoService.atualizarMedico(id, medicoAtualizado);
        ApiResponse<Medico> body = ApiResponse.success("Doctor updated successfully.", atualizado);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluirMedico(@PathVariable Integer id) {
        medicoService.excluirMedico(id);
        ApiResponse<Void> body = ApiResponse.success("Doctor deleted successfully.", null);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/especialidade/{id}")
    public ResponseEntity<ApiResponse<List<Medico>>> listarMedicosPorEspecialidade(
            @PathVariable("id") Integer idEspecialidade) {
        List<Medico> medicos = medicoService.listarPorEspecialidade(idEspecialidade);

        String mensagem = medicos.isEmpty()
                ? "No doctors registered for this specialty."
                : "Doctors found.";

        return ResponseEntity.ok(ApiResponse.success(mensagem, medicos));
    }

    @GetMapping("/{id}/relatorio-consultas")
    public ResponseEntity<ApiResponse<List<ConsultaDTO>>> relatorioConsultasPorMedico(
            @PathVariable Integer id) {

        List<ConsultaDTO> consultas = medicoService.relatorioConsultasPorMedico(id);

        String mensagem = consultas.isEmpty()
                ? "No appointments found for this doctor."
                : "Doctor's appointment report returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(mensagem, consultas));
    }

    @GetMapping("/{id}/proximas-consultas")
    public ResponseEntity<ApiResponse<List<ConsultaDTO>>> relatorioProximasConsultas(
            @PathVariable Integer id) {

        List<ConsultaDTO> consultas = medicoService.relatorioProximasConsultas(id);

        String mensagem = consultas.isEmpty()
                ? "No future appointments found for this doctor."
                : "Doctor's upcoming appointments returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(mensagem, consultas));
    }

    @GetMapping("/{id}/horarios-disponiveis")
    public ResponseEntity<ApiResponse<List<Map<String, LocalTime>>>> buscarHorariosDisponiveis(
            @PathVariable("id") Integer idMedico,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        List<Map<String, LocalTime>> horarios = medicoService.buscarHorariosDisponiveis(idMedico, data);
        String msg = horarios.isEmpty()
                ? "No available time slots."
                : "Available time slots returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(msg, horarios));
    }

}
