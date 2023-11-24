const db = require('./db');
const { encrypt, decrypt, decryptUser } = require('./encryption');
const bcrypt = require('bcrypt');
const jwt = require("jwt-simple");
const saltRounds = 10;

const secret = "1z98AJf901JZAa"

// only expects the username and password to add it to the login/authentication table
async function register(user){
        
        console.log(user);
        const passwordHash = await  bcrypt.hash(user.password, saltRounds);

        // confirm username is not already being used
        const noDuplicate = await db.query(
            `SELECT * FROM login WHERE username = ?;`,
            [user.username]
        );

        if (noDuplicate.length > 0) {
            throw new Error('Username taken');
        }

        const result = await db.query(
            `INSERT INTO login (username, password_hash) VALUES (?, ?)`,
            [user.username, passwordHash]
        );

        const resultUserInsert = await db.query(
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



        // if (result.affectedRows) {
        //     return jwt.encode({ username: user.username}, secret);
        // }
    // }



// app.post('/register', (req, res) => {
//     bcrypt.hash(req.body.password, saltRounds, function(err, hash) {
//         // Store hash in your password DB.
//         // Save the hash to MySQL database
//     });
// });

module.exports = {
    register
  }