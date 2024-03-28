const db = require('./db');
const fcc = require('./fcc')
const appInbox = require("./inbox")
const smsService = require("./messaging")

async function create(breach){

    // get doctors we want to alert
    const rows = await db.query(
        `SELECT * FROM provider_patient_associations WHERE patient_id = ?;`,
        [breach.user_id]
    );

    for (const provider of rows) {
        // get fcc of entry
        let fccRows = await fcc.get(provider.provider_id)
  
        for (let currFcc of fccRows) {
          // add threshold breach message to the inbox
        const result = await db.query(
          `INSERT INTO provider_inbox 
          (provider_id, message, message_type, sender_id) 
          VALUES 
          (?, ?, ?,?)`,
          [provider.provider_id,
            "User-id " + breach.user_id + " has recorded a threshold breach on sensor-id " + breach.sensor_id,
            "breach",
            breach.user_id
        ]
        );
        // issue a push notification
          smsService.sendFirebaseNotification(currFcc.fcc, "Threshold breach detected!", "User-id " + breach.user_id + " has recorded a threshold breach on sensor-id " + breach.sensor_id)

          
        

        }
      }

    if (rows.length === 0) {
        message = "Error sending threshold breach notification"
    }
    else {
        message = "Succesfully sent notification"
    }
    return {message};
  }

  module.exports = {
    create
  }