-- ============================================
-- Migration Script: Portuguese to English
-- Database: clinica_do_campus -> campus_clinic
-- ============================================

USE clinica_do_campus;
GO

-- Step 1: Drop all foreign key constraints
ALTER TABLE consultas DROP CONSTRAINT fk_consulta_paciente;
ALTER TABLE consultas DROP CONSTRAINT fk_consulta_medico;
ALTER TABLE medicos DROP CONSTRAINT fk_medico_especialidade;
ALTER TABLE prontuarios DROP CONSTRAINT fk_prontuario_consulta;
ALTER TABLE auditoria_prontuario DROP CONSTRAINT fk_auditoria_prontuario;
GO

-- Step 2: Drop triggers
DROP TRIGGER IF EXISTS trg_auditoria_prontuario_update;
GO

-- Step 3: Drop indexes
DROP INDEX IF EXISTS idx_consultas_fk_id_medico ON consultas;
DROP INDEX IF EXISTS idx_consultas_fk_id_paciente ON consultas;
DROP INDEX IF EXISTS idx_auditoria_fk_prontuario ON auditoria_prontuario;
DROP INDEX IF EXISTS idx_prontuarios_fk_id_consulta ON prontuarios;
GO

-- Step 4: Rename tables
EXEC sp_rename 'especialidades', 'specialties';
EXEC sp_rename 'medicos', 'doctors';
EXEC sp_rename 'pacientes', 'patients';
EXEC sp_rename 'consultas', 'appointments';
EXEC sp_rename 'prontuarios', 'medical_records';
EXEC sp_rename 'auditoria_prontuario', 'medical_record_audit';
GO

-- Step 5: Rename columns in specialties table
EXEC sp_rename 'specialties.id_especialidade', 'specialty_id', 'COLUMN';
EXEC sp_rename 'specialties.nome', 'name', 'COLUMN';
EXEC sp_rename 'specialties.descricao', 'description', 'COLUMN';
GO

-- Step 6: Rename columns in doctors table  
EXEC sp_rename 'doctors.id_medico', 'doctor_id', 'COLUMN';
EXEC sp_rename 'doctors.nome', 'name', 'COLUMN';
EXEC sp_rename 'doctors.crm', 'medical_license', 'COLUMN';
EXEC sp_rename 'doctors.fk_id_especialidade', 'specialty_id', 'COLUMN';
EXEC sp_rename 'doctors.data_nascimento', 'birth_date', 'COLUMN';
EXEC sp_rename 'doctors.telefone', 'phone', 'COLUMN';
EXEC sp_rename 'doctors.ativo', 'active', 'COLUMN';
GO

-- Step 7: Rename columns in patients table
EXEC sp_rename 'patients.id_paciente', 'patient_id', 'COLUMN';
EXEC sp_rename 'patients.nome', 'name', 'COLUMN';
EXEC sp_rename 'patients.sexo', 'gender', 'COLUMN';
EXEC sp_rename 'patients.data_nascimento', 'birth_date', 'COLUMN';
EXEC sp_rename 'patients.telefone', 'phone', 'COLUMN';
EXEC sp_rename 'patients.logradouro', 'address', 'COLUMN';
GO

-- Step 8: Rename columns in appointments table
EXEC sp_rename 'appointments.id_consulta', 'appointment_id', 'COLUMN';
EXEC sp_rename 'appointments.fk_id_paciente', 'patient_id', 'COLUMN';
EXEC sp_rename 'appointments.fk_id_medico', 'doctor_id', 'COLUMN';
EXEC sp_rename 'appointments.data_consulta', 'appointment_date', 'COLUMN';
EXEC sp_rename 'appointments.hora_inicio', 'start_time', 'COLUMN';
EXEC sp_rename 'appointments.hora_fim', 'end_time', 'COLUMN';
GO

-- Step 9: Rename columns in medical_records table
EXEC sp_rename 'medical_records.id_prontuario', 'record_id', 'COLUMN';
EXEC sp_rename 'medical_records.fk_id_consulta', 'appointment_id', 'COLUMN';
EXEC sp_rename 'medical_records.anamnese', 'anamnesis', 'COLUMN';
EXEC sp_rename 'medical_records.diagnostico', 'diagnosis', 'COLUMN';
EXEC sp_rename 'medical_records.prescricao', 'prescription', 'COLUMN';
EXEC sp_rename 'medical_records.data_registro', 'record_date', 'COLUMN';
GO

-- Step 10: Rename columns in medical_record_audit table
EXEC sp_rename 'medical_record_audit.id_auditoria', 'audit_id', 'COLUMN';
EXEC sp_rename 'medical_record_audit.fk_id_prontuario', 'record_id', 'COLUMN';
EXEC sp_rename 'medical_record_audit.anamnese_antiga', 'old_anamnesis', 'COLUMN';
EXEC sp_rename 'medical_record_audit.anamnese_nova', 'new_anamnesis', 'COLUMN';
EXEC sp_rename 'medical_record_audit.diagnostico_antigo', 'old_diagnosis', 'COLUMN';
EXEC sp_rename 'medical_record_audit.diagnostico_novo', 'new_diagnosis', 'COLUMN';
EXEC sp_rename 'medical_record_audit.prescricao_antiga', 'old_prescription', 'COLUMN';
EXEC sp_rename 'medical_record_audit.prescricao_nova', 'new_prescription', 'COLUMN';
EXEC sp_rename 'medical_record_audit.data_alteracao', 'change_date', 'COLUMN';
GO

