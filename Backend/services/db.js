const mysql = require('mysql2/promise');
const config = require('../config');

const pool = mysql.createPool(config.db); // Ensure config.db includes connectionLimit, etc.

async function query(sql, params) {
  const [results, ] = await pool.execute(sql, params); // Use the pool directly.
  return results;
}

module.exports = {
  query
}