const { Report, User, Offer } = require('../models');
const { body, validationResult } = require('express-validator');

// Create report
exports.createReport = [
  body('reason').notEmpty().withMessage('Reason is required'),
  body('details').optional(),

  async (req, res) => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          status: 400,
          error: 'Validation Error',
          errors: errors.array(),
          timestamp: new Date().toISOString()
        });
      }

      const { reason, details, reportedUserId, offerId } = req.body;

      // At least one of reportedUserId or offerId must be provided
      if (!reportedUserId && !offerId) {
        return res.status(400).json({
          status: 400,
          error: 'Bad Request',
          message: 'Either reportedUserId or offerId must be provided',
          timestamp: new Date().toISOString()
        });
      }

      const report = await Report.create({
        reporterId: req.user.id,
        reportedUserId,
        offerId,
        reason,
        details,
        status: 'PENDING'
      });

      const fullReport = await Report.findByPk(report.id, {
        include: [
          { model: User, as: 'reporter', attributes: ['firstName', 'lastName', 'email'] },
          { model: User, as: 'reportedUser', attributes: ['firstName', 'lastName', 'email'] },
          { model: Offer, as: 'offer' }
        ]
      });

      res.status(201).json(fullReport);
    } catch (error) {
      console.error('Create report error:', error);
      res.status(500).json({
        status: 500,
        error: 'Internal Server Error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
    }
  }
];

// Get my reports
exports.getMyReports = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    const { count, rows } = await Report.findAndCountAll({
      where: { reporterId: req.user.id },
      limit: size,
      offset,
      include: [
        { model: User, as: 'reportedUser', attributes: ['firstName', 'lastName'] },
        { model: Offer, as: 'offer', attributes: ['title'] }
      ],
      order: [['createdAt', 'DESC']]
    });

    res.json({
      content: rows,
      pageNumber: page,
      pageSize: size,
      totalElements: count,
      totalPages: Math.ceil(count / size),
      last: offset + size >= count,
      first: page === 0
    });
  } catch (error) {
    console.error('Get my reports error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

