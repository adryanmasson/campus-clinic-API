package com.example.clinica.controllers;

import com.example.clinica.dto.ApiResponse;
import com.example.clinica.dto.AppointmentDTO;
import com.example.clinica.models.Doctor;
import com.example.clinica.services.DoctorService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Doctor>>> listDoctors() {
        List<Doctor> doctors = doctorService.listDoctors();
        String message = doctors.isEmpty()
                ? "No doctors found."
                : "Doctors listed successfully.";
        ApiResponse<List<Doctor>> body = ApiResponse.success(message, doctors);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Doctor>> findDoctorById(@PathVariable Integer id) {
        Doctor doctor = doctorService.findDoctorById(id);
        ApiResponse<Doctor> body = ApiResponse.success("Doctor found successfully.", doctor);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Doctor>> createDoctor(@RequestBody Doctor doctor) {
        Doctor created = doctorService.createDoctor(doctor);
        ApiResponse<Doctor> body = ApiResponse.success("Doctor created successfully.", created);
        return ResponseEntity.status(201).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Doctor>> updateDoctor(
            @PathVariable Integer id,
            @RequestBody Doctor updatedDoctor) {
        Doctor updated = doctorService.updateDoctor(id, updatedDoctor);
        ApiResponse<Doctor> body = ApiResponse.success("Doctor updated successfully.", updated);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Integer id) {
        doctorService.deleteDoctor(id);
        ApiResponse<Void> body = ApiResponse.success("Doctor deleted successfully.", null);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/specialty/{id}")
    public ResponseEntity<ApiResponse<List<Doctor>>> listDoctorsBySpecialty(
            @PathVariable("id") Integer specialtyId) {
        List<Doctor> doctors = doctorService.listBySpecialty(specialtyId);

        String message = doctors.isEmpty()
                ? "No doctors registered for this specialty."
                : "Doctors found.";

        return ResponseEntity.ok(ApiResponse.success(message, doctors));
    }

    @GetMapping("/{id}/relatorio-consultas")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> appointmentReportByDoctor(
            @PathVariable Integer id) {

        List<AppointmentDTO> appointments = doctorService.appointmentReportByDoctor(id);

        String message = appointments.isEmpty()
                ? "No appointments found for this doctor."
                : "Doctor's appointment report returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(message, appointments));
    }

    @GetMapping("/{id}/proximas-consultas")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> upcomingAppointmentsReport(
            @PathVariable Integer id) {

        List<AppointmentDTO> appointments = doctorService.upcomingAppointmentsReport(id);

        String message = appointments.isEmpty()
                ? "No future appointments found for this doctor."
                : "Doctor's upcoming appointments returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(message, appointments));
    }

    @GetMapping("/{id}/horarios-disponiveis")
    public ResponseEntity<ApiResponse<List<Map<String, LocalTime>>>> findAvailableTimeSlots(
            @PathVariable("id") Integer doctorId,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<Map<String, LocalTime>> timeSlots = doctorService.findAvailableTimeSlots(doctorId, date);
        String msg = timeSlots.isEmpty()
                ? "No available time slots."
                : "Available time slots returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(msg, timeSlots));
    }

}
