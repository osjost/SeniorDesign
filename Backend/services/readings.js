const db = require('./db');
const threshold_cache = require('./threshold_cache')
const smsService = require("./messaging")
const fcc = require("./fcc")

async function create(reading){

  
    const result = await db.query(
      `INSERT INTO readings 
      (reading, sensor_id, user_id) 
      VALUES 
      (?, ?, ?)`,
      [reading.reading, reading.sensor_id, reading.user_id]
    );
  
    let message = 'Error in adding reading';
  
    if (result.affectedRows) {
      message = 'Reading added succesfully';
    }

    // perform threshold check if it already exists in the threshold cache
    
    // if (threshold_cache.existsInCache(reading.user_id, reading.sensor_id)) {
    //   threshold = threshold_cache.getThresh(reading.user_id, reading.sensor_id)
    //   lowerBound = threshold[0]
    //   upperBound = threshold[1]
    //   if (reading.reading > upperBound || reading.reading < lowerBound) {
    //     // figure out what doctor we need to alert
    //     const rows = await db.query(
    //       `SELECT * FROM provider_patient_associations WHERE patient_id = ?;`,
    //       [reading.user_id]
    //   );

    //   for (const provider of rows) {
    //     // get fcc of entry
    //     let fccRows = await fcc.get(provider.provider_id)

    //     // add notification to inbox
    //     const result = await db.query(
    //       `INSERT INTO provider_inbox 
    //       (provider_id, message, message_type, sender_id) 
    //       VALUES 
    //       (?, ?, ?,?)`,
    //       [provider.provider_id,
    //         "Threshold breach detected from sensor type " + reading.sensor_id,
    //         "breach",
    //         reading.user_id
    //     ]
    //     );
  
    //     for (const fcc of fccRows) {
    //       smsService.sendFirebaseNotification(fcc.fcc, "Emergency with user " + reading.user_id, "Threshold breach detected") 
    //     }

    //     console.log("triggered")
    //   }

    //   }
    // }
    

    return {message};
  }

  async function getAll(userId, sensorId, date) {
    // Query to get average readings for the last 30 days
    const averageReadingsRows = await db.query(
      `SELECT
        DATE(time_stamp) AS day,
        AVG(reading) AS average_reading
      FROM readings
      WHERE user_id = ? AND sensor_id = ?
        AND time_stamp >= CURDATE() - INTERVAL 30 DAY
      GROUP BY DATE(time_stamp)
      ORDER BY day;`,
      [userId, sensorId]
    );
  
    // Query to get all readings from the most recent day in the table
    const mostRecentDayRows = await db.query(
      `SELECT *
       FROM readings
       WHERE user_id = ? AND sensor_id = ?
         AND DATE(time_stamp) = (
           SELECT MAX(DATE(time_stamp))
           FROM readings
           WHERE user_id = ? AND sensor_id = ?
         );`,
      [userId, sensorId, userId, sensorId] // Parameters are listed twice since they're used in both the main query and the subquery
    );
  
    // Concatenate the results of the two queries
    const rows = averageReadingsRows.concat(mostRecentDayRows);
  
    return rows;
  }
  
  
  module.exports = {
    getAll,
    create
  }