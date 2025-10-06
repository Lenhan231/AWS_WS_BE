const express = require('express');
const router = express.Router();
const authController = require('../controllers/auth.controller');
const { verifyToken } = require('../middleware/auth');

// Public routes (không cần token)
router.post('/register', authController.register);
router.post('/login', authController.login);

// Protected routes (cần token)
router.get('/me', verifyToken, authController.getCurrentUser);
router.put('/me', verifyToken, authController.updateProfile);
router.post('/change-password', verifyToken, authController.changePassword);

module.exports = router;
