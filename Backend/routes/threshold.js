const express = require('express');
const router = express.Router();
const threshold = require('../services/threshold');


/* POST data */
router.post('/', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await threshold.create(req.body));
  } catch (err) {
    console.error(`Error while adding threshold`, err.message);
    next(err);
  }
});

/* GET data */
router.get('/:patient_id/:sensor_id', async function(req, res, next) {
  try {
    res.json(await threshold.get(req.params.patient_id, req.params.sensor_id));
  } catch (err) {
    console.error(`Error while getting threshold`, err.message);
    next(err);
  }
});

module.exports = router;