const { Gym, Location, User, PTUser, GymPTAssociation } = require('../models');
const { body, validationResult } = require('express-validator');
const { Op } = require('sequelize');

// Get all gyms with pagination
exports.getAllGyms = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    const { count, rows } = await Gym.findAndCountAll({
      limit: size,
      offset,
      include: [
        { model: Location, as: 'location' },
        { model: User, as: 'owner', attributes: ['id', 'firstName', 'lastName', 'email'] }
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
    console.error('Get all gyms error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Search gyms by location or text
exports.searchGyms = async (req, res) => {
  try {
    const { query, latitude, longitude, radiusKm } = req.query;
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    let whereClause = { active: true };
    let locationWhere = {};

    // Text search
    if (query) {
      whereClause[Op.or] = [
        { name: { [Op.iLike]: `%${query}%` } },
        { description: { [Op.iLike]: `%${query}%` } }
      ];
    }

    // Location-based search
    let gyms;
    if (latitude && longitude && radiusKm) {
      const radius = parseFloat(radiusKm) || 10;

      // Use raw SQL for PostGIS distance query
      const sql = `
        SELECT g.*, l.*, 
        ST_Distance(
          l.geolocation::geography,
          ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
        ) / 1000 AS distance_km
        FROM gyms g
        JOIN locations l ON g.location_id = l.id
        WHERE g.active = true
        AND ST_DWithin(
          l.geolocation::geography,
          ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
          :radius * 1000
        )
        ORDER BY distance_km
        LIMIT :limit OFFSET :offset
      `;

      gyms = await Gym.sequelize.query(sql, {
        replacements: {
          latitude: parseFloat(latitude),
          longitude: parseFloat(longitude),
          radius,
          limit: size,
          offset
        },
        type: Gym.sequelize.QueryTypes.SELECT
      });

      return res.json({
        content: gyms,
        pageNumber: page,
        pageSize: size,
        totalElements: gyms.length,
        totalPages: 1,
        last: true,
        first: true
      });
    }

    // Regular search without geo-location
    const { count, rows } = await Gym.findAndCountAll({
      where: whereClause,
      limit: size,
      offset,
      include: [
        { model: Location, as: 'location' },
        { model: User, as: 'owner', attributes: ['id', 'firstName', 'lastName'] }
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
    console.error('Search gyms error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Get gym by ID
exports.getGymById = async (req, res) => {
  try {
    const gym = await Gym.findByPk(req.params.id, {
      include: [
        { model: Location, as: 'location' },
        { model: User, as: 'owner', attributes: ['id', 'firstName', 'lastName', 'email'] }
      ]
    });

    if (!gym) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'Gym not found',
        timestamp: new Date().toISOString()
      });
    }

    res.json(gym);
  } catch (error) {
    console.error('Get gym by ID error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Create gym
exports.createGym = [
  body('name').notEmpty().withMessage('Gym name is required'),
  body('latitude').isFloat({ min: -90, max: 90 }).withMessage('Invalid latitude'),
  body('longitude').isFloat({ min: -180, max: 180 }).withMessage('Invalid longitude'),

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
        name, description, logoUrl, phoneNumber, email, website,
        address, city, state, country, postalCode, latitude, longitude
      } = req.body;

      // Create location first
      const location = await Location.create({
        latitude,
        longitude,
        address,
        city,
        state,
        country,
        postalCode,
        formattedAddress: `${address}, ${city}, ${state}, ${country}`
      });

      // Create gym
      const gym = await Gym.create({
        name,
        description,
        logoUrl,
        phoneNumber,
        email,
        website,
        ownerId: req.user.id,
        locationId: location.id,
        active: true,
        verified: false
      });

      // Return gym with location
      const fullGym = await Gym.findByPk(gym.id, {
        include: [{ model: Location, as: 'location' }]
      });

      res.status(201).json(fullGym);
    } catch (error) {
      console.error('Create gym error:', error);
      res.status(500).json({
        status: 500,
        error: 'Internal Server Error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
    }
  }
];

// Update gym
exports.updateGym = async (req, res) => {
  try {
    const gym = await Gym.findByPk(req.params.id, {
      include: [{ model: Location, as: 'location' }]
    });

    if (!gym) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'Gym not found',
        timestamp: new Date().toISOString()
      });
    }

    // Check ownership (unless admin)
    if (req.user.role !== 'ADMIN' && gym.ownerId !== req.user.id) {
      return res.status(403).json({
        status: 403,
        error: 'Forbidden',
        message: 'You do not have permission to update this gym',
        timestamp: new Date().toISOString()
      });
    }

    const {
      name, description, logoUrl, phoneNumber, email, website,
      address, city, state, country, postalCode, latitude, longitude
    } = req.body;

    // Update gym
    await gym.update({
      ...(name && { name }),
      ...(description && { description }),
      ...(logoUrl && { logoUrl }),
      ...(phoneNumber && { phoneNumber }),
      ...(email && { email }),
      ...(website && { website })
    });

    // Update location if provided
    if (gym.location && (latitude || longitude || address)) {
      await gym.location.update({
        ...(latitude && { latitude }),
        ...(longitude && { longitude }),
        ...(address && { address }),
        ...(city && { city }),
        ...(state && { state }),
        ...(country && { country }),
        ...(postalCode && { postalCode })
      });
    }

    const updatedGym = await Gym.findByPk(gym.id, {
      include: [{ model: Location, as: 'location' }]
    });

    res.json(updatedGym);
  } catch (error) {
    console.error('Update gym error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Delete gym
exports.deleteGym = async (req, res) => {
  try {
    const gym = await Gym.findByPk(req.params.id);

    if (!gym) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'Gym not found',
        timestamp: new Date().toISOString()
      });
    }

    await gym.destroy();
    res.status(204).send();
  } catch (error) {
    console.error('Delete gym error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Assign PT to Gym
exports.assignPT = async (req, res) => {
  try {
    const gymId = parseInt(req.params.id);
    const ptUserId = parseInt(req.query.ptUserId);

    if (!ptUserId) {
      return res.status(400).json({
        status: 400,
        error: 'Bad Request',
        message: 'ptUserId is required',
        timestamp: new Date().toISOString()
      });
    }

    const gym = await Gym.findByPk(gymId);
    const ptUser = await PTUser.findByPk(ptUserId);

    if (!gym || !ptUser) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'Gym or PT User not found',
        timestamp: new Date().toISOString()
      });
    }

    // Check if association already exists
    const existing = await GymPTAssociation.findOne({
      where: { gymId, ptUserId }
    });

    if (existing) {
      return res.status(400).json({
        status: 400,
        error: 'Bad Request',
        message: 'PT is already assigned to this gym',
        timestamp: new Date().toISOString()
      });
    }

    // Create association with PENDING status
    const association = await GymPTAssociation.create({
      gymId,
      ptUserId,
      status: 'PENDING'
    });

    res.status(201).json(association);
  } catch (error) {
    console.error('Assign PT error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Get gym's PTs
exports.getGymPTs = async (req, res) => {
  try {
    const gym = await Gym.findByPk(req.params.id, {
      include: [{
        model: PTUser,
        as: 'personalTrainers',
        through: {
          where: { status: 'APPROVED' }
        },
        include: [
          { model: User, attributes: ['firstName', 'lastName', 'email'] },
          { model: Location, as: 'location' }
        ]
      }]
    });

    if (!gym) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'Gym not found',
        timestamp: new Date().toISOString()
      });
    }

    res.json(gym.personalTrainers);
  } catch (error) {
    console.error('Get gym PTs error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};
