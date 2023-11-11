// verifies a connection exists to a mysql database based on the db.js targeting fields

const mysql = require('mysql2');
const config = require('./config');  

const connection = mysql.createConnection(config.db);

connection.connect(function(err) {
  if (err) {
    console.error('Error connecting: ' + err.stack);
    return;
  }

  console.log('Connected as id ' + connection.threadId);
});

connection.end();
