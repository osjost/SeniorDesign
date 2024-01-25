const db = require('./db');

async function getAll(providerId) {
    const rows = await db.query(
        `SELECT * FROM provider_patient_associations WHERE provider_id = ?;`,
        [providerId]
    );

    console.log(rows);
    return rows;
}

async function create(associationReq){
  const result = await db.query(
    `INSERT INTO provider_patient_associations 
    (provider_id, patient_id) 
    VALUES 
    (?, ?)`,
    [
      associationReq.provider_id,
      associationReq.patient_id
    ]
  );

  let message = 'Error in creating association';

  if (result.affectedRows) {
    message = 'Association created succesfully';
  }

  return {message};
}

async function remove(patient_id){
  const result = await db.query(
    `DELETE FROM provider_patient_associations WHERE patient_id= ?`,
    [patient_id]
  );

  let message = 'Error in deleting association';

  if (result.affectedRows) {
    message = 'Association deleted successfully';
  }

  return {message};
}

  
  module.exports = {
    getAll,
    create,
    remove
  }