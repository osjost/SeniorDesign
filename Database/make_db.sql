CREATE DATABASE cytocheck;

CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    role ENUM('Provider', 'Patient'),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    middle_name VARCHAR(255),
    ssn VARCHAR(15),
    date_of_birth DATE,
    email VARCHAR(255),
    phone_number VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    num_measures VARCHAR(255)
);

CREATE TABLE Sensor_Metadata (
    sensor_id INT AUTO_INCREMENT PRIMARY KEY,
    sensor_brand VARCHAR(255),
    units VARCHAR(255),
    sensor_type VARCHAR(255)
);

CREATE TABLE Readings (
    reading_id INT AUTO_INCREMENT PRIMARY KEY,
    reading FLOAT,
    sensor_id INT,
    user_id INT,
    time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Login (
    username  VARCHAR(255),
    password_hash VARCHAR(255),
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    failed_login_attempts INT DEFAULT 0
);

CREATE TABLE Qualatative_Data (
    user_id INT,
    nausea INT,
    fatigue INT,
    pain INT,
    rash BOOL,
    other VARCHAR(255),
    time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Provider_Patient_Associations (
    provider_id INT,
    patient_id INT
);

CREATE TABLE Sensors_Per_Patient (
    patient_id INT,
    sensor_id INT
);
