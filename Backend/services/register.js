const db = require('./db');
const bcrypt = require('bcrypt');
const jwt = require("jwt-simple");
const saltRounds = 10;

const secret = "1z98AJf901JZAa"

// only expects the username and password to add it to the login/authentication table
async function register(user){
        
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

        if (result.affectedRows) {
            return jwt.encode({ username: user.username}, secret);
        }
    }



// app.post('/register', (req, res) => {
//     bcrypt.hash(req.body.password, saltRounds, function(err, hash) {
//         // Store hash in your password DB.
//         // Save the hash to MySQL database
//     });
// });

module.exports = {
    register
  }