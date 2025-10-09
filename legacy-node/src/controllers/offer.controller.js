const { Offer, Gym, PTUser, User, Location } = require('../models');
const { body, validationResult } = require('express-validator');
const { Op } = require('sequelize');

// Get all offers with pagination
exports.getAllOffers = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    const { count, rows } = await Offer.findAndCountAll({
      limit: size,
      offset,
      where: { status: 'APPROVED', active: true },
      include: [
        {
          model: Gym,
          as: 'gym',
          include: [{ model: Location, as: 'location' }]
        },
        {
          model: PTUser,
          as: 'ptUser',
          include: [
            { model: User, attributes: ['firstName', 'lastName'] },
            { model: Location, as: 'location' }
          ]
        }
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
    console.error('Get all offers error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Search offers
exports.searchOffers = async (req, res) => {
  try {
    const {
      query, offerType, minPrice, maxPrice,
      latitude, longitude, radiusKm
    } = req.query;
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    let whereClause = { status: 'APPROVED', active: true };

    // Text search
    if (query) {
      whereClause[Op.or] = [
        { title: { [Op.iLike]: `%${query}%` } },
        { description: { [Op.iLike]: `%${query}%` } }
      ];
    }

    // Offer type filter
    if (offerType) {
      whereClause.offerType = offerType;
    }

    // Price range
    if (minPrice) {
      whereClause.price = { [Op.gte]: parseFloat(minPrice) };
    }
    if (maxPrice) {
      whereClause.price = {
        ...whereClause.price,
        [Op.lte]: parseFloat(maxPrice)
      };
    }

    const { count, rows } = await Offer.findAndCountAll({
      where: whereClause,
      limit: size,
      offset,
      include: [
        {
          model: Gym,
          as: 'gym',
          include: [{ model: Location, as: 'location' }]
        },
        {
          model: PTUser,
          as: 'ptUser',
          include: [
            { model: User, attributes: ['firstName', 'lastName'] },
            { model: Location, as: 'location' }
          ]
        }
      ],
      order: [['averageRating', 'DESC'], ['createdAt', 'DESC']]
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
    console.error('Search offers error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Get offer by ID
exports.getOfferById = async (req, res) => {
  try {
    const offer = await Offer.findByPk(req.params.id, {
      include: [
        {
          model: Gym,
          as: 'gym',
          include: [{ model: Location, as: 'location' }]
        },
        {
          model: PTUser,
          as: 'ptUser',
          include: [
            { model: User, attributes: ['firstName', 'lastName', 'email'] },
            { model: Location, as: 'location' }
          ]
        },
        {
          model: User,
          as: 'creator',
          attributes: ['firstName', 'lastName', 'email']
        }
      ]
    });

    if (!offer) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'Offer not found',
        timestamp: new Date().toISOString()
      });
    }

    res.json(offer);
  } catch (error) {
    console.error('Get offer by ID error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Create offer
exports.createOffer = [
  body('title').notEmpty().withMessage('Title is required'),
  body('offerType').isIn(['GYM_OFFER', 'PT_OFFER']).withMessage('Invalid offer type'),
  body('price').isFloat({ min: 0 }).withMessage('Price must be a positive number'),

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

      const {
        title, description, offerType, price, currency,
        durationDescription, imageUrls, gymId, ptUserId
      } = req.body;

      // Validate gym or PT ownership
      if (offerType === 'GYM_OFFER') {
        if (!gymId) {
          return res.status(400).json({
            status: 400,
            error: 'Bad Request',
            message: 'gymId is required for GYM_OFFER',
            timestamp: new Date().toISOString()
          });
        }

        const gym = await Gym.findByPk(gymId);
        if (!gym) {
          return res.status(404).json({
            status: 404,
            error: 'Not Found',
            message: 'Gym not found',
            timestamp: new Date().toISOString()
          });
        }

        // Check ownership
        if (req.user.role !== 'ADMIN' && gym.ownerId !== req.user.id) {
          return res.status(403).json({
            status: 403,
            error: 'Forbidden',
            message: 'You do not have permission to create offers for this gym',
            timestamp: new Date().toISOString()
          });
        }
      }

      if (offerType === 'PT_OFFER') {
        if (!ptUserId) {
          return res.status(400).json({
            status: 400,
            error: 'Bad Request',
            message: 'ptUserId is required for PT_OFFER',
            timestamp: new Date().toISOString()
          });
        }

        const pt = await PTUser.findByPk(ptUserId);
        if (!pt) {
          return res.status(404).json({
            status: 404,
            error: 'Not Found',
            message: 'PT User not found',
            timestamp: new Date().toISOString()
          });
        }

        // Check ownership
        if (req.user.role !== 'ADMIN' && pt.userId !== req.user.id) {
          return res.status(403).json({
            status: 403,
            error: 'Forbidden',
            message: 'You do not have permission to create offers for this PT',
            timestamp: new Date().toISOString()
          });
        }
      }

      const offer = await Offer.create({
        title,
        description,
        offerType,
        price,
        currency: currency || 'USD',
        durationDescription,
        imageUrls: JSON.stringify(imageUrls || []),
        gymId: offerType === 'GYM_OFFER' ? gymId : null,
        ptUserId: offerType === 'PT_OFFER' ? ptUserId : null,
        createdBy: req.user.id,
        status: 'PENDING',
        active: true
      });

      const fullOffer = await Offer.findByPk(offer.id, {
        include: [
          { model: Gym, as: 'gym' },
          { model: PTUser, as: 'ptUser' }
        ]
      });

      res.status(201).json(fullOffer);
    } catch (error) {
      console.error('Create offer error:', error);
      res.status(500).json({
        status: 500,
        error: 'Internal Server Error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
    }
  }
];

// Update offer
exports.updateOffer = async (req, res) => {
  try {
    const offer = await Offer.findByPk(req.params.id, {
      include: [
        { model: Gym, as: 'gym' },
        { model: PTUser, as: 'ptUser' }
      ]
    });

    if (!offer) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'Offer not found',
        timestamp: new Date().toISOString()
      });
    }

    // Check ownership
    if (req.user.role !== 'ADMIN' && offer.createdBy !== req.user.id) {
      return res.status(403).json({
        status: 403,
        error: 'Forbidden',
        message: 'You do not have permission to update this offer',
        timestamp: new Date().toISOString()
      });
    }

    const {
      title, description, price, currency,
      durationDescription, imageUrls, active
    } = req.body;

    await offer.update({
      ...(title && { title }),
      ...(description && { description }),
      ...(price !== undefined && { price }),
      ...(currency && { currency }),
      ...(durationDescription && { durationDescription }),
      ...(imageUrls && { imageUrls: JSON.stringify(imageUrls) }),
      ...(active !== undefined && { active })
    });

    const updatedOffer = await Offer.findByPk(offer.id, {
      include: [
        { model: Gym, as: 'gym' },
        { model: PTUser, as: 'ptUser' }
      ]
    });

    res.json(updatedOffer);
  } catch (error) {
    console.error('Update offer error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Delete offer
exports.deleteOffer = async (req, res) => {
  try {
    const offer = await Offer.findByPk(req.params.id);

    if (!offer) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'Offer not found',
        timestamp: new Date().toISOString()
      });
    }

    // Check ownership
    if (req.user.role !== 'ADMIN' && offer.createdBy !== req.user.id) {
      return res.status(403).json({
        status: 403,
        error: 'Forbidden',
        message: 'You do not have permission to delete this offer',
        timestamp: new Date().toISOString()
      });
    }

    await offer.destroy();
    res.status(204).send();
  } catch (error) {
    console.error('Delete offer error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

