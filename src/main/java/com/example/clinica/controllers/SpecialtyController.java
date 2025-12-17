package com.example.clinica.controllers;

import com.example.clinica.dto.ApiResponse;
import com.example.clinica.models.Specialty;
import com.example.clinica.services.SpecialtyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Specialty>>> listSpecialties() {
        List<Specialty> specialties = specialtyService.listSpecialties();
        String message = specialties.isEmpty()
                ? "No specialties found."
                : "Specialties listed successfully.";
        ApiResponse<List<Specialty>> body = ApiResponse.success(message, specialties);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Specialty>> findSpecialtyById(@PathVariable Integer id) {
        Specialty specialty = specialtyService.findSpecialtyById(id);
        ApiResponse<Specialty> body = ApiResponse.success("Specialty found successfully.", specialty);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Specialty>> createSpecialty(@RequestBody Specialty specialty) {
        Specialty created = specialtyService.createSpecialty(specialty);
        ApiResponse<Specialty> body = ApiResponse.success("Specialty created successfully.", created);
        return ResponseEntity.status(201).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Specialty>> updateSpecialty(
            @PathVariable Integer id,
            @RequestBody Specialty updatedSpecialty) {
        Specialty updated = specialtyService.updateSpecialty(id, updatedSpecialty);
        ApiResponse<Specialty> body = ApiResponse.success("Specialty updated successfully.", updated);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSpecialty(@PathVariable Integer id) {
        specialtyService.deleteSpecialty(id);
        ApiResponse<Void> body = ApiResponse.success("Specialty deleted successfully.", null);
        return ResponseEntity.ok(body);
    }

}