-- Step 11: Drop and recreate constraints with new names
ALTER TABLE doctors DROP CONSTRAINT chk_medico_data;
ALTER TABLE doctors ADD CONSTRAINT chk_doctor_birth_date CHECK (birth_date <= CAST(GETDATE() AS DATE));

ALTER TABLE patients DROP CONSTRAINT chk_paciente_data;
ALTER TABLE patients ADD CONSTRAINT chk_patient_birth_date CHECK (birth_date <= CAST(GETDATE() AS DATE));

ALTER TABLE appointments DROP CONSTRAINT chk_hora_consulta;
ALTER TABLE appointments ADD CONSTRAINT chk_appointment_time CHECK (start_time < end_time);

ALTER TABLE medical_records DROP CONSTRAINT chk_prontuario_data;
ALTER TABLE medical_records ADD CONSTRAINT chk_record_date CHECK (record_date <= CAST(GETDATE() AS DATE));
GO

-- Step 12: Update CHECK constraint for gender
ALTER TABLE patients DROP CONSTRAINT CK__pacientes__sexo__*; -- Drop existing check constraint (name varies)
ALTER TABLE patients ADD CONSTRAINT chk_patient_gender CHECK (gender IN ('M','F','OTHER'));
GO

-- Step 13: Update CHECK constraint for status  
ALTER TABLE appointments DROP CONSTRAINT CK__consultas__statu__*; -- Drop existing check constraint (name varies)
ALTER TABLE appointments ADD CONSTRAINT chk_appointment_status CHECK (status IN ('SCHEDULED','COMPLETED','CANCELLED'));
GO

-- Step 14: Recreate foreign key constraints
ALTER TABLE doctors ADD CONSTRAINT fk_doctor_specialty 
  FOREIGN KEY (specialty_id) REFERENCES specialties(specialty_id) ON UPDATE CASCADE;

ALTER TABLE appointments ADD CONSTRAINT fk_appointment_patient 
  FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE appointments ADD CONSTRAINT fk_appointment_doctor 
  FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE medical_records ADD CONSTRAINT fk_record_appointment 
  FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE CASCADE;

ALTER TABLE medical_record_audit ADD CONSTRAINT fk_audit_record 
  FOREIGN KEY (record_id) REFERENCES medical_records(record_id) ON DELETE CASCADE ON UPDATE CASCADE;
GO

-- Step 15: Drop old function and procedure
DROP FUNCTION IF EXISTS dbo.calcular_idade;
DROP PROCEDURE IF EXISTS dbo.criar_consulta;
GO

-- Step 16: Create new function with English name
CREATE FUNCTION calculate_age (@birth_date DATE)
RETURNS INT
AS
BEGIN
  DECLARE @age INT;
  SET @age = DATEDIFF(YEAR, @birth_date, GETDATE());
  
  IF (MONTH(@birth_date) > MONTH(GETDATE()))
     OR (MONTH(@birth_date) = MONTH(GETDATE()) AND DAY(@birth_date) > DAY(GETDATE()))
    SET @age = @age - 1;
    
  RETURN @age;
END;
GO

-- Step 17: Create new procedure with English name
CREATE PROCEDURE create_appointment
  @patient_id INT,
  @doctor_id INT,
  @appointment_date DATE,
  @start_time TIME,
  @end_time TIME
AS
BEGIN
  SET NOCOUNT ON;
  BEGIN TRANSACTION;

  IF EXISTS (
    SELECT 1 FROM appointments WITH (UPDLOCK)
    WHERE doctor_id = @doctor_id
      AND appointment_date = @appointment_date
      AND status = 'SCHEDULED'
      AND (@start_time < end_time AND @end_time > start_time)
  )
  BEGIN
    ROLLBACK;
    THROW 50001, 'Doctor already has an appointment scheduled at this time.', 1;
  END

  INSERT INTO appointments (patient_id, doctor_id, appointment_date, start_time, end_time, status)
  VALUES (@patient_id, @doctor_id, @appointment_date, @start_time, @end_time, 'SCHEDULED');

  COMMIT;
END;
GO

-- Step 18: Create new trigger with English name
CREATE TRIGGER trg_medical_record_audit_update
ON medical_records
AFTER UPDATE
AS
BEGIN
  INSERT INTO medical_record_audit (
    record_id, old_anamnesis, new_anamnesis, old_diagnosis, new_diagnosis,
    old_prescription, new_prescription, change_date
  )
  SELECT
    d.record_id, d.anamnesis, i.anamnesis, d.diagnosis, i.diagnosis,
    d.prescription, i.prescription, SYSDATETIME()
  FROM deleted d
  JOIN inserted i ON d.record_id = i.record_id
  WHERE ISNULL(d.anamnesis,'') <> ISNULL(i.anamnesis,'')
     OR ISNULL(d.diagnosis,'') <> ISNULL(i.diagnosis,'')
     OR ISNULL(d.prescription,'') <> ISNULL(i.prescription,'');
END;
GO

-- Step 19: Recreate indexes with new names
CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX idx_audit_record_id ON medical_record_audit(record_id);
CREATE INDEX idx_records_appointment_id ON medical_records(appointment_id);
GO

PRINT 'Migration completed successfully!';
