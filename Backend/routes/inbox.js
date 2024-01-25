const express = require('express');
const router = express.Router();
const inbox = require('../services/inbox');


/* POST general inbox */
router.post('/', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await inbox.create(req.body));
  } catch (err) {
    console.error(`Error while adding inbox message`, err.message);
    next(err);
  }
});

/* POST association request */
router.post('/associations', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await inbox.requestAssociation(req.body));
  } catch (err) {
    console.error(`Error while requesting association`, err.message);
    next(err);
  }
});


/* POST emergency alert */

/* GET inbox */
router.get('/:provider_id', async function(req, res, next) {
  try {
    res.json(await inbox.getAll(req.params.provider_id));
  } catch (err) {
    console.error(`Error while getting inbox messages`, err.message);
    next(err);
  }
});

/* REMOVE message */
router.delete('/:message_id', async function(req, res, next) {
  try {
    console.log(req.body)
    res.json(await inbox.remove(req.params.message_id));
  } catch (err) {
    console.error(`Error while deleting message`, err.message);
    next(err);
  }
});


module.exports = router;