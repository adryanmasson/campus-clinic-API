package com.example.clinica.services;

import com.example.clinica.models.Consulta;
import com.example.clinica.models.Paciente;
import com.example.clinica.repositories.PacienteRepository;
import com.example.clinica.repositories.ConsultaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;

    public PacienteService(PacienteRepository pacienteRepository, ConsultaRepository consultaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
    }

    public List<Paciente> listarPacientes() {
        return pacienteRepository.findAll();
    }

    public Paciente buscarPacientePorId(Integer id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado com ID: " + id));
    }

    public Paciente criarPaciente(Paciente paciente) {
        Optional<Paciente> pacienteExistente = pacienteRepository.findByCpf(paciente.getCpf());
        if (pacienteExistente.isPresent()) {
            throw new RuntimeException("Já existe paciente com esse CPF: " + paciente.getCpf());
        }
        return pacienteRepository.save(paciente);
    }

    public Paciente atualizarPaciente(Integer id, Paciente dadosAtualizados) {
        Paciente paciente = buscarPacientePorId(id);

        if (dadosAtualizados.getTelefone() != null) {
            paciente.setTelefone(dadosAtualizados.getTelefone());
        }
        if (dadosAtualizados.getEmail() != null) {
            paciente.setEmail(dadosAtualizados.getEmail());
        }
        if (dadosAtualizados.getLogradouro() != null) {
            paciente.setLogradouro(dadosAtualizados.getLogradouro());
        }

        return pacienteRepository.save(paciente);
    }

    public void excluirPaciente(Integer id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        List<Consulta> consultas = consultaRepository.findByPacienteId(id);
        if (!consultas.isEmpty()) {
            throw new RuntimeException("Paciente possui consultas associadas e não pode ser excluído.");
        }

        pacienteRepository.delete(paciente);
    }

    public Integer calcularIdadePaciente(Integer idPaciente) {
        return pacienteRepository.calcularIdade(idPaciente);
    }

}
