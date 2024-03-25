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
    
    if (threshold_cache.existsInCache(reading.user_id, reading.sensor_id)) {
      threshold = threshold_cache.getThresh(reading.user_id, reading.sensor_id)
      lowerBound = threshold[0]
      upperBound = threshold[1]
      if (!( upperBound < reading.reading < lowerBound)) {
        // figure out what doctor we need to alert
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

        console.log("triggered")
      }

      }
    }
    

    return {message};
  }

  async function getAll(userId, sensorId) {
    const rows = await db.query(
        `SELECT * FROM readings WHERE user_id = ? AND sensor_id = ?;`,
        [userId, sensorId]
    );

    console.log(rows);
    return rows;
}
  
  module.exports = {
    getAll,
    create
  }