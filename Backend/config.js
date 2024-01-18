// for the aws released version:
const config = {
  db: {
    /* don't expose password or any sensitive info, done only for demo */
    host: "database-test1.chaq6oaiibfk.us-west-1.rds.amazonaws.com",
    user: "admin",
    password: "bdYND8yKgub4z3",
    database: "cytocheck",
    port: '3306'
  },
};
module.exports = config;

// below is for local host testing, JUST COMMENT THE ABOVE OUT DO NOT DELETE IT PLS
// const config = {
//     db: {
//       /* don't expose password or any sensitive info, done only for demo */
//       host: "localhost",
//       user: "root",
//       password: "X4eeqle35/",
//       database: "cytocheck",
//       port: '3306'
//     },
//   };
//   module.exports = config;


