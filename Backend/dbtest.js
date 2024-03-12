// db.js - Setup your database connection here and export the 'db' object
const db = require('./services/db');

// userOperations.js - Contains your 'create' function
const userOperations = require('./services/users');



// testing for associations
// NOTE: YOU PASS THE PROVIDER ID IN HERE!
// curl -k https://localhost:443/associations/1

// testing for inbox
// curl -k -X POST https://localhost:443/inbox -H "Content-Type: application/json" -d "{\"provider_id\":\"1\", \"message\":\"this is a test message\"}"
// curl -k https://localhost:443/inbox/1


// testing for qualitative
// curl -k -X POST https://localhost:443/qualitative -H "Content-Type: application/json" -d "{\"user_id\":\"1\", \"nausea\":\"1\", \"fatigue\":\"1\",\"pain\":\"1\",\"rash\":\"1\",\"other\":\"test\"}"
// curl -k https://localhost:443/qualitative/1



// testing for readings:
// curl -k -X POST https://localhost:443/readings -H "Content-Type: application/json" -d "{\"reading\":\"11.1\", \"sensor_id\":\"1\", \"user_id\":\"2\"}"

// curl -k https://localhost:443/readings/2/1

// readings test with JWT:
// curl -k -H "Authorization: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InVzZXIifQ.9b6LUDKCeDq14atPhQLgtpXKUUEHgVLXx-Nkrcb_pnI" https://localhost:443/readings/2/1



// testing for login
// curl -k -X POST https://localhost:443/login -H "Content-Type: application/json" -d "{\"password\":\"blah\", \"username\":\"user\"}"

// testing for login this time with JWT.





// testing for register
// curl -k -X POST https://localhost:443/register -H "Content-Type: application/json" -d "{\"password\":\"blah\", \"username\":\"zzaxa12cbdf111\",\"role\":\"patient\", \"first_name\":\"test\", \"last_name\":\"test\", \"date_of_birth\":\"1991-02-02\", \"email\":\"test@gmail.com\", \"phone_number\":\"12345678\", \"num_measures\":\"twelve\",\"middle_name\":\"test5\",\"ssn\":\"123-45-6789\"}"




// testing for users
// test post
// curl -k -X POST https://localhost:443/users -H "Content-Type: application/json" -d "{\"role\":\"Patient\", \"first_name\":\"test3\", \"last_name\":\"test\", \"date_of_birth\":\"1991-02-02\", \"email\":\"test@gmail.com\", \"phone_number\":\"12345678\", \"num_measures\":\"twelve\",\"middle_name\":\"test5\",\"ssn\":\"123-45-6789\"}"

//test get
// curl -k https://localhost:443/users/1

// test associatoins
// curl -k https://localhost:443/associations/1
// making a new associatino:
// curl -k -X POST https://localhost:443/associations -H "Content-Type: application/json" -d "{\"patient_id\":\"1\", \"provider_id\":\"2\"}"


// test put
// curl -k -X PUT https://localhost:443/users -H "Content-Type: application/json" -d "{\"role\":\"NEWROLEHAHA\", \"first_name\":\"test\", \"last_name\":\"test\", \"date_of_birth\":\"1991-02-02\", \"email\":\"test@gmail.com\", \"phone_number\":\"12345678\", \"num_measures\":\"twelve\",\"middle_name\":\"test5\",\"ssn\":\"123-45-6789\", \"user_id\":\"1\"}"

// test for threshold get and create
// curl -k -X POST https://localhost:443/threshold -H "Content-Type: application/json" -d "{\"patient_id\":\"1\", \"sensor_id\":\"1\", \"lower\":\"1\",\"upper\":\"2\" }"
// curl -k https://localhost:443/threshold/1/1
// curl -k -X POST https://localhost:443/threshold -H "Content-Type: application/json" -d "{\"patient_id\":\"1\", \"sensor_id\":\"1\", \"lower\":\"1\",\"upper\":\"2\" }"

// association create request
// curl -k -X POST https://localhost:443/inbox/associations -H "Content-Type: application/json" -d "{\"provider_id\":\"1\", \"message\":\"patient name goes here\",\"sender_id\":\"1\" }"

// association delete request
// curl -k -X DELETE https://localhost:443/associations/1


// curl -k -X DELETE https://localhost:443/inbox/1

// curl -k https://localhost:443/associations/1


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



