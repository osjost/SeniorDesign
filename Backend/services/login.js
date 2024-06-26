const db = require('./db');
const bcrypt = require('bcrypt');
const jwt = require("jwt-simple");
const secret = "1z98AJf901JZAa";
const saltRounds = 10;

async function authenticate(username, password) {
    const result = await db.query(
        `SELECT password_hash, user_id FROM login WHERE username = ?;`,
        [username]
    );

    if (result.length > 0) {
        const user = result[0]; // user now contains { password_hash: '...', user_id: '...' }

        return new Promise((resolve, reject) => {
            bcrypt.compare(password, user.password_hash, function(err, compareResult) {
                if (err) {
                    // Handle bcrypt error
                    reject(err);
                } else if (compareResult) {
                    // Passwords match, create JWT
                    const payload = {
                        username: username,
                        userId: user.user_id,
                        exp: Math.floor(Date.now() / 1000) + (60 * 60) // Expires in 1 hour
                    };
                    const jwtString = jwt.encode(payload, secret);
                    resolve({ jwt: jwtString, user_id: user.user_id }); // Return the JWT and the user object
                } else {
                    // Passwords do not match
                    reject(new Error('Authentication failed'));
                }
            });
        });
    } else {
        throw new Error('User not found');
    }
}

module.exports = {
    authenticate
};
