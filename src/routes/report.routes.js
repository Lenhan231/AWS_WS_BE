const express = require('express');
const router = express.Router();
const reportController = require('../controllers/report.controller');
const { verifyToken } = require('../middleware/auth');

// All report routes are protected
router.post('/', verifyToken, reportController.createReport);
router.get('/my-reports', verifyToken, reportController.getMyReports);

module.exports = router;

