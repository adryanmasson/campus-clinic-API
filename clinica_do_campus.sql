-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 28, 2025 at 10:30 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "-03:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `clinica_do_campus`
--
CREATE DATABASE clinica_do_campus;

USE clinica_do_campus;

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `criar_consulta` (IN `p_id_paciente` INT, IN `p_id_medico` INT, IN `p_data_consulta` DATE, IN `p_hora_inicio` TIME, IN `p_hora_fim` TIME)   BEGIN
    -- Inicia transação
    START TRANSACTION;
    IF EXISTS (
        SELECT 1
        FROM consultas
        WHERE fk_id_medico = p_id_medico
          AND data_consulta = p_data_consulta
          AND status = 'AGENDADA'
          AND (
                (hora_inicio <= p_hora_inicio AND hora_fim > p_hora_inicio) OR
                (hora_inicio < p_hora_fim AND hora_fim >= p_hora_fim) OR
                (hora_inicio >= p_hora_inicio AND hora_fim <= p_hora_fim)
              )
        FOR UPDATE
    ) THEN
        -- Não pode agendar, gerando conflito, logo, reverte e sinaliza erro.
        ROLLBACK;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Erro: Médico já possui consulta agendada neste horário.';
    ELSE
        -- 2) Caso tudo ocorra bem (Sem conflito), uma nova consulta é inserida na tabela.
        INSERT INTO consultas (fk_id_paciente, fk_id_medico, data_consulta, hora_inicio, hora_fim, status)
        VALUES (p_id_paciente, p_id_medico, p_data_consulta, p_hora_inicio, p_hora_fim, 'AGENDADA');
        -- 3) Tudo ok, a transação é então confirmada.
        COMMIT;
    END IF;
END$$

--
-- Functions
--
CREATE DEFINER=`root`@`localhost` FUNCTION `calcular_idade` (`p_data_nascimento` DATE) RETURNS INT(11) DETERMINISTIC BEGIN
    DECLARE v_idade INT;
    
    SET v_idade = YEAR(CURDATE()) - YEAR(p_data_nascimento);
    
    -- Ajusta se ainda não fez aniversário neste ano e subtrai 1
    IF MONTH(CURDATE()) < MONTH(p_data_nascimento)
       OR (MONTH(CURDATE()) = MONTH(p_data_nascimento) AND DAY(CURDATE()) < DAY(p_data_nascimento)) THEN
        SET v_idade = v_idade - 1;
    END IF;
    
    RETURN v_idade;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `auditoria_prontuario`
--

