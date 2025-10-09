const express = require('express');
const router = express.Router();
const ptUserController = require('../controllers/ptUser.controller');
const { verifyToken, checkRole } = require('../middleware/auth');

// Public routes
router.get('/', ptUserController.getAllPTs);
router.get('/search', ptUserController.searchPTs);
router.get('/:id', ptUserController.getPTById);

// Protected routes
router.post('/', verifyToken, checkRole('PT_USER', 'ADMIN'), ptUserController.createPT);
router.put('/:id', verifyToken, checkRole('PT_USER', 'ADMIN'), ptUserController.updatePT);
router.delete('/:id', verifyToken, checkRole('ADMIN'), ptUserController.deletePT);

module.exports = router;

