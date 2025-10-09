const express = require('express');
const router = express.Router();
const offerController = require('../controllers/offer.controller');
const { verifyToken, checkRole } = require('../middleware/auth');

// Public routes
router.get('/', offerController.getAllOffers);
router.get('/search', offerController.searchOffers);
router.get('/:id', offerController.getOfferById);

// Protected routes
router.post('/', verifyToken, checkRole('GYM_STAFF', 'PT_USER', 'ADMIN'), offerController.createOffer);
router.put('/:id', verifyToken, checkRole('GYM_STAFF', 'PT_USER', 'ADMIN'), offerController.updateOffer);
router.delete('/:id', verifyToken, checkRole('GYM_STAFF', 'PT_USER', 'ADMIN'), offerController.deleteOffer);

module.exports = router;

