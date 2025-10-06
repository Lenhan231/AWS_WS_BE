const { Gym, PTUser, Offer, Location, User } = require('../models');
const { Op } = require('sequelize');

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
        { model: User, attributes: ['firstName', 'lastName', 'email'] },
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

