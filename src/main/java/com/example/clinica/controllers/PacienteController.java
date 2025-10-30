package com.example.clinica.controllers;

import com.example.clinica.models.Paciente;
import com.example.clinica.services.PacienteService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {
    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public List<Paciente> listarPacientes() {
        return pacienteService.listarPacientes();
    }

    @GetMapping("/{id}")
    public Paciente buscarPacientePorId(@PathVariable Integer id) {
        try {
            return pacienteService.buscarPacientePorId(id);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    public Paciente criarPaciente(@RequestBody Paciente paciente) {
        try {
            return pacienteService.criarPaciente(paciente);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Paciente atualizarPaciente(
            @PathVariable Integer id,
            @RequestBody Paciente pacienteAtualizado) {
        try {
            return pacienteService.atualizarPaciente(id, pacienteAtualizado);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void excluirPaciente(@PathVariable Integer id) {
        try {
            pacienteService.excluirPaciente(id);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
