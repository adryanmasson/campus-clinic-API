package com.example.clinica.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.clinica.dto.MedicalRecordDTO;
import com.example.clinica.dto.CreateMedicalRecordDTO;
import com.example.clinica.services.MedicalRecordService;
import com.example.clinica.dto.ApiResponse;
import com.example.clinica.dto.UpdateMedicalRecordDTO;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService prontuarioService;

    public MedicalRecordController(MedicalRecordService prontuarioService) {
        this.prontuarioService = prontuarioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicalRecordDTO>>> listMedicalRecords() {
        List<MedicalRecordDTO> prontuarios = prontuarioService.listMedicalRecords();
        String mensagem = prontuarios.isEmpty()
                ? "No medical records found."
                : "Medical records returned successfully.";
        return ResponseEntity.ok(ApiResponse.success(mensagem, prontuarios));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<MedicalRecordDTO>> findByAppointment(@PathVariable Integer appointmentId) {
        MedicalRecordDTO dto = prontuarioService.findByAppointment(appointmentId);
        if (dto == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Medical record not found for appointment " + appointmentId));
        }
        return ResponseEntity.ok(ApiResponse.success("Medical record returned successfully.", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicalRecordDTO>> createMedicalRecord(@RequestBody CreateMedicalRecordDTO dto) {
        MedicalRecordDTO criado = prontuarioService.createMedicalRecord(dto);
        return ResponseEntity.status(201).body(ApiResponse.success("Medical record created successfully.", criado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicalRecordDTO>> updateMedicalRecord(
            @PathVariable Integer id,
            @RequestBody UpdateMedicalRecordDTO dados) {
        MedicalRecordDTO prontuarioAtualizado = prontuarioService.updateMedicalRecord(id, dados);
        if (prontuarioAtualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success("Medical record updated successfully.", prontuarioAtualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> excluirProntuario(@PathVariable Integer id) {
        boolean excluido = prontuarioService.excluirProntuario(id);
        if (excluido) {
            return ResponseEntity.ok(ApiResponse.success("Medical record deleted successfully.", null));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error("Medical record not found."));
        }
    }

}
