const db = require('./db');
const helper = require('../helper');
const config = require('../config');

async function getMultiple(page = 1){
  const offset = helper.getOffset(page, config.listPerPage);
  const rows = await db.query(
    `SELECT * FROM cytocheck.users;`
  );
  const data = helper.emptyOrRows(rows);
  const meta = {page};

  return {
    data,
    meta
  }
}

async function create(user){
  const result = await db.query(
    `INSERT INTO users 
    (role, username, first_name, last_name, date_of_birth, email, phone_number, num_measures) 
    VALUES 
    (?, ?, ?, ?, ?, ?, ?, ?)`,
    [user.role, user.username, user.first_name, user.last_name, user.date_of_birth, user.email, user.phone_number, user.num_measures]
  );

  let message = 'Error in creating user';

  if (result.affectedRows) {
    message = 'User created succesfully';
  }

  return {message};
}

async function update(id, user){
  const result = await db.query(
    `UPDATE users
    SET name="${programmingLanguage.name}", released_year=${programmingLanguage.released_year}, githut_rank=${programmingLanguage.githut_rank}, 
    pypl_rank=${programmingLanguage.pypl_rank}, tiobe_rank=${programmingLanguage.tiobe_rank} 
    WHERE id=${id}` 
  );

  let message = 'Error in updating user;

  if (result.affectedRows) {
    message = 'User updated successfully';
  }

  return {message};
}


module.exports = {
  getMultiple,
  create,
  update
}

