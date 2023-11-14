const express = require('express');
const router = express.Router();
const users = require('../services/data');

/* GET data */
router.get('/:id', async function(req, res, next) {
  try {
    res.json(await users.getSingle(req.params.id));
  } catch (err) {
    console.error(`Error while getting user`, err.message);
    next(err);
  }
});

/* POST data */
router.post('/', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await users.create(req.body));
    
  } catch (err) {
    console.error(`Error while creating user`, err.message);
    next(err);
  }
});


/* PUT data */
router.put('/', async function(req, res, next) {

  console.log(await req.body)
  try {
    res.json(await users.update(req.body));
  } catch (err) {
    console.error(`Error while updating user`, err.message);
    next(err);
  }
});

/* DELETE data */
router.delete('/', async function(req, res, next) {
  try {
    res.json(await users.remove(req.body));
  } catch (err) {
    console.error(`Error while deleting user`, err.message);
    next(err);
  }
});

module.exports = router;