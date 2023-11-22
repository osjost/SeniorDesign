// db.js - Setup your database connection here and export the 'db' object
const db = require('./services/db');

// userOperations.js - Contains your 'create' function
const userOperations = require('./services/users');

// testing for readings:
// curl -k -X POST https://localhost:443/readings -H "Content-Type: application/json" -d "{\"reading\":\"11.1\", \"sensor_id\":\"1\", \"user_id\":\"2\"}"

// curl -k "https://localhost:443/readings?userId=2&sensorId=1"




// testing for login
// curl -k -X POST https://localhost:443/login -H "Content-Type: application/json" -d "{\"password\":\"blah\", \"username\":\"user\"}"

// testing for register
// curl -k -X POST https://localhost:443/register -H "Content-Type: application/json" -d "{\"password\":\"blah\", \"username\":\"user\"}"




// testing for users
// test post
// curl -k -X POST https://localhost:443/users -H "Content-Type: application/json" -d "{\"role\":\"Patient\", \"first_name\":\"test\", \"last_name\":\"test\", \"date_of_birth\":\"1991-02-02\", \"email\":\"test@gmail.com\", \"phone_number\":\"12345678\", \"num_measures\":\"twelve\",\"middle_name\":\"test5\",\"ssn\":\"123-45-6789\"}"

// test put
// curl -k -X PUT https://localhost:443/users -H "Content-Type: application/json" -d "{\"role\":\"NEWROLEHAHA\", \"first_name\":\"test\", \"last_name\":\"test\", \"date_of_birth\":\"1991-02-02\", \"email\":\"test@gmail.com\", \"phone_number\":\"12345678\", \"num_measures\":\"twelve\",\"middle_name\":\"test5\",\"ssn\":\"123-45-6789\", \"user_id\":\"1\"}"

//test get
// curl -k https://localhost:443/users/1

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
userOperations.create(sampleCreateUser)
  .then(result => console.log(result.message))
  .catch(error => console.error(error));



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
// userOperations.remove(sampleDeleteUser)
//   .then(result => console.log(result.message))
//   .catch(error => console.error(error));



