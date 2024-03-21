const db = require('./db');

async function create(qualitative){

      // get the previously stored value
      const rows = await db.query(
        `SELECT * 
          FROM qualitative_data 
          WHERE user_id = ? 
          ORDER BY time_stamp DESC 
          LIMIT 1`,
        [qualitative.user_id]
      );

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



    if (rows.length != 0) {
      entry = rows[0]
      const conditionsMet = 
        Math.abs(entry.nausea - qualitative.nausea) > 3 ||
        Math.abs(entry.fatigue - qualitative.fatigue) > 3 ||
        Math.abs(entry.pain - qualitative.pain) > 3 ||
        Math.abs(entry.rash - qualitative.rash) > 3;

      if (conditionsMet) {
          const rows = await db.query(
            `SELECT * FROM provider_patient_associations WHERE patient_id = ?;`,
            [reading.user_id]
        );

        for (const provider of rows) {
          // get fcc of entry
          let fccRows = await fcc.get(provider.provider_id)



          for (const fcc of fccRows) {
            smsService.sendFirebaseNotification(fcc, "Emergency with user " + reading.user_id, "Threshold breach detected") 
          }
        }

      }
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