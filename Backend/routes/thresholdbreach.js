const express = require('express');
const router = express.Router();
const thresholdbreach = require('../services/thresholdbreach');

  /* POST association */
router.post('/', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await thresholdbreach.create(req.body));
    
  } catch (err) {
    console.error(`Error while sending threshold breach alert`, err.message);
    next(err);
  }
});

  module.exports = router;

