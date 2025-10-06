const express = require('express');
const router = express.Router();
const ratingController = require('../controllers/rating.controller');
const { verifyToken } = require('../middleware/auth');

// Public routes
router.get('/offer/:offerId', ratingController.getOfferRatings);

// Protected routes
router.post('/', verifyToken, ratingController.createRating);
router.put('/:id', verifyToken, ratingController.updateRating);
router.delete('/:id', verifyToken, ratingController.deleteRating);

module.exports = router;