CREATE TABLE `auditoria_prontuario` (
  `id_auditoria` int(11) NOT NULL,
  `fk_id_prontuario` int(11) NOT NULL,
  `anamnese_antiga` text DEFAULT NULL,
  `anamnese_nova` text DEFAULT NULL,
  `diagnostico_antigo` text DEFAULT NULL,
  `diagnostico_novo` text DEFAULT NULL,
  `prescricao_antiga` text DEFAULT NULL,
  `prescricao_nova` text DEFAULT NULL,
  `data_alteracao` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `auditoria_prontuario`
--

INSERT INTO `auditoria_prontuario` (`id_auditoria`, `fk_id_prontuario`, `anamnese_antiga`, `anamnese_nova`, `diagnostico_antigo`, `diagnostico_novo`, `prescricao_antiga`, `prescricao_nova`, `data_alteracao`) VALUES
(4, 1, 'Paciente com palpitações cardiacas.', ' Paciente com dor de cabeça intensa.', 'Hipertensão leve.', 'Enxaqueca', 'Prescrever medicação anti-hipertensiva.', 'Analgésicos', '2025-10-26 22:31:44');

-- --------------------------------------------------------

--
-- Table structure for table `consultas`
--

CREATE TABLE `consultas` (
  `id_consulta` int(11) NOT NULL,
  `fk_id_paciente` int(11) NOT NULL,
  `fk_id_medico` int(11) NOT NULL,
  `data_consulta` date NOT NULL,
  `hora_inicio` time NOT NULL,
  `hora_fim` time NOT NULL,
  `status` enum('AGENDADA','REALIZADA','CANCELADA') NOT NULL DEFAULT 'AGENDADA'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `consultas`
--

INSERT INTO `consultas` (`id_consulta`, `fk_id_paciente`, `fk_id_medico`, `data_consulta`, `hora_inicio`, `hora_fim`, `status`) VALUES
(1, 1, 1, '2025-10-20', '09:00:00', '09:30:00', 'REALIZADA'),
(2, 2, 2, '2025-10-21', '10:00:00', '10:30:00', 'REALIZADA'),
(3, 3, 3, '2025-10-22', '11:00:00', '11:30:00', 'CANCELADA'),
(4, 4, 4, '2025-10-23', '14:00:00', '14:30:00', 'REALIZADA'),
(8, 7, 5, '2025-10-24', '15:00:00', '15:30:00', 'REALIZADA'),
(9, 1, 2, '2025-10-30', '10:00:00', '11:00:00', 'AGENDADA'),
(10, 1, 2, '2025-10-30', '16:00:00', '17:00:00', 'AGENDADA'),
(11, 1, 2, '2025-10-15', '09:00:00', '10:00:00', 'AGENDADA'),
(12, 1, 3, '2025-09-28', '14:00:00', '15:00:00', 'AGENDADA'),
(13, 1, 2, '2025-08-10', '08:30:00', '09:00:00', ''),
(14, 1, 4, '2025-07-05', '15:00:00', '16:00:00', 'AGENDADA'),
(15, 1, 3, '2025-05-19', '10:30:00', '11:00:00', 'CANCELADA');

--
-- Triggers `consultas`
--
DELIMITER $$
CREATE TRIGGER `trg_consultas_valores_insert` BEFORE INSERT ON `consultas` FOR EACH ROW BEGIN
    IF NEW.hora_inicio >= NEW.hora_fim THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A hora de início deve ser antes da hora do fim da consulta. ';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_consultas_valores_update` BEFORE UPDATE ON `consultas` FOR EACH ROW BEGIN
    IF NEW.hora_inicio >= NEW.hora_fim THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A hora de início deve ser antes da hora do fim da consulta. ';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `especialidades`
--

CREATE TABLE `especialidades` (
  `id_especialidade` int(11) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `descricao` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `especialidades`
--

INSERT INTO `especialidades` (`id_especialidade`, `nome`, `descricao`) VALUES
(1, 'Cardiologia', 'Trata doenças do coração.'),
(2, 'Dermatologia', 'Cuida da pele, cabelos e unhas.'),
(3, 'Pediatria', 'Atendimento médico para crianças.'),
(4, 'Ortopedia', 'Tratamento de ossos e articulações.'),
(5, 'Neurologia', 'Trata doenças do sistema nervoso.');

-- --------------------------------------------------------

--
-- Table structure for table `medicos`
--

CREATE TABLE `medicos` (
  `id_medico` int(11) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `crm` varchar(20) NOT NULL CHECK (octet_length(`crm`) >= 6),
  `fk_id_especialidade` int(11) NOT NULL,
  `data_nascimento` date NOT NULL,
  `telefone` varchar(17) DEFAULT NULL CHECK (`telefone` is null or octet_length(`telefone`) >= 11),
  `ativo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `medicos`
--

INSERT INTO `medicos` (`id_medico`, `nome`, `crm`, `fk_id_especialidade`, `data_nascimento`, `telefone`, `ativo`) VALUES
(1, 'Dr. João Pereira', 'CRM123456', 1, '1975-03-15', '11955554444', 1),
(2, 'Dra. Fernanda Costa', 'CRM654321', 2, '1980-11-30', '11944443333', 0),
(3, 'Dr. Marcos Oliveira', 'CRM112233', 3, '1982-07-25', '11933332222', 1),
(4, 'Dra. Paula Martins', 'CRM445566', 4, '1978-02-18', '11922221111', 1),
(5, 'Dr. Ricardo Almeida', 'CRM778899', 5, '1985-12-05', '11911112222', 1),
(6, 'Dr. João Gomes', 'CRM123789', 2, '1971-03-15', '11955554422', 1),
(7, 'Dr. Lucas Costa', 'CRM654567', 2, '1983-11-30', '11944443344', 1),
(8, 'Dr. Vitor Oliveira', 'CRM112098', 2, '1985-07-25', '11933332255', 1);

--
-- Triggers `medicos`
--
DELIMITER $$
CREATE TRIGGER `trg_medicos_data_nascimento_insert` BEFORE INSERT ON `medicos` FOR EACH ROW BEGIN
    IF NEW.data_nascimento > CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A data de nascimento não pode ser futura. ';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_medicos_data_nascimento_update` BEFORE UPDATE ON `medicos` FOR EACH ROW BEGIN
    IF NEW.data_nascimento > CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A data de nascimento não pode ser futura. ';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `pacientes`
--

CREATE TABLE `pacientes` (
  `id_paciente` int(11) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `sexo` enum('M','F','OUTRO') NOT NULL,
  `cpf` varchar(14) NOT NULL CHECK (octet_length(`cpf`) >= 11),
  `data_nascimento` date NOT NULL,
  `telefone` varchar(17) DEFAULT NULL,
  `logradouro` varchar(255) DEFAULT NULL CHECK (`logradouro` is null or octet_length(`logradouro`) >= 3),
  `email` varchar(255) NOT NULL
) ;

--
-- Dumping data for table `pacientes`
--

INSERT INTO `pacientes` (`id_paciente`, `nome`, `sexo`, `cpf`, `data_nascimento`, `telefone`, `logradouro`, `email`) VALUES
(1, 'Ana Silva', 'F', '123.456.789-01', '1990-05-12', '11922223333', 'Rua das Flores, 123', 'ana.silva@email.com'),
(2, 'Carlos Souza', 'M', '234.567.890-12', '1985-08-20', '11988887777', 'Av. Paulista, 1500', 'carlos.souza@email.com'),
(3, 'Beatriz Lima', 'F', '345.678.901-23', '2000-01-10', '11977776666', 'Rua do Sol, 45', 'beatriz.lima@email.com'),
(4, 'Eduardo Mendes', 'M', '456.789.012-34', '1995-03-05', '11966665555', 'Rua das Acácias, 210', 'eduardo.mendes@email.com'),
(7, 'Fernanda Rocha', 'F', '567.890.123-45', '1992-07-22', '11955554444', 'Av. Brasil, 300', 'fernanda.rocha@email.com');

--
-- Triggers `pacientes`
--
DELIMITER $$
CREATE TRIGGER `trg_pacientes_data_nascimento_insert` BEFORE INSERT ON `pacientes` FOR EACH ROW BEGIN
    IF NEW.data_nascimento > CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A data de nascimento não pode ser futura. ';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_pacientes_data_nascimento_update` BEFORE UPDATE ON `pacientes` FOR EACH ROW BEGIN
    IF NEW.data_nascimento > CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A data de nascimento não pode ser futura. ';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `prontuarios`
--

CREATE TABLE `prontuarios` (
  `id_prontuario` int(11) NOT NULL,
  `fk_id_consulta` int(11) NOT NULL,
  `anamnese` text DEFAULT NULL,
  `diagnostico` text DEFAULT NULL,
  `prescricao` text DEFAULT NULL,
  `data_registro` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `prontuarios`
--

INSERT INTO `prontuarios` (`id_prontuario`, `fk_id_consulta`, `anamnese`, `diagnostico`, `prescricao`, `data_registro`) VALUES
(1, 1, ' Paciente com dor de cabeça intensa.', 'Enxaqueca', 'Analgésicos', '2025-10-20'),
(2, 2, 'Paciente apresenta lesões na pele.', 'Dermatite de contato.', 'Prescrever pomada anti-inflamatória.', '2025-10-21'),
(3, 3, 'Criança com febre e tosse.', 'Gripe comum.', 'Reposição de líquidos e antitérmico.', '2025-10-22'),
(4, 4, 'Paciente com dor no joelho após queda.', 'Entorse leve.', 'Prescrever anti-inflamatório e repouso.', '2025-10-23'),
(7, 8, 'Paciente com episódios de dor de cabeça intensa.', 'Enxaqueca.', 'Prescrever analgésico específico e acompanhamento.', '2025-10-24');

--
-- Triggers `prontuarios`
--
DELIMITER $$
CREATE TRIGGER `trg_auditoria_prontuario_update` AFTER UPDATE ON `prontuarios` FOR EACH ROW BEGIN
    IF OLD.anamnese <> NEW.anamnese
       OR OLD.diagnostico <> NEW.diagnostico
       OR OLD.prescricao <> NEW.prescricao THEN
        INSERT INTO auditoria_prontuario (
            fk_id_prontuario,
            anamnese_antiga,
            anamnese_nova,
            diagnostico_antigo,
            diagnostico_novo,
            prescricao_antiga,
            prescricao_nova,
            data_alteracao
        ) VALUES (
            OLD.id_prontuario,
            OLD.anamnese,
            NEW.anamnese,
            OLD.diagnostico,
            NEW.diagnostico,
            OLD.prescricao,
            NEW.prescricao,
            NOW()
        );
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_prontuarios_data_registro_insert` BEFORE INSERT ON `prontuarios` FOR EACH ROW BEGIN
    IF NEW.data_registro > CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A data de registro não pode ser futura. ';
     END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_prontuarios_data_registro_update` BEFORE UPDATE ON `prontuarios` FOR EACH ROW BEGIN
    IF NEW.data_registro > CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A data de registro não pode ser futura. ';
    END IF;
END
$$
DELIMITER ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `auditoria_prontuario`
--
ALTER TABLE `auditoria_prontuario`
  ADD PRIMARY KEY (`id_auditoria`),
  ADD KEY `fk_id_prontuario` (`fk_id_prontuario`);

--
-- Indexes for table `consultas`
--
ALTER TABLE `consultas`
  ADD PRIMARY KEY (`id_consulta`),
  ADD KEY `fk_id_medico` (`fk_id_medico`),
  ADD KEY `idx_consultas_paciente_data` (`fk_id_paciente`,`data_consulta`),
  ADD KEY `idx_consultas_data` (`data_consulta`);

--
-- Indexes for table `especialidades`
--
ALTER TABLE `especialidades`
  ADD PRIMARY KEY (`id_especialidade`),
  ADD UNIQUE KEY `nome` (`nome`);

--
-- Indexes for table `medicos`
--
ALTER TABLE `medicos`
  ADD PRIMARY KEY (`id_medico`),
  ADD UNIQUE KEY `crm` (`crm`),
  ADD KEY `idx_medicos_especialidade` (`fk_id_especialidade`);

--
-- Indexes for table `pacientes`
--
ALTER TABLE `pacientes`
  ADD PRIMARY KEY (`id_paciente`),
  ADD UNIQUE KEY `cpf` (`cpf`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `prontuarios`
--
ALTER TABLE `prontuarios`
  ADD PRIMARY KEY (`id_prontuario`),
  ADD KEY `fk_id_consulta` (`fk_id_consulta`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `auditoria_prontuario`
--
ALTER TABLE `auditoria_prontuario`
  MODIFY `id_auditoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `consultas`
--
ALTER TABLE `consultas`
  MODIFY `id_consulta` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `especialidades`
--
ALTER TABLE `especialidades`
  MODIFY `id_especialidade` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `medicos`
--
ALTER TABLE `medicos`
  MODIFY `id_medico` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `pacientes`
--
ALTER TABLE `pacientes`
  MODIFY `id_paciente` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `prontuarios`
--
ALTER TABLE `prontuarios`
  MODIFY `id_prontuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `auditoria_prontuario`
--
ALTER TABLE `auditoria_prontuario`
  ADD CONSTRAINT `auditoria_prontuario_ibfk_1` FOREIGN KEY (`fk_id_prontuario`) REFERENCES `prontuarios` (`id_prontuario`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `consultas`
--
ALTER TABLE `consultas`
  ADD CONSTRAINT `consultas_ibfk_1` FOREIGN KEY (`fk_id_paciente`) REFERENCES `pacientes` (`id_paciente`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `consultas_ibfk_2` FOREIGN KEY (`fk_id_medico`) REFERENCES `medicos` (`id_medico`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `medicos`
--
ALTER TABLE `medicos`
  ADD CONSTRAINT `medicos_ibfk_1` FOREIGN KEY (`fk_id_especialidade`) REFERENCES `especialidades` (`id_especialidade`) ON UPDATE CASCADE;

--
-- Constraints for table `prontuarios`
--
ALTER TABLE `prontuarios`
  ADD CONSTRAINT `prontuarios_ibfk_1` FOREIGN KEY (`fk_id_consulta`) REFERENCES `consultas` (`id_consulta`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
