package com.example.clinica.controllers;

import com.example.clinica.dto.ApiResponse;
import com.example.clinica.models.Especialidade;
import com.example.clinica.services.EspecialidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/especialidades")
public class EspecialidadeController {

    private final EspecialidadeService especialidadeService;

    public EspecialidadeController(EspecialidadeService especialidadeService) {
        this.especialidadeService = especialidadeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Especialidade>>> listarEspecialidades() {
        List<Especialidade> especialidades = especialidadeService.listarEspecialidades();
        ApiResponse<List<Especialidade>> body = ApiResponse.sucesso("Especialidades listadas com sucesso.",
                especialidades);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Especialidade>> buscarEspecialidadePorId(@PathVariable Integer id) {
        Especialidade especialidade = especialidadeService.buscarEspecialidadePorId(id);
        ApiResponse<Especialidade> body = ApiResponse.sucesso("Especialidade encontrada com sucesso.", especialidade);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Especialidade>> criarEspecialidade(@RequestBody Especialidade especialidade) {
        Especialidade criado = especialidadeService.criarEspecialidade(especialidade);
        ApiResponse<Especialidade> body = ApiResponse.sucesso("Especialidade criada com sucesso.", criado);
        return ResponseEntity.status(201).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Especialidade>> atualizarEspecialidade(
            @PathVariable Integer id,
            @RequestBody Especialidade especialidadeAtualizada) {
        Especialidade atualizado = especialidadeService.atualizarEspecialidade(id, especialidadeAtualizada);
        ApiResponse<Especialidade> body = ApiResponse.sucesso("Especialidade atualizada com sucesso.", atualizado);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluirEspecialidade(@PathVariable Integer id) {
        especialidadeService.excluirEspecialidade(id);
        ApiResponse<Void> body = ApiResponse.sucesso("Especialidade excluída com sucesso.", null);
        return ResponseEntity.ok(body);
    }

}
