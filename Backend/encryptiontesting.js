//Checking the crypto module
// const crypto = require('crypto');
// const algorithm = 'aes-256-cbc'; //Using AES encryption
// const key = crypto.randomBytes(32);
// const iv = crypto.randomBytes(16);


// console.log(key)
// console.log(iv)

// //Encrypting text
// function encrypt(text) {
//    let cipher = crypto.createCipheriv('aes-256-cbc', Buffer.from(key), iv);
//    let encrypted = cipher.update(text);
//    encrypted = Buffer.concat([encrypted, cipher.final()]);
//    return { iv: iv.toString('hex'), encryptedData: encrypted.toString('hex') };
// }

// // Decrypting text
// function decrypt(text) {
//    let iv = Buffer.from(text.iv, 'hex');
//    let encryptedText = Buffer.from(text.encryptedData, 'hex');
//    let decipher = crypto.createDecipheriv('aes-256-cbc', Buffer.from(key), iv);
//    let decrypted = decipher.update(encryptedText);
//    decrypted = Buffer.concat([decrypted, decipher.final()]);
//    return decrypted.toString();
// }

// // Text send to encrypt function
// var hw = encrypt("Welcome to Tutorials Point...")
// console.log(hw)
// console.log(decrypt(hw))


// internal functions for encryption
const crypto = require('crypto');
const algorithm = 'aes-256-ecb'; // Changed to ECB mode
const key = Buffer.from('fc5f09608fb7661b1eace0b37874cdb68a68f8cd80e63541a1a99d63a0cde375', 'hex');

// Encrypt text
function encrypt(text) {
   let cipher = crypto.createCipheriv(algorithm, key, null); // No IV needed
   let encrypted = cipher.update(text, 'utf8', 'hex');
   encrypted += cipher.final('hex');
   return encrypted;
}

// Decrypt text
function decrypt(text) {
   let decipher = crypto.createDecipheriv(algorithm, key, null); // No IV needed
   let decrypted = decipher.update(text, 'hex', 'utf8');
   decrypted += decipher.final('utf8');
   return decrypted;
}

console.log(encrypt('stuff'))
console.log(decrypt('2d218c89d95af951540a15d4a92a6368'))