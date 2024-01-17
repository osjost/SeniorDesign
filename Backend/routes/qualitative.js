const express = require('express');
const router = express.Router();
const data = require('../services/qualitative');


/* POST data */
router.post('/', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await data.create(req.body));
  } catch (err) {
    console.error(`Error while adding qualitative data`, err.message);
    next(err);
  }
});

/* GET data */
router.get('/:user_id', async function(req, res, next) {
  try {
    res.json(await data.getAll(req.params.user_id));
  } catch (err) {
    console.error(`Error while getting qualitative data`, err.message);
    next(err);
  }
});

module.exports = router;