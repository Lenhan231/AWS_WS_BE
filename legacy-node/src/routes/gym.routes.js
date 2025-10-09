const express = require('express');
const router = express.Router();
const gymController = require('../controllers/gym.controller');
const { verifyToken, checkRole } = require('../middleware/auth');

// Public routes
router.get('/', gymController.getAllGyms);
router.get('/search', gymController.searchGyms);
router.get('/:id', gymController.getGymById);

// Protected routes
router.post('/', verifyToken, checkRole('GYM_STAFF', 'ADMIN'), gymController.createGym);
router.put('/:id', verifyToken, checkRole('GYM_STAFF', 'ADMIN'), gymController.updateGym);
router.delete('/:id', verifyToken, checkRole('ADMIN'), gymController.deleteGym);

// PT Assignment
router.post('/:id/assign-pt', verifyToken, checkRole('GYM_STAFF', 'ADMIN'), gymController.assignPT);
router.get('/:id/personal-trainers', gymController.getGymPTs);

module.exports = router;

