const express = require('express');
const router = express.Router();
const login = require('../services/login');

// POST route for authentication
router.post('/', async function(req, res, next) {
  try {
    res.json(await login.authenticate(req.body.username, req.body.password));
  } catch (err) {
    console.error(`Failed to authenticate`, err.message);
    next(err);
  }
});

module.exports = router;
