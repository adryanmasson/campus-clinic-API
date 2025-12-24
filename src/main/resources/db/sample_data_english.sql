-- Sample data in English for Campus Clinic database

-- Insert specialties
INSERT INTO specialties (name, description) VALUES
('General Medicine', 'Treatment of common diseases and general health'),
('Cardiology', 'Treatment of heart and cardiovascular system'),
('Pediatrics', 'Medical care for infants, children and adolescents'),
('Orthopedics', 'Treatment of bones, joints and muscles'),
('Dermatology', 'Treatment of skin, hair and nails');

-- Insert patients
INSERT INTO patients (name, gender, cpf, birth_date, phone, address, email) VALUES
('John Smith', 'M', '12345678901', '1990-05-15', '11987654321', '123 Main St', 'john.smith@email.com'),
('Mary Johnson', 'F', '23456789012', '1985-08-22', '11976543210', '456 Oak Ave', 'mary.johnson@email.com'),
('Robert Brown', 'M', '34567890123', '2015-03-10', '11965432109', '789 Pine Rd', 'robert.brown@email.com'),
('Sarah Davis', 'F', '45678901234', '1978-11-30', '11954321098', '321 Elm St', 'sarah.davis@email.com'),
('Michael Wilson', 'M', '56789012345', '1995-07-18', '11943210987', '654 Maple Dr', 'michael.wilson@email.com');

-- Insert doctors
INSERT INTO doctors (name, medical_license, specialty_id, birth_date, phone, active) VALUES
('Dr. James Anderson', 'CRM123456', 1, '1975-04-20', '11912345678', 1),
('Dr. Patricia Martinez', 'CRM234567', 2, '1980-09-15', '11923456789', 1),
('Dr. William Garcia', 'CRM345678', 3, '1972-12-05', '11934567890', 1),
('Dr. Jennifer Rodriguez', 'CRM456789', 4, '1983-06-28', '11945678901', 1),
('Dr. Richard Lee', 'CRM567890', 5, '1978-02-14', '11956789012', 1);

-- Insert appointments
INSERT INTO appointments (patient_id, doctor_id, appointment_date, start_time, end_time, status) VALUES
(1, 1, '2025-01-15', '09:00:00', '09:30:00', 'SCHEDULED'),
(2, 2, '2025-01-15', '10:00:00', '10:30:00', 'SCHEDULED'),
(3, 3, '2025-01-16', '14:00:00', '14:30:00', 'SCHEDULED'),
(1, 1, '2024-12-10', '09:00:00', '09:30:00', 'COMPLETED'),
(4, 4, '2024-12-12', '15:00:00', '15:30:00', 'COMPLETED');

-- Insert medical records for completed appointments
INSERT INTO medical_records (appointment_id, anamnesis, diagnosis, prescription, record_date) VALUES
(4, 'Patient reports headache and fever for 2 days', 'Viral infection - Common cold', 'Rest, hydration, Paracetamol 500mg every 6 hours', '2024-12-10'),
(5, 'Patient complains of knee pain after sports activity', 'Minor ligament sprain', 'Ice compress, Rest for 7 days, Anti-inflammatory medication', '2024-12-12');