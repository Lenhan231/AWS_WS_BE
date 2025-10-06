const express = require('express');
const router = express.Router();
const adminController = require('../controllers/admin.controller');
const { verifyToken, checkRole } = require('../middleware/auth');

// All admin routes require ADMIN role
router.use(verifyToken, checkRole('ADMIN'));

// Pending approvals
router.get('/pending-offers', adminController.getPendingOffers);
router.get('/pending-gym-pt-associations', adminController.getPendingGymPTAssociations);

// Moderation actions
router.post('/moderate-offer/:id', adminController.moderateOffer);
router.post('/moderate-gym-pt-association/:id', adminController.moderateGymPTAssociation);

// Reports management
router.get('/reports', adminController.getAllReports);
router.post('/reports/:id/review', adminController.reviewReport);

// User management
router.get('/users', adminController.getAllUsers);
router.put('/users/:id/toggle-active', adminController.toggleUserActive);

module.exports = router;

