const { Rating, Offer, User, Gym, PTUser } = require('../models');
const { body, validationResult } = require('express-validator');

// Get ratings for an offer
exports.getOfferRatings = async (req, res) => {
  try {
    const { offerId } = req.params;
    const page = parseInt(req.query.page) || 0;
    const size = Math.min(parseInt(req.query.size) || 20, 100);
    const offset = page * size;

    const { count, rows } = await Rating.findAndCountAll({
      where: { offerId },
      limit: size,
      offset,
      include: [
        { model: User, as: 'user', attributes: ['firstName', 'lastName'] }
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
    console.error('Get offer ratings error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Create rating
exports.createRating = [
  body('offerId').notEmpty().isInt().withMessage('Offer ID is required'),
  body('rating').isInt({ min: 1, max: 5 }).withMessage('Rating must be between 1 and 5'),
  body('comment').optional(),

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

      const { offerId, rating, comment } = req.body;

      // Check if offer exists
      const offer = await Offer.findByPk(offerId);
      if (!offer) {
        return res.status(404).json({
          status: 404,
          error: 'Not Found',
          message: 'Offer not found',
          timestamp: new Date().toISOString()
        });
      }

      // Check if user already rated this offer
      const existingRating = await Rating.findOne({
        where: {
          userId: req.user.id,
          offerId
        }
      });

      if (existingRating) {
        return res.status(400).json({
          status: 400,
          error: 'Bad Request',
          message: 'You have already rated this offer',
          timestamp: new Date().toISOString()
        });
      }

      // Create rating
      const newRating = await Rating.create({
        userId: req.user.id,
        offerId,
        rating,
        comment,
        verified: false
      });

      // Update offer average rating
      await updateOfferRating(offerId);

      const fullRating = await Rating.findByPk(newRating.id, {
        include: [
          { model: User, as: 'user', attributes: ['firstName', 'lastName'] },
          { model: Offer, as: 'offer' }
        ]
      });

      res.status(201).json(fullRating);
    } catch (error) {
      console.error('Create rating error:', error);
      res.status(500).json({
        status: 500,
        error: 'Internal Server Error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
    }
  }
];

// Update rating
exports.updateRating = async (req, res) => {
  try {
    const ratingRecord = await Rating.findByPk(req.params.id);

    if (!ratingRecord) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'Rating not found',
        timestamp: new Date().toISOString()
      });
    }

    // Check ownership
    if (ratingRecord.userId !== req.user.id) {
      return res.status(403).json({
        status: 403,
        error: 'Forbidden',
        message: 'You do not have permission to update this rating',
        timestamp: new Date().toISOString()
      });
    }

    const { rating, comment } = req.body;

    await ratingRecord.update({
      ...(rating && { rating }),
      ...(comment !== undefined && { comment })
    });

    // Update offer average rating
    await updateOfferRating(ratingRecord.offerId);

    res.json(ratingRecord);
  } catch (error) {
    console.error('Update rating error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Delete rating
exports.deleteRating = async (req, res) => {
  try {
    const ratingRecord = await Rating.findByPk(req.params.id);

    if (!ratingRecord) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'Rating not found',
        timestamp: new Date().toISOString()
      });
    }

    // Check ownership or admin
    if (req.user.role !== 'ADMIN' && ratingRecord.userId !== req.user.id) {
      return res.status(403).json({
        status: 403,
        error: 'Forbidden',
        message: 'You do not have permission to delete this rating',
        timestamp: new Date().toISOString()
      });
    }

    const offerId = ratingRecord.offerId;
    await ratingRecord.destroy();

    // Update offer average rating
    await updateOfferRating(offerId);

    res.status(204).send();
  } catch (error) {
    console.error('Delete rating error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Helper function to update offer average rating
async function updateOfferRating(offerId) {
  const ratings = await Rating.findAll({ where: { offerId } });

  if (ratings.length === 0) {
    await Offer.update(
      { averageRating: 0, ratingCount: 0 },
      { where: { id: offerId } }
    );
    return;
  }

  const sum = ratings.reduce((acc, r) => acc + r.rating, 0);
  const average = sum / ratings.length;

  await Offer.update(
    {
      averageRating: average.toFixed(2),
      ratingCount: ratings.length
    },
    { where: { id: offerId } }
  );
}

