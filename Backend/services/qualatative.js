const db = require('./db');


// user_id INT,
// nausea INT,
// fatigue INT,
// pain INT,
// rash BOOL,
// other VARCHAR(255),

async function create(qualitative){
    const result = await db.query(
      `INSERT INTO qualitative_data 
      (user_id, nausea, fatigue, pain, rash, other) 
      VALUES 
      (?, ?, ?,?,?,?)`,
      [qualitative.user_id,
        qualitative.nausea,
        qualitative.fatigue,
        qualitative.pain,
        qualitative.rash,
        qualitative.other
    ]
    );
  
    let message = 'Error in adding qualitative data';
  
    if (result.affectedRows) {
      message = 'qualitative data added succesfully';
    }
  
    return {message};
  }

  async function getAll(userId) {
    const rows = await db.query(
        `SELECT * FROM qualitative_data WHERE user_id = ?;`,
        [userId]
    );

    console.log(rows);
    return rows;
}
  
  module.exports = {
    getAll,
    create
  }