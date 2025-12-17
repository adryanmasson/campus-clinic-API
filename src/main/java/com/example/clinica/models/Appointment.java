package com.example.clinica.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@NamedStoredProcedureQuery(name = "Appointment.criar_consulta", procedureName = "create_appointment", parameters = {
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_patient_id", type = Integer.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_doctor_id", type = Integer.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_appointment_date", type = java.time.LocalDate.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_start_time", type = java.time.LocalTime.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_end_time", type = java.time.LocalTime.class)
})

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Integer appointmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "doctor_id")
    private Integer doctorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private Doctor doctor;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public Integer getId() {
        return this.appointmentId;
    }

    public void setId(Integer id) {
        this.appointmentId = id;
    }

    public Doctor getDoctor() {
        return this.doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            try {
                Integer mid = null;
                mid = (Integer) doctor.getClass().getMethod("getDoctorId").invoke(doctor);
                if (mid != null)
                    this.doctorId = mid;
            } catch (Exception ignored) {
            }
        }
    }
}
