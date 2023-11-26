const db = require('./db');

async function getAll(providerId) {
    const rows = await db.query(
        `SELECT * FROM provider_patient_associations WHERE provider_id = ?;`,
        [providerId]
    );

    console.log(rows);
    return rows;
}
  
  module.exports = {
    getAll
  }