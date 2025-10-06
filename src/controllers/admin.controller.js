const { Offer, Report, User, GymPTAssociation, Gym, PTUser, Location } = require('../models');
const { body, validationResult } = require('express-validator');

// Get pending offers
exports.getPendingOffers = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    const { count, rows } = await Offer.findAndCountAll({
      where: { status: 'PENDING' },
      limit: size,
      offset,
      include: [
        { model: Gym, as: 'gym' },
        { model: PTUser, as: 'ptUser' },
        { model: User, as: 'creator', attributes: ['firstName', 'lastName', 'email'] }
      ],
      order: [['createdAt', 'ASC']]
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
    console.error('Get pending offers error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Get pending gym-PT associations
exports.getPendingGymPTAssociations = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    const { count, rows } = await GymPTAssociation.findAndCountAll({
      where: { status: 'PENDING' },
      limit: size,
      offset,
      include: [
        { model: Gym, include: [{ model: Location, as: 'location' }] },
        {
          model: PTUser,
          include: [
            { model: User, attributes: ['firstName', 'lastName', 'email'] }
          ]
        }
      ],
      order: [['createdAt', 'ASC']]
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
    console.error('Get pending associations error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Moderate offer (approve/reject)
exports.moderateOffer = [
  body('decision').isIn(['APPROVED', 'REJECTED']).withMessage('Invalid decision'),
  body('moderationNotes').optional(),

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

      const { decision, moderationNotes } = req.body;
      const offer = await Offer.findByPk(req.params.id);

      if (!offer) {
        return res.status(404).json({
          status: 404,
          error: 'Not Found',
          message: 'Offer not found',
          timestamp: new Date().toISOString()
        });
      }

      await offer.update({
        status: decision,
        moderationNotes
      });

      res.json(offer);
    } catch (error) {
      console.error('Moderate offer error:', error);
      res.status(500).json({
        status: 500,
        error: 'Internal Server Error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
    }
  }
];

// Moderate gym-PT association
exports.moderateGymPTAssociation = [
  body('decision').isIn(['APPROVED', 'REJECTED']).withMessage('Invalid decision'),
  body('rejectionReason').optional(),

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

      const { decision, rejectionReason } = req.body;
      const association = await GymPTAssociation.findByPk(req.params.id);

      if (!association) {
        return res.status(404).json({
          status: 404,
          error: 'Not Found',
          message: 'Association not found',
          timestamp: new Date().toISOString()
        });
      }

      await association.update({
        status: decision,
        approvedAt: decision === 'APPROVED' ? new Date() : null,
        rejectionReason: decision === 'REJECTED' ? rejectionReason : null
      });

      res.json(association);
    } catch (error) {
      console.error('Moderate association error:', error);
      res.status(500).json({
        status: 500,
        error: 'Internal Server Error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
    }
  }
];

// Get all reports
exports.getAllReports = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;
    const status = req.query.status || 'PENDING';

    const { count, rows } = await Report.findAndCountAll({
      where: { status },
      limit: size,
      offset,
      include: [
        { model: User, as: 'reporter', attributes: ['firstName', 'lastName', 'email'] },
        { model: User, as: 'reportedUser', attributes: ['firstName', 'lastName', 'email'] },
        { model: Offer, as: 'offer', attributes: ['title'] }
      ],
      order: [['createdAt', 'ASC']]
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
    console.error('Get all reports error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Review report
exports.reviewReport = [
  body('status').isIn(['REVIEWED', 'RESOLVED', 'DISMISSED']).withMessage('Invalid status'),
  body('adminNotes').optional(),

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

      const { status, adminNotes } = req.body;
      const report = await Report.findByPk(req.params.id);

      if (!report) {
        return res.status(404).json({
          status: 404,
          error: 'Not Found',
          message: 'Report not found',
          timestamp: new Date().toISOString()
        });
      }

      await report.update({
        status,
        adminNotes,
        reviewedBy: req.user.id
      });

      res.json(report);
    } catch (error) {
      console.error('Review report error:', error);
      res.status(500).json({
        status: 500,
        error: 'Internal Server Error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
    }
  }
];

// Get all users
exports.getAllUsers = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    const { count, rows } = await User.findAndCountAll({
      limit: size,
      offset,
      attributes: { exclude: ['cognitoSub'] },
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
    console.error('Get all users error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Toggle user active status
exports.toggleUserActive = async (req, res) => {
  try {
    const user = await User.findByPk(req.params.id);

    if (!user) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'User not found',
        timestamp: new Date().toISOString()
      });
    }

    await user.update({ active: !user.active });
    res.json(user);
  } catch (error) {
    console.error('Toggle user active error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

