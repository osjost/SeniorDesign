const db = require('./db');
const threshold_cache = require('./threshold_cache')

async function create(threshold){
    // if threshold already exists for that sensor type assigned to that user, delete it first before adding it

    const deleteResult = await db.query(
    `DELETE FROM threshold 
    WHERE patient_id = ? AND sensor_id = ?`,
    [threshold.patient_id, threshold.sensor_id]
    );

    const result = await db.query(
      `INSERT INTO threshold 
      (upper, lower, sensor_id, patient_id) 
      VALUES 
      (?, ?, ?, ?)`,
      [threshold.lower, threshold.upper, threshold.sensor_id, threshold.patient_id]
    );
  
    let message = 'Error in adding threshold';
  
    if (result.affectedRows) {
      message = 'Threshold added succesfully';
    }

    // add value to the threshold cache
    threshold_cache.addThresh(threshold.patient_id, threshold.sensor_id, threshold.lower, threshold.upper)
  
    return {message};
  }

  async function get(patientId, sensorId) {
    const rows = await db.query(
        `SELECT * FROM threshold WHERE patient_id = ? AND sensor_id = ?;`,
        [patientId, sensorId]
    );

    console.log(rows);
    return rows[0];
}

async function update(threshold){
    const result = await db.query(
      `UPDATE threshold
      SET
          upper = ?,
          lower = ?,
      WHERE patient_id = ? AND sensor_id = ?;`,
      [
        threshold.upper,
        threshold.lower,
        threshold.patient_id,
        threshold.sensor_id
      ]
    );
  
    let message = 'Error in updating threshold';
  
    if (result.affectedRows) {
      message = 'Threshold updated successfully';
    }

    // update value in the threshold cache
    threshold_cache.addThresh(threshold.patient_id, threshold.sensor_id, threshold.lower, threshold.upper)
  
    return {message};
  }
  
  module.exports = {
    get,
    create,
    update
  }