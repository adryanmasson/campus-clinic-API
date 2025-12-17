CREATE DATABASE campus_clinic;

USE campus_clinic;

/* ============================
   TABLES
============================ */

CREATE TABLE specialties (
  specialty_id INT IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(MAX)
);

CREATE TABLE doctors (
  doctor_id INT IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  medical_license VARCHAR(20) NOT NULL UNIQUE,
  specialty_id INT NOT NULL,
  birth_date DATE NOT NULL,
  phone VARCHAR(17),
  active BIT NOT NULL DEFAULT 1,
  CONSTRAINT chk_doctor_birth_date CHECK (birth_date <= CAST(GETDATE() AS DATE)),
  CONSTRAINT fk_doctor_specialty FOREIGN KEY (specialty_id)
    REFERENCES specialties(specialty_id) ON UPDATE CASCADE
);

CREATE TABLE patients (
  patient_id INT IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  gender VARCHAR(10) NOT NULL CHECK (gender IN ('M','F','OTHER')),
  cpf VARCHAR(14) NOT NULL UNIQUE,
  birth_date DATE NOT NULL,
  phone VARCHAR(17),
  address VARCHAR(255),
  email VARCHAR(255) NOT NULL UNIQUE,
  CONSTRAINT chk_patient_birth_date CHECK (birth_date <= CAST(GETDATE() AS DATE))
);

CREATE TABLE appointments (
  appointment_id INT IDENTITY PRIMARY KEY,
  patient_id INT NOT NULL,
  doctor_id INT NOT NULL,
  appointment_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
    CHECK (status IN ('SCHEDULED','COMPLETED','CANCELLED')),
  CONSTRAINT chk_appointment_time CHECK (start_time < end_time),
  CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id)
    REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id)
    REFERENCES doctors(doctor_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE medical_records (
  record_id INT IDENTITY PRIMARY KEY,
  appointment_id INT NOT NULL,
  anamnesis VARCHAR(MAX),
  diagnosis VARCHAR(MAX),
  prescription VARCHAR(MAX),
  record_date DATE NOT NULL,
  CONSTRAINT chk_record_date CHECK (record_date <= CAST(GETDATE() AS DATE)),
  CONSTRAINT fk_record_appointment FOREIGN KEY (appointment_id)
    REFERENCES appointments(appointment_id)
    ON DELETE CASCADE
);

CREATE TABLE medical_record_audit (
  audit_id INT IDENTITY PRIMARY KEY,
  record_id INT NOT NULL,
  old_anamnesis VARCHAR(MAX),
  new_anamnesis VARCHAR(MAX),
  old_diagnosis VARCHAR(MAX),
  new_diagnosis VARCHAR(MAX),
  old_prescription VARCHAR(MAX),
  new_prescription VARCHAR(MAX),
  change_date DATETIME2 NOT NULL,
  CONSTRAINT fk_audit_record FOREIGN KEY (record_id)
    REFERENCES medical_records(record_id) ON DELETE CASCADE ON UPDATE CASCADE
);

GO

/* ============================
   FUNCTION
============================ */

CREATE FUNCTION calculate_age (@birth_date DATE)
RETURNS INT
AS
BEGIN
  DECLARE @age INT;

  SET @age = DATEDIFF(YEAR, @birth_date, GETDATE());

  IF (MONTH(@birth_date) > MONTH(GETDATE()))
     OR (MONTH(@birth_date) = MONTH(GETDATE())
       AND DAY(@birth_date) > DAY(GETDATE()))
    SET @age = @age - 1;

  RETURN @age;
END;
GO

/* ============================
   PROCEDURE
============================ */

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
    SELECT 1
    FROM appointments WITH (UPDLOCK)
    WHERE doctor_id = @doctor_id
      AND appointment_date = @appointment_date
      AND status = 'SCHEDULED'
      AND (
        (@start_time < end_time AND @end_time > start_time)
        )
  )
  BEGIN
    ROLLBACK;
    THROW 50001, 'Doctor already has an appointment scheduled at this time.', 1;
  END

  INSERT INTO appointments
    (patient_id, doctor_id, appointment_date, start_time, end_time, status)
  VALUES
    (@patient_id, @doctor_id, @appointment_date, @start_time, @end_time, 'SCHEDULED');

  COMMIT;
END;
GO

/* ============================
   TRIGGERS
============================ */

CREATE TRIGGER trg_medical_record_audit_update
ON medical_records
AFTER UPDATE
AS
BEGIN
  INSERT INTO medical_record_audit (
    record_id,
    old_anamnesis,
    new_anamnesis,
    old_diagnosis,
    new_diagnosis,
    old_prescription,
    new_prescription,
    change_date
  )
  SELECT
    d.record_id,
    d.anamnesis,
    i.anamnesis,
    d.diagnosis,
    i.diagnosis,
    d.prescription,
    i.prescription,
    SYSDATETIME()
  FROM deleted d
  JOIN inserted i ON d.record_id = i.record_id
  WHERE ISNULL(d.anamnesis,'') <> ISNULL(i.anamnesis,'')
     OR ISNULL(d.diagnosis,'') <> ISNULL(i.diagnosis,'')
     OR ISNULL(d.prescription,'') <> ISNULL(i.prescription,'');
END;
GO

-- Indexes for foreign keys (added to preserve performance of FK-based queries)
CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX idx_audit_record_id ON medical_record_audit(record_id);
CREATE INDEX idx_records_appointment_id ON medical_records(appointment_id);
