const express = require('express');
const router = express.Router();
const searchController = require('../controllers/search.controller');

// Search nearby locations (Gyms, PTs) within radius
router.get('/nearby', searchController.searchNearby);

// Search all (gyms, PTs, offers)
router.get('/', searchController.searchAll);

module.exports = router;
