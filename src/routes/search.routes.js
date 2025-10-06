const express = require('express');
const router = express.Router();
const searchController = require('../controllers/search.controller');

// Search all (gyms, PTs, offers)
router.get('/', searchController.searchAll);

module.exports = router;

