const db = require('./db');
const fcc = require("./fcc")
const smsService = require("./messaging")

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
      message = 'Qualitative data added succesfully';
    }

    if (rows.length != 0) {
      const entry = rows[0];
      let violatedString = "";
      let conditions = [
        { name: 'nausea', value: Math.abs(entry.nausea - qualitative.nausea) },
        { name: 'fatigue', value: Math.abs(entry.fatigue - qualitative.fatigue) },
        { name: 'pain', value: Math.abs(entry.pain - qualitative.pain) }
      ];

      conditions.forEach(condition => {
        if (condition.value > 3) {
          violatedString += `${condition.name}, `;
        }
      });

      // Remove trailing comma and space
      violatedString = violatedString.replace(/, $/, '');

      if (violatedString.length > 0) {
        const providerRows = await db.query(
          `SELECT * FROM provider_patient_associations WHERE patient_id = ?;`,
          [qualitative.user_id]
        );

        for (const provider of providerRows) {
          const inboxResult = await db.query(
            `INSERT INTO provider_inbox 
            (provider_id, message, message_type, sender_id) 
            VALUES 
            (?, ?, ?, ?)`,
            [provider.provider_id,
              `Threshold breach detected for ${violatedString}.`, 
              "breach",
              qualitative.user_id
            ]
          );

          let fccRows = await fcc.get(provider.provider_id);
          for (const fcc of fccRows) {
            smsService.sendFirebaseNotification(fcc.fcc, "Emergency with user " + qualitative.user_id, `Threshold breach detected for ${violatedString}.`)
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
