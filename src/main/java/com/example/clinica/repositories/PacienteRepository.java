package com.example.clinica.repositories;

import com.example.clinica.models.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    boolean existsByCpf(String cpf);

    Optional<Paciente> findByCpf(String cpf);

    @Query(value = "SELECT dbo.calculate_age(birth_date) FROM patients WHERE patient_id = ?1", nativeQuery = true)
    Integer calcularIdade(Integer idPaciente);

    @Query(value = """
                SELECT c.appointment_id AS idConsulta,
                       c.appointment_date AS dataConsulta,
                       c.start_time AS horaInicio,
                       c.end_time AS horaFim,
                       c.status AS status,
                       p.record_id AS idProntuario,
                       p.anamnesis AS anamnese,
                       p.diagnosis AS diagnostico,
                       p.prescription AS prescricao,
                       m.name AS nomeMedico
                FROM appointments c
                LEFT JOIN medical_records p ON p.appointment_id = c.appointment_id
                JOIN doctors m ON m.doctor_id = c.doctor_id
                WHERE c.patient_id = :idPaciente
                ORDER BY c.appointment_date DESC, c.start_time DESC
            """, nativeQuery = true)
    List<Map<String, Object>> listarHistoricoPaciente(@Param("idPaciente") Integer idPaciente);

    @Query(value = """
                SELECT
                    e.name AS especialidade,
                    COUNT(DISTINCT p.patient_id) AS totalPacientes
                FROM appointments c
                INNER JOIN patients p ON p.patient_id = c.patient_id
                INNER JOIN doctors m ON m.doctor_id = c.doctor_id
                INNER JOIN specialties e ON e.specialty_id = m.specialty_id
                GROUP BY e.name
                ORDER BY totalPacientes DESC
            """, nativeQuery = true)
    List<Map<String, Object>> contarPacientesPorEspecialidade();

}
