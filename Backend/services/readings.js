const db = require('./db');

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
  
    return {message};
  }

  async function getAll(userId, sensorId) {
    const rows = await db.query(
        `SELECT * FROM readings WHERE user_id = ? AND sensor_id = ?;`,
        [userId, sensorId]
    );

    console.log(rows);
    return {
        rows
    };
}
  
  module.exports = {
    getAll,
    create
  }