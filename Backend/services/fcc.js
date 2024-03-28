const db = require('./db');

async function create(fcc){
  
  const deleteResult = await db.query(
    `DELETE FROM fcc_associations WHERE user_id = ?`,
    [fcc.user_id]
);
    const result = await db.query(
      `INSERT INTO fcc_associations 
      (user_id, fcc) 
      VALUES 
      (?, ?)`,
      [fcc.user_id, fcc.fcc]
    );
  
    let message = 'Error in adding fcc';
  
    if (result.affectedRows) {
      message = 'Fcc added succesfully';
    }

    return {message};
  }

  async function get(userId) {
    const rows = await db.query(
        `SELECT * FROM fcc_associations  WHERE user_id = ?;`,
        [userId]
    );

    console.log(rows);
    return rows;
}

async function update(fcc){

  const result = await db.query(
    `UPDATE fcc_associations
    SET
        fcc = ?
    WHERE user_id = ?;`,
    [
      fcc.fcc,
      fcc.user_id
    ]
  );
  
    let message = 'Error in updating fcc';
  
    if (result.affectedRows) {
      message = 'fcc updated succesfully';
    }
  
 
    return {message};
  }
  
  module.exports = {
    get,
    create,
    update
  }