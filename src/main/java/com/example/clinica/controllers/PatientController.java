package com.example.clinica.controllers;

import com.example.clinica.dto.ApiResponse;
import com.example.clinica.dto.AppointmentDTO;
import com.example.clinica.dto.PatientHistoryDTO;
import com.example.clinica.models.Patient;
import com.example.clinica.services.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patients")
public class PatientController {
    private final PatientService pacienteService;

    public PatientController(PatientService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Patient>>> listPatients() {
        List<Patient> pacientes = pacienteService.listPatients();
        String mensagem = pacientes.isEmpty()
                ? "No patients found."
                : "Patients listed successfully.";
        ApiResponse<List<Patient>> body = ApiResponse.success(mensagem, pacientes);

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Patient>> findPatientById(@PathVariable Integer id) {
        Patient patient = pacienteService.findPatientById(id);
        ApiResponse<Patient> body = ApiResponse.success("Patient found successfully.", patient);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Patient>> createPatient(@RequestBody Patient patient) {
        Patient criado = pacienteService.createPatient(patient);
        ApiResponse<Patient> body = ApiResponse.success("Patient created successfully.", criado);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Patient>> updatePatient(
            @PathVariable Integer id,
            @RequestBody Patient pacienteAtualizado) {
        Patient atualizado = pacienteService.updatePatient(id, pacienteAtualizado);
        ApiResponse<Patient> body = ApiResponse.success("Patient updated successfully.", atualizado);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Integer id) {
        pacienteService.deletePatient(id);
        ApiResponse<Void> body = ApiResponse.success("Patient deleted successfully.", null);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/idade")
    public ResponseEntity<ApiResponse<Integer>> idadePaciente(@PathVariable Integer id) {
        Integer idade = pacienteService.calculatePatientAge(id);
        ApiResponse<Integer> body = ApiResponse.success("Age calculated successfully.", idade);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<ApiResponse<List<PatientHistoryDTO>>> listPatientHistory(
            @PathVariable Integer id) {

        List<PatientHistoryDTO> historico = pacienteService.listPatientHistory(id);

        String mensagem = historico.isEmpty()
                ? "No history found for this patient."
                : "Patient history returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(mensagem, historico));
    }

    @GetMapping("/{id}/relatorio-consultas/{meses}")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> appointmentsReportLastMonths(
            @PathVariable("id") Integer patientId,
            @PathVariable("meses") Integer meses) {

        List<AppointmentDTO> relatorio = pacienteService.appointmentsReportLastMonths(patientId, meses);

        String mensagem = relatorio.isEmpty()
                ? "No appointments found in the last " + meses + " months."
                : "Appointment report for the last " + meses + " months returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(mensagem, relatorio));
    }

    @GetMapping("/relatorio/especialidades")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> countPatientsBySpecialty() {
        List<Map<String, Object>> resultado = pacienteService.countPatientsBySpecialty();

        String mensagem = resultado.isEmpty()
                ? "No specialties found."
                : "Patient count by specialty returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(mensagem, resultado));
    }

}
