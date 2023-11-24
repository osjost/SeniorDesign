const express = require('express');
const router = express.Router();
const register = require('../services/register');

// POST route for new user creation
router.post('/', async function(req, res, next) {
  try {
    //console.log(req);
    res.json(await register.register(req.body));
  } catch (err) {
    console.error(`Failed to register new user`, err.message);
    next(err);
  }
});

module.exports = router;
