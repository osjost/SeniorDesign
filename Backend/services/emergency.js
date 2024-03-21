const db = require('./db');
const appInbox = require("./inbox")

async function create(emergency){

    // get doctors we want to alert
    const rows = await db.query(
        `SELECT * FROM provider_patient_associations WHERE patient_id = ?;`,
        [emergency.user_id]
    );

    for (const provider of rows) {
        // get fcc of entry
        let fccRows = fcc.get(provider.provider_id)
  
        for (const fcc of fccRows) {
        // issue a push notification
          smsService.sendFirebaseNotification(fcc, "Emergency with user " + emergency.user_id, emergency.user_id + " has issued an emergency notification")

        // add emergency message to the inbox
        let message = {"provider_id":provider.provider_id,
        "message":"Emergency issued!",
        "message_type":"emergency",
        "sender_id":emergency.user_id
        }
        appInbox.create(message)
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
    create,
  }