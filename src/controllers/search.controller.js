const { Gym, PTUser, Offer, Location, User } = require('../models');
const { Op } = require('sequelize');
const sequelize = require('../config/database');

// Helper function: Calculate distance between two coordinates using Haversine formula
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371; // Earth's radius in km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c; // Distance in km
};

// Unified search for gyms, PTs, and offers
exports.searchAll = async (req, res) => {
  try {
    const {
      query,
      latitude,
      longitude,
      radiusKm,
      minPrice,
      maxPrice
    } = req.query;

    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    let results = {
      gyms: [],
      pts: [],
      offers: []
    };

    // Text search condition
    const textSearch = query ? {
      [Op.or]: [
        { name: { [Op.iLike]: `%${query}%` } },
        { description: { [Op.iLike]: `%${query}%` } }
      ]
    } : {};

    // Search Gyms
    const gyms = await Gym.findAll({
      where: {
        active: true,
        ...textSearch
      },
      include: [{ model: Location, as: 'location' }],
      limit: size,
      offset
    });

    // Search PTs
    const ptTextSearch = query ? {
      [Op.or]: [
        { bio: { [Op.iLike]: `%${query}%` } },
        { specializations: { [Op.iLike]: `%${query}%` } }
      ]
    } : {};

    const pts = await PTUser.findAll({
      where: {
        active: true,
        ...ptTextSearch
      },
      include: [
        { model: User, as: 'user', attributes: ['firstName', 'lastName', 'email'] },
        { model: Location, as: 'location' }
      ],
      limit: size,
      offset
    });

    // Search Offers
    const offerTextSearch = query ? {
      [Op.or]: [
        { title: { [Op.iLike]: `%${query}%` } },
        { description: { [Op.iLike]: `%${query}%` } }
      ]
    } : {};

    let offerWhere = {
      status: 'APPROVED',
      active: true,
      ...offerTextSearch
    };

    // Price filter
    if (minPrice) {
      offerWhere.price = { [Op.gte]: parseFloat(minPrice) };
    }
    if (maxPrice) {
      offerWhere.price = {
        ...offerWhere.price,
        [Op.lte]: parseFloat(maxPrice)
      };
    }

    const offers = await Offer.findAll({
      where: offerWhere,
      include: [
        { model: Gym, as: 'gym', include: [{ model: Location, as: 'location' }] },
        { model: PTUser, as: 'ptUser', include: [{ model: Location, as: 'location' }] }
      ],
      limit: size,
      offset
    });

    results = {
      gyms,
      pts,
      offers,
      total: gyms.length + pts.length + offers.length
    };

    res.json(results);
  } catch (error) {
    console.error('Search all error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Search nearby locations (Gyms, PTs) within radius
exports.searchNearby = async (req, res) => {
  try {
    const { lat, lon, radius, type } = req.query;

    // Validate required parameters
    if (!lat || !lon || !radius) {
      return res.status(400).json({
        status: 400,
        error: 'Bad Request',
        message: 'Missing required parameters: lat, lon, radius',
        timestamp: new Date().toISOString()
      });
    }

    const userLat = parseFloat(lat);
    const userLon = parseFloat(lon);
    const radiusKm = parseFloat(radius);

    // Validate coordinate ranges
    if (userLat < -90 || userLat > 90 || userLon < -180 || userLon > 180) {
      return res.status(400).json({
        status: 400,
        error: 'Bad Request',
        message: 'Invalid coordinates. Latitude must be between -90 and 90, longitude between -180 and 180',
        timestamp: new Date().toISOString()
      });
    }

    if (radiusKm <= 0 || radiusKm > 100) {
      return res.status(400).json({
        status: 400,
        error: 'Bad Request',
        message: 'Radius must be between 0 and 100 km',
        timestamp: new Date().toISOString()
      });
    }

    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);

    let results = [];

    // Search Gyms nearby (if type is not specified or is 'gym')
    if (!type || type === 'gym') {
      const gyms = await Gym.findAll({
        where: { active: true },
        include: [{
          model: Location,
          as: 'location',
          required: true
        }]
      });

      const gymsWithDistance = gyms
        .map(gym => {
          if (!gym.location) return null;
          const distance = calculateDistance(
            userLat, userLon,
            parseFloat(gym.location.latitude),
            parseFloat(gym.location.longitude)
          );
          return {
            id: gym.id,
            name: gym.name,
            description: gym.description,
            logoUrl: gym.logoUrl,
            phoneNumber: gym.phoneNumber,
            email: gym.email,
            website: gym.website,
            averageRating: parseFloat(gym.averageRating) || 0,
            ratingCount: gym.ratingCount,
            location: {
              latitude: parseFloat(gym.location.latitude),
              longitude: parseFloat(gym.location.longitude),
              address: gym.location.address,
              city: gym.location.city,
              formattedAddress: gym.location.formattedAddress
            },
            distance: Math.round(distance * 100) / 100, // Round to 2 decimal places
            type: 'gym'
          };
        })
        .filter(gym => gym && gym.distance <= radiusKm);

      results = [...results, ...gymsWithDistance];
    }

    // Search PTs nearby (if type is not specified or is 'pt')
    if (!type || type === 'pt') {
      const pts = await PTUser.findAll({
        where: { active: true },
        include: [
          {
            model: User,
            as: 'user',
            attributes: ['firstName', 'lastName', 'email']
          },
          {
            model: Location,
            as: 'location',
            required: true
          }
        ]
      });

      const ptsWithDistance = pts
        .map(pt => {
          if (!pt.location) return null;
          const distance = calculateDistance(
            userLat, userLon,
            parseFloat(pt.location.latitude),
            parseFloat(pt.location.longitude)
          );
          return {
            id: pt.id,
            name: pt.user ? `${pt.user.firstName} ${pt.user.lastName}` : 'PT User',
            bio: pt.bio,
            specializations: pt.specializations,
            certifications: pt.certifications,
            experience: pt.experience,
            hourlyRate: pt.hourlyRate,
            availability: pt.availability,
            averageRating: parseFloat(pt.averageRating) || 0,
            ratingCount: pt.ratingCount,
            location: {
              latitude: parseFloat(pt.location.latitude),
              longitude: parseFloat(pt.location.longitude),
              address: pt.location.address,
              city: pt.location.city,
              formattedAddress: pt.location.formattedAddress
            },
            distance: Math.round(distance * 100) / 100,
            type: 'pt'
          };
        })
        .filter(pt => pt && pt.distance <= radiusKm);

      results = [...results, ...ptsWithDistance];
    }

    // Sort by distance (nearest first)
    results.sort((a, b) => a.distance - b.distance);

    // Pagination
    const total = results.length;
    const paginatedResults = results.slice(page * size, (page + 1) * size);

    res.json({
      results: paginatedResults,
      pagination: {
        page,
        size,
        total,
        totalPages: Math.ceil(total / size)
      },
      searchCriteria: {
        latitude: userLat,
        longitude: userLon,
        radius: radiusKm,
        type: type || 'all'
      }
    });

  } catch (error) {
    console.error('Nearby search error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};
