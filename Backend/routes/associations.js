const express = require('express');
const router = express.Router();
const data = require('../services/associations');

/* GET data */
router.get('/:provider_id', async function(req, res, next) {
    try {
      res.json(await data.getAll(req.params.provider_id));
    } catch (err) {
      console.error(`Error while getting associations`, err.message);
      next(err);
    }
  });

  module.exports = router;