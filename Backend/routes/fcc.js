const express = require('express');
const router = express.Router();
const tokening = require('../services/fcc');


/* POST data */
router.post('/', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await tokening.create(req.body));
  } catch (err) {
    console.error(`Error while adding notification token`, err.message);
    next(err);
  }
});

/* GET data */
router.get('/:user_id', async function(req, res, next) {
  try {
    res.json(await tokening.get(req.params.user_id, req.params.sensor_id));
  } catch (err) {
    console.error(`Error while getting notification token`, err.message);
    next(err);
  }
});

/* PUT user */
router.put('/', async function(req, res, next) {

    console.log(await req.body)
    try {
      res.json(await tokening.update(req.body));
    } catch (err) {
      console.error(`Error while updating notification token`, err.message);
      next(err);
    }
  });


module.exports = router;