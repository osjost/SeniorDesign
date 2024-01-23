-- Run this command first
CREATE DATABASE cytocheck;
-- then paste in and run all of these commands
USE cytocheck;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    middle_name VARCHAR(255),
    ssn VARCHAR(255),
    date_of_birth VARCHAR(255),
    email VARCHAR(255),
    phone_number VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    num_measures VARCHAR(255)
);

CREATE TABLE sensor_metadata (
    sensor_id INT AUTO_INCREMENT PRIMARY KEY,
    sensor_brand VARCHAR(255),
    units VARCHAR(255),
    sensor_type VARCHAR(255)
);

CREATE TABLE readings (
    reading_id INT AUTO_INCREMENT PRIMARY KEY,
    reading FLOAT,
    sensor_id INT,
    user_id INT,
    time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE login (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(255),
    password_hash VARCHAR(255),
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    failed_login_attempts INT DEFAULT 0
);

CREATE TABLE qualitative_data (
    user_id INT,
    nausea INT,
    fatigue INT,
    pain INT,
    rash VARCHAR(255),
    other VARCHAR(255),
    time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE provider_patient_associations (
    provider_id INT,
    patient_id INT
);

CREATE TABLE sensors_per_patient (
    patient_id INT,
    sensor_id INT
);

CREATE TABLE provider_inbox (
    provider_id INT,
    message VARCHAR(255),
    time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE threshold (
    patient_id INT,
    sensor_id INT,
    threshold VARCHAR(255)
);

-- to query a table since we're using command line you can do the following (substitute desired values in as needed)
SELECT provider_id, message, time_stamp
FROM Provider_Inbox
