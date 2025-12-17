CREATE DATABASE clinica_do_campus;

USE clinica_do_campus;

/* ============================
   TABELAS
============================ */

CREATE TABLE especialidades (
  id_especialidade INT IDENTITY PRIMARY KEY,
  nome VARCHAR(100) NOT NULL UNIQUE,
  descricao VARCHAR(MAX)
);

CREATE TABLE medicos (
  id_medico INT IDENTITY PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  crm VARCHAR(20) NOT NULL UNIQUE,
  fk_id_especialidade INT NOT NULL,
  data_nascimento DATE NOT NULL,
  telefone VARCHAR(17),
  ativo BIT NOT NULL DEFAULT 1,
  CONSTRAINT chk_medico_data CHECK (data_nascimento <= CAST(GETDATE() AS DATE)),
  CONSTRAINT fk_medico_especialidade FOREIGN KEY (fk_id_especialidade)
    REFERENCES especialidades(id_especialidade) ON UPDATE CASCADE
);

CREATE TABLE pacientes (
  id_paciente INT IDENTITY PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  sexo VARCHAR(10) NOT NULL CHECK (sexo IN ('M','F','OUTRO')),
  cpf VARCHAR(14) NOT NULL UNIQUE,
  data_nascimento DATE NOT NULL,
  telefone VARCHAR(17),
  logradouro VARCHAR(255),
  email VARCHAR(255) NOT NULL UNIQUE,
  CONSTRAINT chk_paciente_data CHECK (data_nascimento <= CAST(GETDATE() AS DATE))
);

CREATE TABLE consultas (
  id_consulta INT IDENTITY PRIMARY KEY,
  fk_id_paciente INT NOT NULL,
  fk_id_medico INT NOT NULL,
  data_consulta DATE NOT NULL,
  hora_inicio TIME NOT NULL,
  hora_fim TIME NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'AGENDADA'
    CHECK (status IN ('AGENDADA','REALIZADA','CANCELADA')),
  CONSTRAINT chk_hora_consulta CHECK (hora_inicio < hora_fim),
  CONSTRAINT fk_consulta_paciente FOREIGN KEY (fk_id_paciente)
    REFERENCES pacientes(id_paciente) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_consulta_medico FOREIGN KEY (fk_id_medico)
    REFERENCES medicos(id_medico) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE prontuarios (
  id_prontuario INT IDENTITY PRIMARY KEY,
  fk_id_consulta INT NOT NULL,
  anamnese VARCHAR(MAX),
  diagnostico VARCHAR(MAX),
  prescricao VARCHAR(MAX),
  data_registro DATE NOT NULL,
  CONSTRAINT chk_prontuario_data CHECK (data_registro <= CAST(GETDATE() AS DATE)),
  CONSTRAINT fk_prontuario_consulta FOREIGN KEY (fk_id_consulta)
    REFERENCES consultas(id_consulta)
    ON DELETE CASCADE
);

CREATE TABLE auditoria_prontuario (
  id_auditoria INT IDENTITY PRIMARY KEY,
  fk_id_prontuario INT NOT NULL,
  anamnese_antiga VARCHAR(MAX),
  anamnese_nova VARCHAR(MAX),
  diagnostico_antigo VARCHAR(MAX),
  diagnostico_novo VARCHAR(MAX),
  prescricao_antiga VARCHAR(MAX),
  prescricao_nova VARCHAR(MAX),
  data_alteracao DATETIME2 NOT NULL,
  CONSTRAINT fk_auditoria_prontuario FOREIGN KEY (fk_id_prontuario)
    REFERENCES prontuarios(id_prontuario) ON DELETE CASCADE ON UPDATE CASCADE
);

GO

/* ============================
   FUNCTION
============================ */

CREATE FUNCTION calcular_idade (@data_nascimento DATE)
RETURNS INT
AS
BEGIN
  DECLARE @idade INT;

  SET @idade = DATEDIFF(YEAR, @data_nascimento, GETDATE());

  IF (MONTH(@data_nascimento) > MONTH(GETDATE()))
     OR (MONTH(@data_nascimento) = MONTH(GETDATE())
       AND DAY(@data_nascimento) > DAY(GETDATE()))
    SET @idade = @idade - 1;

  RETURN @idade;
END;
GO

/* ============================
   PROCEDURE
============================ */

CREATE PROCEDURE criar_consulta
  @id_paciente INT,
  @id_medico INT,
  @data_consulta DATE,
  @hora_inicio TIME,
  @hora_fim TIME
AS
BEGIN
  SET NOCOUNT ON;
  BEGIN TRANSACTION;

  IF EXISTS (
    SELECT 1
    FROM consultas WITH (UPDLOCK)
    WHERE fk_id_medico = @id_medico
      AND data_consulta = @data_consulta
      AND status = 'AGENDADA'
      AND (
        (@hora_inicio < hora_fim AND @hora_fim > hora_inicio)
        )
  )
  BEGIN
    ROLLBACK;
    THROW 50001, 'Médico já possui consulta agendada neste horário.', 1;
  END

  INSERT INTO consultas
    (fk_id_paciente, fk_id_medico, data_consulta, hora_inicio, hora_fim, status)
  VALUES
    (@id_paciente, @id_medico, @data_consulta, @hora_inicio, @hora_fim, 'AGENDADA');

  COMMIT;
END;
GO

/* ============================
   TRIGGERS
============================ */

CREATE TRIGGER trg_auditoria_prontuario_update
ON prontuarios
AFTER UPDATE
AS
BEGIN
  INSERT INTO auditoria_prontuario (
    fk_id_prontuario,
    anamnese_antiga,
    anamnese_nova,
    diagnostico_antigo,
    diagnostico_novo,
    prescricao_antiga,
    prescricao_nova,
    data_alteracao
  )
  SELECT
    d.id_prontuario,
    d.anamnese,
    i.anamnese,
    d.diagnostico,
    i.diagnostico,
    d.prescricao,
    i.prescricao,
    SYSDATETIME()
  FROM deleted d
  JOIN inserted i ON d.id_prontuario = i.id_prontuario
  WHERE ISNULL(d.anamnese,'') <> ISNULL(i.anamnese,'')
     OR ISNULL(d.diagnostico,'') <> ISNULL(i.diagnostico,'')
     OR ISNULL(d.prescricao,'') <> ISNULL(i.prescricao,'');
END;
GO

-- Indexes for foreign keys (added to preserve performance of FK-based queries)
CREATE INDEX idx_consultas_fk_id_medico ON consultas(fk_id_medico);
CREATE INDEX idx_consultas_fk_id_paciente ON consultas(fk_id_paciente);
CREATE INDEX idx_auditoria_fk_prontuario ON auditoria_prontuario(fk_id_prontuario);
CREATE INDEX idx_prontuarios_fk_id_consulta ON prontuarios(fk_id_consulta);


