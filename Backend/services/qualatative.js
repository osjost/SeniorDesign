const db = require('./db');


// user_id INT,
// nausea INT,
// fatigue INT,
// pain INT,
// rash BOOL,
// other VARCHAR(255),

async function create(qualatative){
    const result = await db.query(
      `INSERT INTO qualatative_data 
      (user_id, nausea, fatigue, pain, rash, other) 
      VALUES 
      (?, ?, ?,?,?,?)`,
      [qualatative.user_id,
        qualatative.nausea,
        qualatative.fatigue,
        qualatative.pain,
        qualatative.rash,
        qualatative.other
    ]
    );
  
    let message = 'Error in adding qualatative data';
  
    if (result.affectedRows) {
      message = 'Qualatative data added succesfully';
    }
  
    return {message};
  }

  async function getAll(userId) {
    const rows = await db.query(
        `SELECT * FROM qualatative_data WHERE user_id = ?;`,
        [userId]
    );

    console.log(rows);
    return rows;
}
  
  module.exports = {
    getAll,
    create
  }