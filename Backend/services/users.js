const db = require('./db');
const helper = require('../helper');
const config = require('../config');

const { encrypt, decrypt, decryptUser } = require('./encryption');



async function getSingle(id){
  let row = await db.query(
    `SELECT * FROM users WHERE user_id = ?;`,
    [id]
  );
  
  row = decryptUser(row[0])
  return {
    row
  }
}

async function create(user){

  const result = await db.query(
    `INSERT INTO users 
    (role, first_name, last_name, middle_name, ssn, date_of_birth, email, phone_number, num_measures) 
    VALUES 
    (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
    [
      encrypt(user.role),
      encrypt(user.first_name),
      encrypt(user.last_name),
      encrypt(user.middle_name),
      encrypt(user.ssn),
      encrypt(user.date_of_birth),
      encrypt(user.email),
      encrypt(user.phone_number),
      encrypt(user.num_measures)
    ]
  );

  let message = 'Error in creating user';

  if (result.affectedRows) {
    message = 'User created succesfully';
  }

  return {message};
}

async function update(user){
  const result = await db.query(
    `UPDATE users
    SET
        role = ?,
        first_name = ?,
        last_name = ?,
        middle_name = ?,
        ssn = ?,
        date_of_birth = ?,
        email = ?,
        phone_number = ?,
        num_measures = ?
    WHERE user_id = ?;`,
    [
      encrypt(user.role),
      encrypt(user.first_name),
      encrypt(user.last_name),
      encrypt(user.middle_name),
      encrypt(user.ssn),
      encrypt(user.date_of_birth),
      encrypt(user.email),
      encrypt(user.phone_number),
      encrypt(user.num_measures),
      user.user_id
    ]
  );

  let message = 'Error in updating user';

  if (result.affectedRows) {
    message = 'User updated successfully';
  }

  return {message};
}

async function remove(user){
  const result = await db.query(
    `DELETE FROM users WHERE user_id= ?`,
    [user.user_id]
  );

  let message = 'Error in deleting user';

  if (result.affectedRows) {
    message = 'User deleted successfully';
  }

  return {message};
}


module.exports = {
  getSingle,
  create,
  update,
  remove
}

