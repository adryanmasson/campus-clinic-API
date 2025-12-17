package com.example.clinica.controllers;

import com.example.clinica.dto.ScheduleAppointmentDTO;
import com.example.clinica.dto.ApiResponse;
import com.example.clinica.dto.UpdateAppointmentDTO;
import com.example.clinica.dto.AppointmentDTO;
import com.example.clinica.services.AppointmentService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService consultaService;

    public AppointmentController(AppointmentService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> listAppointments() {
        List<AppointmentDTO> consultas = consultaService.listAppointments();
        String mensagem = consultas.isEmpty() ? "No appointments found." : "Appointments listed successfully.";
        ApiResponse<List<AppointmentDTO>> body = ApiResponse.success(mensagem, consultas);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> buscarConsultaPorId(@PathVariable Integer id) {
        AppointmentDTO appointment = consultaService.buscarConsultaPorId(id);
        ApiResponse<AppointmentDTO> body = ApiResponse.success("Appointment found successfully.", appointment);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/agendar")
    public ResponseEntity<ApiResponse<AppointmentDTO>> scheduleAppointment(@RequestBody ScheduleAppointmentDTO dto) {
        AppointmentDTO appointment = consultaService.scheduleAppointment(dto.getIdPaciente(), dto.getIdMedico(),
                dto.getData(), dto.getHoraInicio(), dto.getHoraFim());
        return ResponseEntity.ok(ApiResponse.success("Appointment scheduled successfully", appointment));

    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> atualizar(
            @PathVariable Integer id,
            @RequestBody UpdateAppointmentDTO dto) {

        AppointmentDTO consultaAtualizada = consultaService.updateAppointment(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Appointment updated successfully", consultaAtualizada));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<AppointmentDTO>> cancelar(@PathVariable Integer id) {
        AppointmentDTO consultaCancelada = consultaService.cancelAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", consultaCancelada));
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> listarPorPaciente(@PathVariable Integer id) {
        List<AppointmentDTO> consultas = consultaService.listarConsultasPorPaciente(id);

        String mensagem = consultas.isEmpty()
                ? "No appointments found for the patient."
                : "Patient's appointments returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(mensagem, consultas));
    }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> listarPorMedico(@PathVariable Integer id) {
        List<AppointmentDTO> consultas = consultaService.listarConsultasPorMedico(id);

        String mensagem = consultas.isEmpty()
                ? "No appointments found for the doctor."
                : "Doctor's appointments returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(mensagem, consultas));
    }

    @GetMapping("/data/{data}")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> listarPorData(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<AppointmentDTO> consultas = consultaService.listarConsultasPorData(data);

        String mensagem = consultas.isEmpty()
                ? "No appointments found for this date."
                : "Appointments for the date returned successfully.";

        return ResponseEntity.ok(ApiResponse.success(mensagem, consultas));
    }

}