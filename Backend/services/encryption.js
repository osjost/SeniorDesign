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

module.exports = {
    encrypt,
    decrypt,
    decryptUser
 };