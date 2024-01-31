const express = require('express');
const router = express.Router();
const associations = require('../services/associations');

/* GET */
router.get('/:provider_id', async function(req, res, next) {
    try {
      res.json(await associations.getAll(req.params.provider_id));
    } catch (err) {
      console.error(`Error while getting associations`, err.message);
      next(err);
    }
  });

  /* POST association */
router.post('/', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await associations.create(req.body));
    
  } catch (err) {
    console.error(`Error while creating association`, err.message);
    next(err);
  }
});

/* remove association */
router.delete('/:patient_id', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await associations.remove(req.params.patient_id));
  } catch (err) {
    console.error(`Error while deleting association`, err.message);
    next(err);
  }
});


  module.exports = router;

