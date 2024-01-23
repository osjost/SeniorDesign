const db = require('./db');

async function create(threshold){
    const result = await db.query(
      `INSERT INTO threshold 
      (threshold, sensor_id, patient_id) 
      VALUES 
      (?, ?, ?)`,
      [threshold.threshold, threshold.sensor_id, threshold.patient_id]
    );
  
    let message = 'Error in adding threshold';
  
    if (result.affectedRows) {
      message = 'Threshold added succesfully';
    }
  
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
  
  module.exports = {
    get,
    create
  }