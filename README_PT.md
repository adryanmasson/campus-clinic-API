# 🏥 Campus Clinic - Sistema de Gestão Médica

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen?style=for-the-badge&logo=spring)
![SQL Server](https://img.shields.io/badge/SQL%20Server-2019+-CC2927?style=for-the-badge&logo=microsoft-sql-server)
![Azure](https://img.shields.io/badge/Azure-Deployed-0078D4?style=for-the-badge&logo=microsoft-azure)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**Sistema completo de gestão de clínicas médicas com API RESTful, agendamento de consultas, prontuário eletrônico e auditoria automatizada.**

[🌐 Demo Online](https://clinica-api-adryan.azurewebsites.net/api/specialties) | [📖 Documentação](#-endpoints-da-api) | [🚀 Deploy](#-azure-deployment)

> ⚠️ **Aviso:** Demo hospedada no plano gratuito Azure App Service Free (F1) - pode levar 30-60 segundos para ativar na primeira requisição ou estar temporariamente offline devido à cota de 60 min/dia de CPU. Para testes locais, veja [Executando Localmente](#-executando-localmente).

</div>

---

## 📋 Sobre o Projeto

Sistema completo de gestão para clínicas médicas, desenvolvido com **Spring Boot** e **SQL Server**, oferecendo controle total sobre:

- 👨‍⚕️ **Gestão de Médicos e Especialidades**
- 👤 **Cadastro de Pacientes e Histórico Médico**
- 📅 **Agendamento e Gerenciamento de Consultas**
- 📝 **Prontuário Eletrônico com Trilha de Auditoria Automática**
- 🔍 **Consultas Avançadas e Relatórios**

### 🎯 Destaques Técnicos

- ✅ **Arquitetura RESTful** com padrões de resposta padronizados
- ✅ **Código 100% em Inglês** - todas as classes, métodos e endpoints em inglês
- ✅ **Stored Procedures e Functions Nativas** em SQL Server
- ✅ **Triggers de Auditoria** para rastreamento de alterações em prontuários
- ✅ **Deploy Automatizado** via GitHub Actions para Azure App Service
- ✅ **Validações de Negócio** em múltiplas camadas (Banco de Dados + Aplicação)
- ✅ **Spring Security** configurado com CORS para integração com frontend

---

## 🛠️ Stack Tecnológico

### Backend
- **Java 17** - LTS com recursos modernos
- **Spring Boot 3.4.5** - Framework principal
- **Spring Data JPA** - Persistência e ORM
- **Spring Security** - Autenticação e autorização
- **Maven** - Gerenciamento de dependências

### Banco de Dados
- **Microsoft SQL Server** - Banco de dados principal
- **T-SQL** - Procedures, functions e triggers customizados
- **Azure SQL Database** - Hospedagem em nuvem

### DevOps & Cloud
- **Azure App Service** - Hospedagem da aplicação
- **GitHub Actions** - CI/CD automatizado
- **Azure CLI** - Gerenciamento de infraestrutura

---

## 🏗️ Arquitetura

```
┌─────────────────┐
│   Frontend      │
│  (Angular/React)│
└────────┬────────┘
         │ HTTPS
         ▼
┌─────────────────────────────────────────┐
│         Spring Boot REST API            │
├─────────────────────────────────────────┤
│  ┌──────────────┐  ┌─────────────────┐ │
│  │ Controllers  │  │  Security       │ │
│  │  (REST)      │  │  (CORS/Auth)    │ │
│  └──────┬───────┘  └─────────────────┘ │
│         │                                │
│  ┌──────▼───────────────────────────┐  │
│  │       Services Layer             │  │
│  │  (Lógica de Negócio)             │  │
│  └──────┬───────────────────────────┘  │
│         │                                │
│  ┌──────▼───────────────────────────┐  │
│  │    Repositories (JPA/Hibernate)  │  │
│  └──────┬───────────────────────────┘  │
└─────────┼───────────────────────────────┘
          │ JDBC
          ▼
┌─────────────────────────────────────────┐
│      SQL Server Database (Azure)        │
├─────────────────────────────────────────┤
│  • Tables: specialties, doctors,        │
│    patients, appointments,              │
│    medical_records, medical_record_audit│
│  • Functions: calculate_age             │
│  • Procedures: create_appointment       │
│  • Triggers: trg_medical_record_audit   │
└─────────────────────────────────────────┘
```

---

## 📚 Endpoints da API

### URL Base
```
https://clinica-api-adryan.azurewebsites.net
```

Todos os endpoints são prefixados automaticamente com `/api` pela configuração `spring.mvc.servlet.path=/api` no application.properties.

### 🩺 Especialidades Médicas

#### Listar Todas as Especialidades
```http
GET /api/specialties
```

**Resposta (200 OK)**
```json
{
  "status": "success",
  "message": "Specialties listed successfully.",
  "data": [
    {
      "specialtyId": 1,
      "name": "General Medicine",
      "description": "Treatment of common diseases and general health"
    }
  ]
}
```

#### Buscar Especialidade por ID
```http
GET /api/specialties/{id}
```

#### Criar Nova Especialidade
```http
POST /api/specialties
Content-Type: application/json

{
  "name": "Cardiology",
  "description": "Treatment of heart and cardiovascular system"
}
```

#### Atualizar Especialidade
```http
PUT /api/specialties/{id}
Content-Type: application/json

{
  "name": "Clinical Cardiology",
  "description": "Updated description"
}
```

#### Deletar Especialidade
```http
DELETE /api/specialties/{id}
```

---

### 👨‍⚕️ Médicos

#### Listar Todos os Médicos
```http
GET /api/doctors
```

**Resposta (200 OK)**
```json
{
  "status": "success",
  "message": "Doctors listed successfully.",
  "data": [
    {
      "doctorId": 1,
      "name": "Dr. James Anderson",
      "medicalLicense": "CRM123456",
      "specialty": {
        "specialtyId": 1,
        "name": "General Medicine"
      },
      "birthDate": "1975-04-20",
      "phone": "11912345678",
      "active": true
    }
  ]
}
```

#### Buscar Médico por ID
```http
GET /api/doctors/{id}
```

#### Buscar Médicos por Especialidade
```http
GET /api/doctors?specialty={specialty_id}
```

#### Cadastrar Novo Médico
```http
POST /api/doctors
Content-Type: application/json

{
  "name": "Dr. James Anderson",
  "medicalLicense": "CRM123456",
  "specialty": {
    "specialtyId": 1
  },
  "birthDate": "1975-04-20",
  "phone": "11912345678"
}
```

#### Atualizar Médico
```http
PUT /api/doctors/{id}
Content-Type: application/json

{
  "name": "Dr. James Anderson Jr.",
  "phone": "11999999999"
}
```

#### Verificar Disponibilidade do Médico
```http
GET /api/doctors/{id}/availability?date={yyyy-MM-dd}
```

**Resposta**: Horários disponíveis para consultas

---

### 👤 Pacientes

#### Listar Todos os Pacientes
```http
GET /api/patients
```

**Resposta (200 OK)**
```json
{
  "status": "success",
  "message": "Patients listed successfully.",
  "data": [
    {
      "patientId": 1,
      "name": "John Smith",
      "gender": "M",
      "cpf": "12345678901",
      "birthDate": "1990-05-15",
      "phone": "11987654321",
      "address": "123 Main St",
      "email": "john.smith@email.com"
    }
  ]
}
```

#### Buscar Paciente por ID
```http
GET /api/patients/{id}
```

#### Buscar Paciente por CPF
```http
GET /api/patients/cpf/{cpf}
```

#### Cadastrar Novo Paciente
```http
POST /api/patients
Content-Type: application/json

{
  "name": "John Smith",
  "gender": "M",
  "cpf": "12345678901",
  "birthDate": "1990-05-15",
  "phone": "11987654321",
  "address": "123 Main St",
  "email": "john.smith@email.com"
}
```

#### Atualizar Paciente
```http
PUT /api/patients/{id}
Content-Type: application/json

{
  "phone": "11999999999",
  "email": "new.email@email.com"
}
```

#### Obter Histórico do Paciente
```http
GET /api/patients/{id}/history
```

**Resposta**: Histórico médico completo com consultas e prontuários

---

### 📅 Consultas

#### Listar Todas as Consultas
```http
GET /api/appointments
```

**Resposta (200 OK)**
```json
{
  "status": "success",
  "message": "Appointments listed successfully.",
  "data": [
    {
      "id": 1,
      "patientName": "John Smith",
      "doctorName": "Dr. James Anderson",
      "appointmentDate": "2025-01-15",
      "startTime": "09:00:00",
      "endTime": "09:30:00",
      "status": "SCHEDULED"
    }
  ]
}
```

#### Buscar Consultas por Paciente
```http
GET /api/appointments?patient={patient_id}
```

#### Buscar Consultas por Médico
```http
GET /api/appointments?doctor={doctor_id}
```

#### Buscar Consultas por Data
```http
GET /api/appointments?date={yyyy-MM-dd}
```

#### Buscar Consultas por Status
```http
GET /api/appointments?status={SCHEDULED|COMPLETED|CANCELLED}
```

#### Agendar Nova Consulta
```http
POST /api/appointments/schedule
Content-Type: application/json

{
  "patientId": 1,
  "doctorId": 1,
  "appointmentDate": "2025-01-15",
  "startTime": "09:00:00",
  "endTime": "09:30:00"
}
```

#### Atualizar Consulta
```http
PUT /api/appointments/{id}
Content-Type: application/json

{
  "appointmentDate": "2025-01-16",
  "startTime": "10:00:00"
}
```

#### Cancelar Consulta
```http
DELETE /api/appointments/{id}
```

> **Nota**: O cancelamento altera o status para CANCELLED e preserva o registro para auditoria

---

### 📝 Prontuários

#### Listar Todos os Prontuários
```http
GET /api/medical-records
```

**Resposta (200 OK)**
```json
{
  "status": "success",
  "message": "Medical records returned successfully.",
  "data": [
    {
      "recordId": 1,
      "appointmentId": 1,
      "patientName": "John Smith",
      "doctorName": "Dr. James Anderson",
      "anamnesis": "Patient reports headache and fever for 2 days",
      "diagnosis": "Viral infection - Common cold",
      "prescription": "Rest, hydration, Paracetamol 500mg every 6 hours",
      "recordDate": "2024-12-10"
    }
  ]
}
```

#### Buscar Prontuário por ID
```http
GET /api/medical-records/{id}
```

#### Buscar Prontuário por Consulta
```http
GET /api/medical-records/appointment/{appointment_id}
```

#### Criar Novo Prontuário
```http
POST /api/medical-records
Content-Type: application/json

{
  "appointmentId": 1,
  "anamnesis": "Patient reports...",
  "diagnosis": "Diagnostic hypothesis...",
  "prescription": "Prescribed medication..."
}
```

#### Atualizar Prontuário
```http
PUT /api/medical-records/{id}
Content-Type: application/json

{
  "diagnosis": "Updated diagnosis",
  "prescription": "New prescription"
}
```

> **⚠️ Importante**: Todas as alterações em prontuários são automaticamente registradas na tabela de auditoria através de um trigger no banco de dados.

---

## 🗄️ Modelo de Dados

### Tabelas Principais

- **`specialties`** - Especialidades médicas (General Medicine, Cardiology, etc.)
- **`doctors`** - Cadastro de médicos com CRM e especialidade
- **`patients`** - Cadastro de pacientes com dados pessoais e de contato
- **`appointments`** - Agendamentos de consultas médicas
- **`medical_records`** - Prontuários eletrônicos vinculados a consultas
- **`medical_record_audit`** - Histórico de alterações em prontuários

### Functions e Procedures

#### Function: `dbo.calculate_age`
Calcula a idade de uma pessoa baseada na data de nascimento.

```sql
SELECT dbo.calculate_age('1990-05-15') AS age
-- Retorna: 35
```

#### Stored Procedure: `dbo.create_appointment`
Cria uma nova consulta com validações de negócio integradas.

```sql
EXEC create_appointment 
  @p_patient_id = 1,
  @p_doctor_id = 1,
  @p_appointment_date = '2025-01-15',
  @p_start_time = '10:00',
  @p_end_time = '10:30'
```

#### Trigger: `trg_medical_record_audit_update`
Registra automaticamente todas as alterações em prontuários na tabela de auditoria.

---

## 🚀 Executando Localmente

### Pré-requisitos

- ☕ **Java 17** ou superior
- 📦 **Maven 3.8+**
- 🗄️ **SQL Server 2019+** (ou LocalDB/Docker)
- 🔧 **IDE** (IntelliJ IDEA, Eclipse, VS Code)

### Passos

1. **Clone o repositório**
```bash
git clone https://github.com/adryanmasson/campus-clinic-api.git
cd campus-clinic-api
```

2. **Configure o banco de dados**

Crie um banco de dados SQL Server:
```sql
CREATE DATABASE campus_clinic;
```

Execute o script de schema:
```bash
sqlcmd -S localhost -d campus_clinic -i campus_clinic_schema.sql
```

(Opcional) Popule com dados de exemplo:
```bash
sqlcmd -S localhost -d campus_clinic -i sample_data_english.sql
```

3. **Configure as variáveis de ambiente**

A aplicação usa variáveis de ambiente para configuração do banco de dados. Configure-as no seu sistema:

**Windows (PowerShell):**
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:sqlserver://localhost:1433;database=campus_clinic;encrypt=false"
$env:SPRING_DATASOURCE_USERNAME="seu_usuario"
$env:SPRING_DATASOURCE_PASSWORD="sua_senha"
```

**Linux/Mac:**
```bash
export SPRING_DATASOURCE_URL="jdbc:sqlserver://localhost:1433;database=campus_clinic;encrypt=false"
export SPRING_DATASOURCE_USERNAME="seu_usuario"
export SPRING_DATASOURCE_PASSWORD="sua_senha"
```

**Alternativa**: Edite `src/main/resources/application.properties` e substitua `${...}` pelos valores reais:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;database=campus_clinic;encrypt=false
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

4. **Compile e execute**
```bash
mvn clean package
java -jar target/clinica-0.0.1-SNAPSHOT.jar
```

Ou rode diretamente com Maven:
```bash
mvn spring-boot:run
```

5. **Acesse a API**
```
http://localhost:8080/api/specialties
```

---

## ☁️ Azure Deployment

Este projeto está configurado para deploy automatizado no **Azure App Service** através de **GitHub Actions**.

### Configuração do CI/CD

O workflow `.github/workflows/main_clinica-api-adryan.yml` automatiza:

1. ✅ **Build** do projeto com Maven
2. ✅ **Empacotamento** como JAR executável
3. ✅ **Deploy** para Azure App Service
4. ✅ **Verificação** de saúde da aplicação

### Variáveis de Ambiente no Azure

Configure no Azure Portal (App Service → Configuration → Application settings):

```
SPRING_DATASOURCE_URL=jdbc:sqlserver://seu-servidor.database.windows.net:1433;database=campus_clinic;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
SPRING_DATASOURCE_USERNAME=seu_usuario
SPRING_DATASOURCE_PASSWORD=sua_senha
```

### Infraestrutura Azure

- **App Service**: Plano Gratuito F1 - Brazil South
- **Azure SQL Database**: GeneralPurpose Gen5 - West US 2
- **Database Server**: clinica-campus-banco.database.windows.net

### Comandos de Deploy (Azure CLI)

```bash
# Login no Azure
az login

# Atualizar string de conexão do banco
az webapp config appsettings set \
  --name clinica-api-adryan \
  --resource-group clinica-api-adryan_group \
  --settings SPRING_DATASOURCE_URL="jdbc:sqlserver://..."

# Reiniciar app service
az webapp restart \
  --name clinica-api-adryan \
  --resource-group clinica-api-adryan_group
```

---

## 📁 Estrutura do Projeto

```
campus-clinic-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/clinica/
│   │   │   ├── controllers/          # Endpoints REST
│   │   │   │   ├── SpecialtyController.java
│   │   │   │   ├── DoctorController.java
│   │   │   │   ├── PatientController.java
│   │   │   │   ├── AppointmentController.java
│   │   │   │   └── MedicalRecordController.java
│   │   │   ├── models/               # Entidades JPA
│   │   │   │   ├── Specialty.java
│   │   │   │   ├── Doctor.java
│   │   │   │   ├── Patient.java
│   │   │   │   ├── Appointment.java
│   │   │   │   ├── MedicalRecord.java
│   │   │   │   ├── AppointmentStatus.java
│   │   │   │   └── Gender.java
│   │   │   ├── repositories/         # Camada de acesso a dados
│   │   │   ├── services/             # Lógica de negócio
│   │   │   ├── dto/                  # Objetos de transferência
│   │   │   ├── exceptions/           # Tratamento de exceções
│   │   │   ├── SecurityConfig.java   # Configuração de segurança
│   │   │   └── WebConfig.java        # Configuração CORS
│   │   └── resources/
│   │       └── application.properties
│   └── test/                         # Testes unitários
├── campus_clinic_schema.sql          # Schema do banco
├── sample_data_english.sql           # Dados de exemplo
├── pom.xml                           # Dependências Maven
└── README.md                         # Este arquivo
```

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👨‍💻 Autor

**Adryan Masson**

- GitHub: [@adryanmasson](https://github.com/adryanmasson)
- LinkedIn: [Adryan Masson](https://linkedin.com/in/adryanmasson)
- Email: adryanpereiramasson@gmail.com

---

## 🙏 Agradecimentos

- Spring Boot Team pela excelente documentação
- Comunidade Microsoft Azure pelo suporte
- Colegas de curso pela colaboração e feedback

---

<div align="center">

**⭐ Se este projeto foi útil para você, considere dar uma estrela!**

Desenvolvido com ☕ e ❤️ por [Adryan Masson](https://github.com/adryanmasson)

</div>
