const db = require('./db');
const helper = require('../helper');
const config = require('../config');


// internal functions for encryption
const crypto = require('crypto');
const algorithm = 'aes-256-ecb';
const key = Buffer.from('fc5f09608fb7661b1eace0b37874cdb68a68f8cd80e63541a1a99d63a0cde375', 'hex');

// Encrypt text
function encrypt(text) {
   let cipher = crypto.createCipheriv(algorithm, key, null);
   let encrypted = cipher.update(text, 'utf8', 'hex');
   encrypted += cipher.final('hex');
   return encrypted;
}

// Decrypt text
function decrypt(text) {
   let decipher = crypto.createDecipheriv(algorithm, key, null);
   let decrypted = decipher.update(text, 'hex', 'utf8');
   decrypted += decipher.final('utf8');
   return decrypted;
}

// decrypt entire user
function decryptUser(user) {
  // List of fields to decrypt
  const fieldsToDecrypt = [
    'role', 'first_name', 'last_name', 
    'middle_name', 'ssn', 'date_of_birth', 
    'email', 'phone_number', 'num_measures'
  ];

  // Decrypting the specified fields
  fieldsToDecrypt.forEach(field => {
    if (user[field]) {
      console.log(user[field]);
      user[field] = decrypt(user[field]);
      console.log('decrypt check')
    }
  });


  return user;
}


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

