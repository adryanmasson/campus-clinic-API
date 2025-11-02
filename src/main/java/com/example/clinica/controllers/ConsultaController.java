package com.example.clinica.controllers;

import com.example.clinica.dto.AgendarConsultaDTO;
import com.example.clinica.dto.ApiResponse;
import com.example.clinica.dto.AtualizarConsultaDTO;
import com.example.clinica.dto.ConsultaDTO;
import com.example.clinica.services.ConsultaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConsultaDTO>>> listarConsultas() {
        List<ConsultaDTO> consultas = consultaService.listarConsultas();
        ApiResponse<List<ConsultaDTO>> body = ApiResponse.sucesso(
                consultas.isEmpty() ? "Nenhuma consulta encontrada." : "Consultas listadas com sucesso.",
                consultas);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsultaDTO>> buscarConsultaPorId(@PathVariable Integer id) {
        ConsultaDTO consulta = consultaService.buscarConsultaPorId(id);
        ApiResponse<ConsultaDTO> body = ApiResponse.sucesso("Consulta encontrada com sucesso.", consulta);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/agendar")
    public ResponseEntity<ApiResponse<ConsultaDTO>> agendarConsulta(@RequestBody AgendarConsultaDTO dto) {
        ConsultaDTO consulta = consultaService.agendarConsulta(dto.getIdPaciente(), dto.getIdMedico(),
                dto.getData(), dto.getHoraInicio(), dto.getHoraFim());
        return ResponseEntity.ok(ApiResponse.sucesso("Consulta agendada com sucesso", consulta));

    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsultaDTO>> atualizar(
            @PathVariable Integer id,
            @RequestBody AtualizarConsultaDTO dto) {

        ConsultaDTO consultaAtualizada = consultaService.atualizarConsulta(id, dto);
        return ResponseEntity.ok(ApiResponse.sucesso("Consulta atualizada com sucesso", consultaAtualizada));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<ConsultaDTO>> cancelar(@PathVariable Integer id) {
        ConsultaDTO consultaCancelada = consultaService.cancelarConsulta(id);
        return ResponseEntity.ok(ApiResponse.sucesso("Consulta cancelada com sucesso", consultaCancelada));
    }

}