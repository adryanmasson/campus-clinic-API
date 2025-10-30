package com.example.clinica.controllers;

import com.example.clinica.dto.ApiResponse;
import com.example.clinica.models.Paciente;
import com.example.clinica.services.PacienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {
    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Paciente>>> listarPacientes() {
        List<Paciente> pacientes = pacienteService.listarPacientes();
        ApiResponse<List<Paciente>> body = ApiResponse.sucesso("Pacientes listados com sucesso.", pacientes);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Paciente>> buscarPacientePorId(@PathVariable Integer id) {
        Paciente paciente = pacienteService.buscarPacientePorId(id);
        ApiResponse<Paciente> body = ApiResponse.sucesso("Paciente encontrado com sucesso.", paciente);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Paciente>> criarPaciente(@RequestBody Paciente paciente) {
        Paciente criado = pacienteService.criarPaciente(paciente);
        ApiResponse<Paciente> body = ApiResponse.sucesso("Paciente criado com sucesso.", criado);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Paciente>> atualizarPaciente(
            @PathVariable Integer id,
            @RequestBody Paciente pacienteAtualizado) {
        Paciente atualizado = pacienteService.atualizarPaciente(id, pacienteAtualizado);
        ApiResponse<Paciente> body = ApiResponse.sucesso("Paciente atualizado com sucesso.", atualizado);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluirPaciente(@PathVariable Integer id) {
        pacienteService.excluirPaciente(id);
        ApiResponse<Void> body = ApiResponse.sucesso("Paciente excluído com sucesso.", null);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/idade")
    public ResponseEntity<ApiResponse<Integer>> idadePaciente(@PathVariable Integer id) {
        Integer idade = pacienteService.calcularIdadePaciente(id);
        ApiResponse<Integer> body = ApiResponse.sucesso("Idade calculada com sucesso.", idade);
        return ResponseEntity.ok(body);
    }

}
