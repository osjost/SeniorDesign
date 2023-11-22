const express = require('express');
const router = express.Router();
const data = require('../services/inbox');


/* POST data */
router.post('/', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await data.create(req.body));
  } catch (err) {
    console.error(`Error while adding inbox message`, err.message);
    next(err);
  }
});

/* GET data */
router.get('/:provider_id', async function(req, res, next) {
  try {
    res.json(await data.getAll(req.params.provider_id));
  } catch (err) {
    console.error(`Error while getting inbox messages`, err.message);
    next(err);
  }
});

module.exports = router;