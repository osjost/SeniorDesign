const express = require('express');
const router = express.Router();
const data = require('../services/readings');


/* POST data */
router.post('/', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await data.create(req.body));
  } catch (err) {
    console.error(`Error while adding reading`, err.message);
    next(err);
  }
});

/* GET data */
router.get('/:user_id/:sensor_id', async function(req, res, next) {
  try {
    res.json(await data.getAll(req.params.user_id, req.params.sensor_id, req.params.date));
  } catch (err) {
    console.error(`Error while getting reading`, err.message);
    next(err);
  }
});

module.exports = router;