package com.example.clinica.repositories;

import com.example.clinica.models.Appointment;
import com.example.clinica.models.AppointmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

  @Query("select c from Appointment c where c.patient.patientId = :id")
  List<Appointment> findByPatientId(@Param("id") Integer id);

  boolean existsByDoctorIdAndStatus(Integer doctorId, AppointmentStatus status);

  @Procedure(name = "Appointment.criar_consulta")
  void createAppointment(
      @Param("patientId") Integer patientId,
      @Param("doctorId") Integer doctorId,
      @Param("appointmentDate") java.time.LocalDate data,
      @Param("startTime") java.time.LocalTime startTime,
      @Param("endTime") java.time.LocalTime endTime);

  @Query(value = "SELECT TOP 1 * FROM appointments " +
      "WHERE patient_id = :patientId " +
      "  AND doctor_id = :doctorId " +
      "  AND appointment_date = :data " +
      "  AND start_time = :horaInicio " +
      "  AND end_time = :horaFim ", nativeQuery = true)
  Optional<Appointment> findInserted(
      @Param("patientId") Integer patientId,
      @Param("doctorId") Integer doctorId,
      @Param("data") java.time.LocalDate data,
      @Param("horaInicio") java.time.LocalTime horaInicio,
      @Param("horaFim") java.time.LocalTime horaFim);

  @Query(value = "SELECT c.* FROM appointments c " +
      "WHERE c.doctor_id = :doctorId " +
      "  AND c.appointment_date = :date " +
      "  AND ( (c.start_time <= :startTime AND c.end_time > :startTime) " +
      "     OR (c.start_time < :endTime AND c.end_time >= :endTime) " +
      "     OR (c.start_time >= :startTime AND c.end_time <= :endTime) ) " +
      "  AND c.appointment_id <> :excludeId", nativeQuery = true)
  List<Appointment> findByDoctorDateAndTime(@Param("doctorId") Integer doctorId,
      @Param("date") LocalDate date,
      @Param("startTime") LocalTime startTime,
      @Param("endTime") LocalTime endTime,
      @Param("excludeId") Integer excludeId);

  @Query(value = "SELECT c.appointment_id AS id, c.appointment_date, c.start_time, c.end_time, c.status, " +
      "p.name AS patientName, m.name AS doctorName " +
      "FROM appointments c " +
      "INNER JOIN patients p ON p.patient_id = c.patient_id " +
      "INNER JOIN doctors m ON m.doctor_id = c.doctor_id " +
      "WHERE c.appointment_id = :id", nativeQuery = true)
  AppointmentDetailProjection findDetailedAppointment(@Param("id") Integer id);

  @Query(value = "SELECT c.appointment_id AS id, c.appointment_date, c.start_time, c.end_time, c.status, " +
      "p.name AS patientName, m.name AS doctorName " +
      "FROM appointments c " +
      "INNER JOIN patients p ON p.patient_id = c.patient_id " +
      "INNER JOIN doctors m ON m.doctor_id = c.doctor_id " +
      "WHERE c.doctor_id = :doctorId", nativeQuery = true)
  List<Map<String, Object>> findAppointmentsByDoctor(@Param("doctorId") Integer doctorId);

  @Query(value = "SELECT c.appointment_id AS id, c.appointment_date, c.start_time, c.end_time, c.status, " +
      "p.name AS patientName, m.name AS doctorName " +
      "FROM appointments c " +
      "INNER JOIN patients p ON p.patient_id = c.patient_id " +
      "INNER JOIN doctors m ON m.doctor_id = c.doctor_id " +
      "WHERE c.appointment_date = :data", nativeQuery = true)
  List<Map<String, Object>> findAppointmentsByDate(@Param("data") LocalDate data);

  @Query(value = """
      SELECT
          c.appointment_id AS appointmentId,
          c.appointment_date AS dataConsulta,
          c.start_time AS horaInicio,
          c.end_time AS horaFim,
          c.status AS status,
          m.name AS nomeMedico,
          p.name AS nomePaciente
      FROM appointments c
      INNER JOIN doctors m ON m.doctor_id = c.doctor_id
      INNER JOIN patients p ON p.patient_id = c.patient_id
      WHERE c.patient_id = :patientId
        AND c.appointment_date >= DATEADD(MONTH, -:meses, CAST(GETDATE() AS DATE))
      ORDER BY c.appointment_date DESC
      """, nativeQuery = true)
  List<Map<String, Object>> appointmentsReportLastMonths(
      @Param("patientId") Integer patientId,
      @Param("meses") Integer meses);

  @Query(value = """
      SELECT
          c.appointment_id AS appointmentId,
          c.appointment_date AS appointmentDate,
          c.start_time AS startTime,
          c.end_time AS endTime,
          c.status AS status,
          m.name AS doctorName,
          p.name AS patientName
      FROM appointments c
      INNER JOIN doctors m ON m.doctor_id = c.doctor_id
      INNER JOIN patients p ON p.patient_id = c.patient_id
      WHERE c.doctor_id = :doctorId
      ORDER BY c.appointment_date DESC
      """, nativeQuery = true)
  List<Map<String, Object>> appointmentReportByDoctor(@Param("doctorId") Integer doctorId);

  @Query(value = """
      SELECT TOP 50
          c.appointment_id AS appointmentId,
          c.appointment_date AS appointmentDate,
          c.start_time AS startTime,
          c.end_time AS endTime,
          c.status AS status,
          p.name AS patientName,
          m.name AS doctorName
      FROM appointments c
      INNER JOIN patients p ON p.patient_id = c.patient_id
      INNER JOIN doctors m ON m.doctor_id = c.doctor_id
      WHERE c.doctor_id = :doctorId
        AND (c.appointment_date > CAST(GETDATE() AS DATE) OR (c.appointment_date = CAST(GETDATE() AS DATE) AND c.end_time >= CAST(GETDATE() AS time)))
      ORDER BY c.appointment_date ASC, c.start_time ASC
      """, nativeQuery = true)
  List<Map<String, Object>> upcomingAppointmentsReport(@Param("doctorId") Integer doctorId);

  @Query("SELECT c FROM Appointment c WHERE c.doctorId = :doctorId AND c.appointmentDate = :date")
  List<Appointment> findByDoctorIdAndAppointmentDate(@Param("doctorId") Integer doctorId,
      @Param("date") LocalDate date);

}
