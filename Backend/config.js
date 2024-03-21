// For the AWS released version:
const config = {
  db: {
    // don't expose password or any sensitive info, done only for demo
    host: "database-test1.chaq6oaiibfk.us-west-1.rds.amazonaws.com",
    user: "admin",
    password: "bdYND8yKgub4z3",
    database: "cytocheck",
    port: '3306',
    connectionLimit: 100 // Example limit, adjust based on your needs
  },
};

// below is for local host testing, JUST COMMENT THE ABOVE OUT DO NOT DELETE IT PLS
// const config = {
//   db: {
//     // don't expose password or any sensitive info, done only for demo
//     host: "localhost",
//     user: "root",
//     password: "3G3ct9mh",
//     database: "cytocheck",
//     port: '3306',
//     connectionLimit: 10 // Example limit, adjust based on your needs
//   },
// };
  module.exports = config;


