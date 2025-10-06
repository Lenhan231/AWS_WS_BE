const express = require('express');
const router = express.Router();
const mediaController = require('../controllers/media.controller');
const { verifyToken } = require('../middleware/auth');

// All media routes require authentication
router.post('/presigned-url', verifyToken, mediaController.getPresignedUrl);
router.post('/upload-complete', verifyToken, mediaController.confirmUpload);

module.exports = router;

