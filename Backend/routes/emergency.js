const express = require('express');
const router = express.Router();
const emergency = require('../services/emergency');

  /* POST association */
router.post('/', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await emergency.create(req.body));
    
  } catch (err) {
    console.error(`Error while sending emergency`, err.message);
    next(err);
  }
});

  module.exports = router;

