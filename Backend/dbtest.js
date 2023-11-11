// db.js - Setup your database connection here and export the 'db' object
const db = require('./services/db');

// userOperations.js - Contains your 'create' function
const userOperations = require('./services/users');

// testData.js or your_test_file.js - File where you test the 'create' function
const sampleCreateUser = {
  role: 'Patient',
  username: 'johndoe',
  first_name: 'John2',
  last_name: 'Doe',
  date_of_birth: '1990-01-01',
  email: 'john.doe@example.com',
  phone_number: '1234567890',
  num_measures: 'twelve'
};

// commented out for testing purposes
// userOperations.create(sampleCreateUser)
//   .then(result => console.log(result.message))
//   .catch(error => console.error(error));



// test update
const sampleUpdateUser = {
  role: 'Provider',
  username: 'johnathan',
  first_name: 'johnathan2',
  last_name: 'Doe',
  date_of_birth: '1990-01-01',
  email: 'bigpoopy@gmail.com',
  phone_number: '1234567890',
  num_measures: 'twelve',
  id: 2
};

// userOperations.update(sampleUpdateUser)
//   .then(result => console.log(result.message))
//   .catch(error => console.error(error));

const sampleDeleteUser = {
  role: 'Provider',
  username: 'johnathan',
  first_name: 'johnathan2',
  last_name: 'Doe',
  date_of_birth: '1990-01-01',
  email: 'bigpoopy@gmail.com',
  phone_number: '1234567890',
  num_measures: 'twelve',
  id: 2
};
// test delete
userOperations.remove(sampleDeleteUser)
  .then(result => console.log(result.message))
  .catch(error => console.error(error));



