package com.example.clinica.repositories;

import java.sql.Date;
import java.sql.Time;

public interface AppointmentDetailProjection {
    Integer getId();

    Date getAppointmentDate();

    Time getStartTime();

    Time getEndTime();

    String getStatus();

    String getNome_paciente();

    String getNome_medico();
}
