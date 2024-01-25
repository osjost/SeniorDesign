const db = require('./db');



async function create(messageIn){
    const result = await db.query(
      `INSERT INTO provider_inbox 
      (provider_id, message, message_type, sender_id) 
      VALUES 
      (?, ?, ?,?)`,
      [messageIn.provider_id,
        messageIn.message,
        "normal",
        messageIn.sender_id
    ]
    );
  
    let message = 'Error in adding normal inbox message';
  
    if (result.affectedRows) {
      message = 'Normal inbox message added succesfully';
    }
  
    return {message};
  }

  async function getAll(providerId) {
    const rows = await db.query(
        `SELECT * FROM provider_inbox WHERE provider_id = ?;`,
        [providerId]
    );

    console.log(rows);
    return rows;
  }

  // this needs to be passed the patient 

  async function requestAssociation(messageIn){
    const result = await db.query(
      `INSERT INTO provider_inbox 
      (provider_id, message, message_type, sender_id) 
      VALUES 
      (?, ?, ?,?)`,
      [messageIn.provider_id,
        "Association request from user " + messageIn.patient_name,
        "association_request",
        messageIn.sender_id
    ]
    );
  
    let message = 'Error in adding association inbox message';
  
    if (result.affectedRows) {
      message = 'Association inbox message added succesfully';
    }

    return {message};
  }

  async function remove(message_id){
    const result = await db.query(
      `DELETE FROM provider_inbox WHERE message_id= ?`,
      [message_id]
    );
  
    let message = 'Error in deleting message';
  
    if (result.affectedRows) {
      message = 'Message deleted successfully';
    }
  
    return {message};
  }
  
  module.exports = {
    getAll,
    create,
    requestAssociation,
    remove
  }