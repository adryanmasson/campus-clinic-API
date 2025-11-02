package com.example.clinica.controllers;

import com.example.clinica.dto.ApiResponse;
import com.example.clinica.dto.ConsultaDTO;
import com.example.clinica.models.Medico;
import com.example.clinica.services.MedicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Medico>>> listarMedicos() {
        List<Medico> medicos = medicoService.listarMedicos();
        String mensagem = medicos.isEmpty()
                ? "Nenhum medico encontrado."
                : "Médicos listados com sucesso.";
        ApiResponse<List<Medico>> body = ApiResponse.sucesso(mensagem, medicos);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Medico>> buscarMedicoPorId(@PathVariable Integer id) {
        Medico medico = medicoService.buscarMedicoPorId(id);
        ApiResponse<Medico> body = ApiResponse.sucesso("Médico encontrado com sucesso.", medico);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Medico>> criarMedico(@RequestBody Medico medico) {
        Medico criado = medicoService.criarMedico(medico);
        ApiResponse<Medico> body = ApiResponse.sucesso("Médico criado com sucesso.", criado);
        return ResponseEntity.status(201).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Medico>> atualizarMedico(
            @PathVariable Integer id,
            @RequestBody Medico medicoAtualizado) {
        Medico atualizado = medicoService.atualizarMedico(id, medicoAtualizado);
        ApiResponse<Medico> body = ApiResponse.sucesso("Médico atualizado com sucesso.", atualizado);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluirMedico(@PathVariable Integer id) {
        medicoService.excluirMedico(id);
        ApiResponse<Void> body = ApiResponse.sucesso("Médico excluído com sucesso.", null);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/especialidade/{id}")
    public ResponseEntity<ApiResponse<List<Medico>>> listarMedicosPorEspecialidade(
            @PathVariable("id") Integer idEspecialidade) {
        List<Medico> medicos = medicoService.listarPorEspecialidade(idEspecialidade);

        String mensagem = medicos.isEmpty()
                ? "Não há médicos cadastrados para essa especialidade."
                : "Médicos encontrados.";

        return ResponseEntity.ok(ApiResponse.sucesso(mensagem, medicos));
    }

    @GetMapping("/{id}/relatorio-consultas")
    public ResponseEntity<ApiResponse<List<ConsultaDTO>>> relatorioConsultasPorMedico(
            @PathVariable Integer id) {

        List<ConsultaDTO> consultas = medicoService.relatorioConsultasPorMedico(id);

        String mensagem = consultas.isEmpty()
                ? "Nenhuma consulta encontrada para este médico."
                : "Relatório de consultas do médico retornado com sucesso.";

        return ResponseEntity.ok(ApiResponse.sucesso(mensagem, consultas));
    }

}
