package com.example.clinica.controllers;

import com.example.clinica.dto.ApiResponse;
import com.example.clinica.models.Especialidade;
import com.example.clinica.services.EspecialidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
public class EspecialidadeController {

    private final EspecialidadeService especialidadeService;

    public EspecialidadeController(EspecialidadeService especialidadeService) {
        this.especialidadeService = especialidadeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Especialidade>>> listarEspecialidades() {
        List<Especialidade> especialidades = especialidadeService.listarEspecialidades();
        String mensagem = especialidades.isEmpty()
                ? "No specialties found."
                : "Specialties listed successfully.";
        ApiResponse<List<Especialidade>> body = ApiResponse.success(mensagem, especialidades);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Especialidade>> buscarEspecialidadePorId(@PathVariable Integer id) {
        Especialidade especialidade = especialidadeService.buscarEspecialidadePorId(id);
        ApiResponse<Especialidade> body = ApiResponse.success("Specialty found successfully.", especialidade);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Especialidade>> criarEspecialidade(@RequestBody Especialidade especialidade) {
        Especialidade criado = especialidadeService.criarEspecialidade(especialidade);
        ApiResponse<Especialidade> body = ApiResponse.success("Specialty created successfully.", criado);
        return ResponseEntity.status(201).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Especialidade>> atualizarEspecialidade(
            @PathVariable Integer id,
            @RequestBody Especialidade especialidadeAtualizada) {
        Especialidade atualizado = especialidadeService.atualizarEspecialidade(id, especialidadeAtualizada);
        ApiResponse<Especialidade> body = ApiResponse.success("Specialty updated successfully.", atualizado);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluirEspecialidade(@PathVariable Integer id) {
        especialidadeService.excluirEspecialidade(id);
        ApiResponse<Void> body = ApiResponse.success("Specialty deleted successfully.", null);
        return ResponseEntity.ok(body);
    }

}
