CREATE DATABASE cytocheck;

CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    role ENUM('Provider', 'Patient'),
    username VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
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
    time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sensor_id) REFERENCES Sensor_Metadata(sensor_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE Authentication (
    user_id INT PRIMARY KEY,
    password_hash VARCHAR(255),
    password_salt VARCHAR(255),
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    failed_login_attempts INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE Qualatative_Data (
    user_id INT,
    nausea INT,
    fatigue INT,
    pain INT,
    rash BOOL,
    other VARCHAR(255),
    time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE Provider_Patient_Associations (
    provider_id INT,
    patient_id INT,
    PRIMARY KEY (provider_id, patient_id),
    FOREIGN KEY (provider_id) REFERENCES Users(user_id),
    FOREIGN KEY (patient_id) REFERENCES Users(user_id)
);

CREATE TABLE Sensors_Per_Patient (
    patient_id INT,
    sensor_id INT,
    PRIMARY KEY (patient_id, sensor_id),
    FOREIGN KEY (patient_id) REFERENCES Users(user_id),
    FOREIGN KEY (sensor_id) REFERENCES Sensor_Metadata(sensor_id)
);
