const { PTUser, User, Location, Gym } = require('../models');
const { body, validationResult } = require('express-validator');
const { Op } = require('sequelize');

// Get all PT users with pagination
exports.getAllPTs = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    const { count, rows } = await PTUser.findAndCountAll({
      limit: size,
      offset,
      include: [
        { model: User, attributes: ['firstName', 'lastName', 'email', 'phoneNumber'] },
        { model: Location, as: 'location' }
      ],
      where: { active: true },
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
    console.error('Get all PTs error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Search PT users
exports.searchPTs = async (req, res) => {
  try {
    const { query, specialization, minRate, maxRate, latitude, longitude, radiusKm } = req.query;
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    let whereClause = { active: true };

    // Text search in specializations
    if (query) {
      whereClause[Op.or] = [
        { bio: { [Op.iLike]: `%${query}%` } },
        { specializations: { [Op.iLike]: `%${query}%` } }
      ];
    }

    if (specialization) {
      whereClause.specializations = { [Op.iLike]: `%${specialization}%` };
    }

    // Rate filter
    if (minRate) {
      whereClause.hourlyRate = { [Op.gte]: parseFloat(minRate) };
    }
    if (maxRate) {
      whereClause.hourlyRate = { 
        ...whereClause.hourlyRate,
        [Op.lte]: parseFloat(maxRate) 
      };
    }

    // Location-based search
    if (latitude && longitude && radiusKm) {
      const radius = parseFloat(radiusKm) || 10;
      
      const sql = `
        SELECT pt.*, l.*, u.first_name, u.last_name, u.email,
        ST_Distance(
          l.geolocation::geography,
          ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
        ) / 1000 AS distance_km
        FROM pt_users pt
        JOIN users u ON pt.user_id = u.id
        LEFT JOIN locations l ON pt.location_id = l.id
        WHERE pt.active = true
        AND ST_DWithin(
          l.geolocation::geography,
          ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
          :radius * 1000
        )
        ORDER BY distance_km, pt.average_rating DESC
        LIMIT :limit OFFSET :offset
      `;

      const pts = await PTUser.sequelize.query(sql, {
        replacements: {
          latitude: parseFloat(latitude),
          longitude: parseFloat(longitude),
          radius,
          limit: size,
          offset
        },
        type: PTUser.sequelize.QueryTypes.SELECT
      });

      return res.json({
        content: pts,
        pageNumber: page,
        pageSize: size,
        totalElements: pts.length,
        totalPages: 1,
        last: true,
        first: true
      });
    }

    const { count, rows } = await PTUser.findAndCountAll({
      where: whereClause,
      limit: size,
      offset,
      include: [
        { model: User, attributes: ['firstName', 'lastName', 'email', 'phoneNumber'] },
        { model: Location, as: 'location' }
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
    console.error('Search PTs error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Get PT by ID
exports.getPTById = async (req, res) => {
  try {
    const pt = await PTUser.findByPk(req.params.id, {
      include: [
        { model: User, attributes: ['firstName', 'lastName', 'email', 'phoneNumber', 'profileImageUrl'] },
        { model: Location, as: 'location' },
        { 
          model: Gym, 
          as: 'gyms',
          through: { where: { status: 'APPROVED' } },
          include: [{ model: Location, as: 'location' }]
        }
      ]
    });

    if (!pt) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'PT User not found',
        timestamp: new Date().toISOString()
      });
    }

    res.json(pt);
  } catch (error) {
    console.error('Get PT by ID error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Create PT profile
exports.createPT = [
  body('bio').optional(),
  body('specializations').optional(),
  body('hourlyRate').optional().isFloat({ min: 0 }),

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

      // Check if PT profile already exists for this user
      const existing = await PTUser.findOne({ where: { userId: req.user.id } });
      if (existing) {
        return res.status(400).json({
          status: 400,
          error: 'Bad Request',
          message: 'PT profile already exists for this user',
          timestamp: new Date().toISOString()
        });
      }

      const {
        bio, specializations, certifications, yearsOfExperience, hourlyRate,
        profileImageUrl, address, city, state, country, postalCode, latitude, longitude
      } = req.body;

      // Create location if provided
      let locationId = null;
      if (latitude && longitude) {
        const location = await Location.create({
          latitude,
          longitude,
          address,
          city,
          state,
          country,
          postalCode,
          formattedAddress: address ? `${address}, ${city}, ${state}, ${country}` : null
        });
        locationId = location.id;
      }

      // Create PT profile
      const pt = await PTUser.create({
        userId: req.user.id,
        bio,
        specializations,
        certifications,
        yearsOfExperience,
        hourlyRate,
        profileImageUrl,
        locationId,
        active: true,
        verified: false
      });

      const fullPT = await PTUser.findByPk(pt.id, {
        include: [
          { model: User, attributes: ['firstName', 'lastName', 'email'] },
          { model: Location, as: 'location' }
        ]
      });

      res.status(201).json(fullPT);
    } catch (error) {
      console.error('Create PT error:', error);
      res.status(500).json({
        status: 500,
        error: 'Internal Server Error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
    }
  }
];

// Update PT profile
exports.updatePT = async (req, res) => {
  try {
    const pt = await PTUser.findByPk(req.params.id, {
      include: [{ model: Location, as: 'location' }]
    });

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
        message: 'You do not have permission to update this PT profile',
        timestamp: new Date().toISOString()
      });
    }

    const {
      bio, specializations, certifications, yearsOfExperience, hourlyRate,
      profileImageUrl, address, city, state, country, postalCode, latitude, longitude
    } = req.body;

    await pt.update({
      ...(bio && { bio }),
      ...(specializations && { specializations }),
      ...(certifications && { certifications }),
      ...(yearsOfExperience !== undefined && { yearsOfExperience }),
      ...(hourlyRate !== undefined && { hourlyRate }),
      ...(profileImageUrl && { profileImageUrl })
    });

    // Update location
    if (pt.location && (latitude || longitude || address)) {
      await pt.location.update({
        ...(latitude && { latitude }),
        ...(longitude && { longitude }),
        ...(address && { address }),
        ...(city && { city }),
        ...(state && { state }),
        ...(country && { country }),
        ...(postalCode && { postalCode })
      });
    }

    const updatedPT = await PTUser.findByPk(pt.id, {
      include: [
        { model: User, attributes: ['firstName', 'lastName', 'email'] },
        { model: Location, as: 'location' }
      ]
    });

    res.json(updatedPT);
  } catch (error) {
    console.error('Update PT error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Delete PT profile
exports.deletePT = async (req, res) => {
  try {
    const pt = await PTUser.findByPk(req.params.id);

    if (!pt) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'PT User not found',
        timestamp: new Date().toISOString()
      });
    }

    await pt.destroy();
    res.status(204).send();
  } catch (error) {
    console.error('Delete PT error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

