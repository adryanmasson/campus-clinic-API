# 🏥 Campus Clinic - Medical Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen?style=for-the-badge&logo=spring)
![SQL Server](https://img.shields.io/badge/SQL%20Server-2019+-CC2927?style=for-the-badge&logo=microsoft-sql-server)
![Azure](https://img.shields.io/badge/Azure-Deployed-0078D4?style=for-the-badge&logo=microsoft-azure)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**Complete medical clinic management system with RESTful API, appointment scheduling, electronic medical records, and automated audit logging.**

[🌐 Live Demo](https://clinica-api-adryan.azurewebsites.net/api/specialties) | [📖 Documentation](#-api-endpoints) | [🚀 Deploy](#-azure-deployment)

</div>

---

## 📋 About the Project

Complete management system for medical clinics, developed with **Spring Boot** and **SQL Server**, providing full control over:

- 👨‍⚕️ **Doctors and Medical Specialties Management**
- 👤 **Patient Registration and Medical History**
- 📅 **Appointment Scheduling and Management**
- 📝 **Electronic Medical Records with Automatic Audit Trail**
- 🔍 **Advanced Queries and Reports**

### 🎯 Technical Highlights

- ✅ **RESTful Architecture** with standardized response patterns
- ✅ **Complete Migration** from MySQL to SQL Server with optimizations
- ✅ **Native Stored Procedures & Functions** in SQL Server
- ✅ **Audit Triggers** for tracking medical record changes
- ✅ **Automated Deployment** via GitHub Actions to Azure App Service
- ✅ **Business Validations** at multiple layers (Database + Application)
- ✅ **Spring Security** configured with CORS for frontend integration

---

## 🛠️ Technology Stack

### Backend
- **Java 17** - LTS with modern features
- **Spring Boot 3.4.5** - Main framework
- **Spring Data JPA** - Persistence and ORM
- **Spring Security** - Authentication and authorization
- **Maven** - Dependency management

### Database
- **Microsoft SQL Server** - Primary database
- **T-SQL** - Custom procedures, functions, and triggers
- **Azure SQL Database** - Cloud hosting

### DevOps & Cloud
- **Azure App Service** - Application hosting
- **GitHub Actions** - Automated CI/CD
- **Azure CLI** - Infrastructure management

---

## 🏗️ Architecture

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
│  │  (Business Logic & Validation)   │  │
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

## 📚 API Endpoints

### Base URL
```
https://clinica-api-adryan.azurewebsites.net
```

### 🩺 Medical Specialties

#### List All Specialties
```http
GET /api/specialties
```

**Response (200 OK)**
```json
{
  "status": "success",
  "message": "Specialties listed successfully.",
  "data": [
    {
      "id_especialidade": 1,
      "nome": "Cardiology",
      "descricao": "Medical specialty that focuses on heart care"
    }
  ]
}
```

#### Get Specialty by ID
```http
GET /api/especialidades/{id}
```

#### Create New Specialty
```http
POST /api/especialidades
Content-Type: application/json

{
  "nome": "Cardiology",
  "descricao": "Medical specialty that focuses on heart care"
}
```

#### Update Specialty
```http
PUT /api/especialidades/{id}
Content-Type: application/json

{
  "nome": "Clinical Cardiology",
  "descricao": "Updated description"
}
```

#### Delete Specialty
```http
DELETE /api/especialidades/{id}
```

---

### 👨‍⚕️ Doctors

#### List All Doctors
```http
GET /api/medicos
```

**Response (200 OK)**
```json
{
  "status": "success",
  "message": "Doctors listed successfully.",
  "data": [
    {
      "id": 1,
      "nome": "Dr. John Silva",
      "crm": "CRM12345-SP",
      "especialidade": {
        "id": 1,
        "nome": "Cardiology"
      },
      "dataNascimento": "1980-05-15",
      "telefone": "(11) 98765-4321",
      "idade": 45,
      "ativo": true
    }
  ]
}
```

#### Get Doctor by ID
```http
GET /api/medicos/{id}
```

#### Get Doctors by Specialty
```http
GET /api/medicos?especialidade={especialidade_id}
```

#### Register New Doctor
```http
POST /api/medicos
Content-Type: application/json

{
  "nome": "Dr. John Silva",
  "crm": "CRM12345-SP",
  "fkIdEspecialidade": 1,
  "dataNascimento": "1980-05-15",
  "telefone": "(11) 98765-4321"
}
```

#### Update Doctor
```http
PUT /api/medicos/{id}
Content-Type: application/json

{
  "nome": "Dr. John Silva Jr.",
  "telefone": "(11) 98765-9999"
}
```

#### Activate/Deactivate Doctor
```http
PATCH /api/medicos/{id}/status
Content-Type: application/json

{
  "ativo": false
}
```

---

### 👤 Patients

#### List All Patients
```http
GET /api/pacientes
```

**Response (200 OK)**
```json
{
  "status": "success",
  "message": "Patients listed successfully.",
  "data": [
    {
      "id": 1,
      "nome": "Joseph Silva",
      "cpf": "123.456.789-00",
      "sexo": "M",
      "dataNascimento": "1995-01-10",
      "idade": 30,
      "telefone": "(11) 91234-5678",
      "email": "joseph.silva@email.com",
      "logradouro": "123 Flowers Street"
    }
  ]
}
```

#### Get Patient by ID
```http
GET /api/pacientes/{id}
```

#### Get Patient by CPF
```http
GET /api/pacientes/cpf/{cpf}
```

#### Register New Patient
```http
POST /api/pacientes
Content-Type: application/json

{
  "nome": "Joseph Silva",
  "cpf": "123.456.789-00",
  "sexo": "M",
  "dataNascimento": "1995-01-10",
  "telefone": "(11) 91234-5678",
  "email": "joseph.silva@email.com",
  "logradouro": "123 Flowers Street"
}
```

#### Update Patient
```http
PUT /api/pacientes/{id}
Content-Type: application/json

{
  "telefone": "(11) 99999-9999",
  "email": "new.email@email.com"
}
```

---

### 📅 Appointments

#### List All Appointments
```http
GET /api/consultas
```

**Response (200 OK)**
```json
{
  "status": "success",
  "message": "Appointments listed successfully.",
  "data": [
    {
      "id": 1,
      "paciente": {
        "id": 1,
        "nome": "Joseph Silva"
      },
      "medico": {
        "id": 1,
        "nome": "Dr. John Silva",
        "especialidade": "Cardiology"
      },
      "dataConsulta": "2025-12-20",
      "horaInicio": "09:00:00",
      "horaFim": "10:00:00",
      "status": "SCHEDULED"
    }
  ]
}
```

#### Get Appointments by Patient
```http
GET /api/consultas?paciente={paciente_id}
```

#### Get Appointments by Doctor
```http
GET /api/consultas?medico={medico_id}
```

#### Get Appointments by Date
```http
GET /api/consultas?data={yyyy-MM-dd}
```

#### Get Appointments by Status
```http
GET /api/consultas?status={AGENDADA|REALIZADA|CANCELADA}
```

#### Schedule New Appointment
```http
POST /api/consultas
Content-Type: application/json

{
  "fkIdPaciente": 1,
  "fkIdMedico": 1,
  "dataConsulta": "2025-12-20",
  "horaInicio": "09:00:00",
  "horaFim": "10:00:00"
}
```

#### Update Appointment Status
```http
PATCH /api/consultas/{id}/status
Content-Type: application/json

{
  "status": "REALIZADA"
}
```

#### Cancel Appointment
```http
DELETE /api/consultas/{id}
```

---

### 📝 Medical Records

#### List All Medical Records
```http
GET /api/prontuarios
```

**Response (200 OK)**
```json
{
  "status": "success",
  "message": "Medical records listed successfully.",
  "data": [
    {
      "id": 1,
      "consulta": {
        "id": 1,
        "data": "2025-12-20"
      },
      "anamnese": "Patient reports chest pain for 3 days",
      "diagnostico": "Suspected angina",
      "prescricao": "Rest and cardiac follow-up",
      "dataRegistro": "2025-12-17"
    }
  ]
}
```

#### Get Medical Record by ID
```http
GET /api/prontuarios/{id}
```

#### Get Complete Patient History
```http
GET /api/prontuarios/paciente/{paciente_id}
```

**Returns**: All appointments and medical records for the patient with doctor and specialty information.

#### Create New Medical Record
```http
POST /api/prontuarios
Content-Type: application/json

{
  "fkIdConsulta": 1,
  "anamnese": "Patient reports...",
  "diagnostico": "Diagnostic hypothesis...",
  "prescricao": "Prescribed medication..."
}
```

#### Update Medical Record
```http
PUT /api/prontuarios/{id}
Content-Type: application/json

{
  "diagnostico": "Updated diagnosis",
  "prescricao": "New prescription"
}
```

> **⚠️ Important**: All changes to medical records are automatically logged in the audit table through a database trigger.

---

## 🗄️ Data Model

### Main Tables

- **`specialties`** - Medical specialties (Cardiology, Dermatology, etc.)
- **`doctors`** - Doctor registry with medical license and specialty
- **`patients`** - Patient registry with personal and contact information
- **`appointments`** - Medical appointment bookings
- **`medical_records`** - Electronic medical records linked to appointments
- **`medical_record_audit`** - Medical record change history

### Functions and Procedures

#### Function: `dbo.calculate_age`
Calculates a person's age based on their date of birth.

```sql
SELECT dbo.calculate_age('1995-01-10') AS age
-- Returns: 30
```

#### Stored Procedure: `dbo.create_appointment`
Creates a new appointment with integrated business validations.

```sql
EXEC create_appointment 
  @patient_id = 1,
  @doctor_id = 1,
  @appointment_date = '2025-12-20',
  @start_time = '10:00',
  @end_time = '11:00'
```

#### Trigger: `trg_medical_record_audit_update`
Automatically logs all changes to medical records in the audit table.

---

## 🚀 Running Locally

### Prerequisites

- ☕ **Java 17** or higher
- 📦 **Maven 3.8+**
- 🗄️ **SQL Server 2019+** (or LocalDB)
- 🔧 **IDE** (IntelliJ IDEA, Eclipse, VS Code)

### Steps

1. **Clone the repository**
```bash
git clone https://github.com/adryanmasson/clinica-API.git
cd clinica-API
```

2. **Configure the database**

Create a SQL Server database:
```sql
CREATE DATABASE clinica_do_campus;
```

Execute the schema script:
```bash
sqlcmd -S localhost -d clinica_do_campus -i clinica_do_campus.sql
```

(Optional) Populate with sample data:
```bash
sqlcmd -S localhost -d clinica_do_campus -i dados_exemplo.sql
```

3. **Configure environment variables**

Create a `.env` file or configure in system:
```bash
DB_URL=jdbc:sqlserver://localhost:1433;database=clinica_do_campus;encrypt=false
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

4. **Build and run**
```bash
mvn clean package
java -jar target/clinica-0.0.1-SNAPSHOT.jar
```

5. **Access the API**
```
http://localhost:8080/api/especialidades
```

---

## ☁️ Azure Deployment

This project is configured for automated deployment to **Azure App Service** via **GitHub Actions**.

### CI/CD Configuration

The workflow `.github/workflows/main_clinica-api-adryan.yml` automates:

1. ✅ **Build** the project with Maven
2. ✅ **Automated Tests**
3. ✅ **Deploy** to Azure App Service
4. ✅ **Health Check** verification

### Azure Environment Variables

Configure in Azure Portal (App Service → Configuration):

```
DB_URL=jdbc:sqlserver://your-server.database.windows.net:1433;database=clinica_do_campus;encrypt=true
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

### Azure Infrastructure

- **App Service**: Free F1 Plan - Brazil South
- **Azure SQL Database**: GeneralPurpose Gen5 (2 vCores) - West US 2

---

## 🧪 Tests

### Run Tests
```bash
mvn test
```

### Test Coverage
```bash
mvn test jacoco:report
```

> **Note**: Integration tests with Testcontainers were removed due to Windows compatibility issues. Manual testing or Linux environment recommended for integration tests.

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Adryan Masson**

- GitHub: [@adryanmasson](https://github.com/adryanmasson)
- LinkedIn: [Adryan Masson](https://linkedin.com/in/adryanmasson)
- Email: adryanpereiramasson@gmail.com

---

## 🙏 Acknowledgments

- Spring Boot Team for excellent documentation
- Microsoft Azure Community for support
- Course colleagues for collaboration and feedback

---

<div align="center">

**⭐ If this project was useful to you, consider giving it a star!**

Developed with ☕ and ❤️ by [Adryan Masson](https://github.com/adryanmasson)

</div>
