//main entry point. Start the server with node index.js
// on windows, to verify mysql is running, go windows button -> services -> scroll to find mySQL
const express = require("express");
const https = require("https");
const fs = require("fs");
const app = express();

const privateKey = fs.readFileSync('key.pem', 'utf8');
const certificate = fs.readFileSync('cert.pem', 'utf8');

const credentials = { key: privateKey, cert: certificate };

const usersRouter = require("./routes/users");
const loginRouter = require("./routes/login");
const registerRouter = require("./routes/register");
const readingsRouter = require("./routes/readings");
const qualitativeRouter = require("./routes/qualitative");
const inboxRouter = require("./routes/inbox");
const associationRouter = require("./routes/associations");
const thresholdRouter = require("./routes/threshold");
const fccRouter = require("./routes/fcc");

const threshold_test_cache = require("./services/threshold_cache")

const verifyJWT = require("./services/jwtverifier");

app.use(express.json());
app.use(
  express.urlencoded({
    extended: true,
  })
);

app.get("/", (req, res) => {
  res.json({ message: "ok" });
});


// COMMENTED OUT FOR TESTING!
// app.use("/users", verifyJWT, usersRouter);
// app.use("/readings", verifyJWT, readingsRouter);
// app.use("/qualitative", verifyJWT, qualitativeRouter);
// app.use("/inbox", verifyJWT, inboxRouter);
// app.use("/associations", verifyJWT, associationRouter);
// app.use("/threshold", verifyJWT, thresholdRouter)
// app.use("/fcc", verifyJWT, fccRouter)

app.use("/users", usersRouter);
app.use("/readings", readingsRouter);
app.use("/qualitative", qualitativeRouter);
app.use("/inbox", inboxRouter);
app.use("/associations", associationRouter);
app.use("/threshold", thresholdRouter)
app.use("/fcc", fccRouter)

app.use("/login", loginRouter);
app.use("/register", registerRouter);



threshold_test_cache.createThresholdCache();

/* Error handler middleware */
app.use((err, req, res, next) => {
  const statusCode = err.statusCode || 500;
  console.error(err.message, err.stack);
  res.status(statusCode).json({ message: err.message });
  return;
});

// Stuff on a timer


// Create HTTPS server with the Express app
const httpsServer = https.createServer(credentials, app);

const httpsPort = 443; // Standard port for HTTPS
httpsServer.listen(httpsPort, () => {
  console.log(`HTTPS Server running`);
});


// const express = require("express");
// const app = express();
// const port = 3000;
// const usersRouter = require("./routes/users");
// app.use(express.json());
// app.use(
//   express.urlencoded({
//     extended: true,
//   })
// );
// app.get("/", (req, res) => {
//   res.json({ message: "ok" });
// });
// app.use("/users", usersRouter);
// /* Error handler middleware */
// app.use((err, req, res, next) => {
//   const statusCode = err.statusCode || 500;
//   console.error(err.message, err.stack);
//   res.status(statusCode).json({ message: err.message });
//   return;
// });
// app.listen(port, () => {
//   console.log(`Server listening at http://localhost:${port}`);
// });