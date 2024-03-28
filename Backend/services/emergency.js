const db = require('./db');
const fcc = require('./fcc')
const appInbox = require("./inbox")
const smsService = require("./messaging")

async function create(emergency){

    // get doctors we want to alert
    const rows = await db.query(
        `SELECT * FROM provider_patient_associations WHERE patient_id = ?;`,
        [emergency.user_id]
    );

    for (const provider of rows) {
        // get fcc of entry
        let fccRows = await fcc.get(provider.provider_id)
  
        for (let currFcc of fccRows) {
          // add emergency message to the inbox
        const result = await db.query(
          `INSERT INTO provider_inbox 
          (provider_id, message, message_type, sender_id) 
          VALUES 
          (?, ?, ?,?)`,
          [provider.provider_id,
            "Emergency issued!",
            "emergency",
            emergency.user_id
        ]
        );
        // issue a push notification
          smsService.sendFirebaseNotification(currFcc.fcc, "Emergency with user " + emergency.user_id, emergency.user_id + " has issued an emergency notification")

          
        

        }
      }

    if (rows.length === 0) {
        message = "Error sending emergency notification"
    }
    else {
        message = "Succesfully sent notification"
    }
    return {message};
  }

  module.exports = {
    create
  }