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
@RequestMapping("/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicalRecordDTO>>> listMedicalRecords() {
        List<MedicalRecordDTO> records = medicalRecordService.listMedicalRecords();
        String message = records.isEmpty()
                ? "No medical records found."
                : "Medical records returned successfully.";
        return ResponseEntity.ok(ApiResponse.success(message, records));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<MedicalRecordDTO>> findByAppointment(@PathVariable Integer appointmentId) {
        MedicalRecordDTO dto = medicalRecordService.findByAppointment(appointmentId);
        if (dto == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Medical record not found for appointment " + appointmentId));
        }
        return ResponseEntity.ok(ApiResponse.success("Medical record returned successfully.", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicalRecordDTO>> createMedicalRecord(@RequestBody CreateMedicalRecordDTO dto) {
        MedicalRecordDTO created = medicalRecordService.createMedicalRecord(dto);
        return ResponseEntity.status(201).body(ApiResponse.success("Medical record created successfully.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicalRecordDTO>> updateMedicalRecord(
            @PathVariable Integer id,
            @RequestBody UpdateMedicalRecordDTO data) {
        MedicalRecordDTO updatedRecord = medicalRecordService.updateMedicalRecord(id, data);
        if (updatedRecord == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success("Medical record updated successfully.", updatedRecord));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMedicalRecord(@PathVariable Integer id) {
        boolean deleted = medicalRecordService.deleteMedicalRecord(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Medical record deleted successfully.", null));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error("Medical record not found."));
        }
    }

}
