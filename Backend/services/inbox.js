const db = require('./db');



async function create(messageIn){
    const result = await db.query(
      `INSERT INTO provider_inbox 
      (provider_id, message) 
      VALUES 
      (?, ?)`,
      [messageIn.provider_id,
        messageIn.message
    ]
    );
  
    let message = 'Error in adding inbox message';
  
    if (result.affectedRows) {
      message = 'Inbox message added succesfully';
    }
  
    return {message};
  }

  async function getAll(providerId) {
    const rows = await db.query(
        `SELECT * FROM provider_inbox WHERE provider_id = ?;`,
        [providerId]
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