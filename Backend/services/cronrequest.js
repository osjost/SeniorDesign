const db = require('./db');
const messaging = require('./messaging')

async function sendRequests(){
    // get every fcc token in database
    const rows = await db.query(
        `SELECT * FROM fcc_associations;`
      );

    
    // send message to every fcc token currently registered
    for (const currUser of rows) {
        messaging.sendFirebaseNotification(currUser.fcc, "Please check Cytocheck", "Please take readings or validate patient input!")
    }

    console.log(rows)
}


module.exports = {
    sendRequests
 };