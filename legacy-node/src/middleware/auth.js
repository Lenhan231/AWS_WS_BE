const jwt = require('jsonwebtoken');
const { User, PTUser } = require('../models');

// Verify JWT token (LOCAL - không cần AWS Cognito)
const verifyToken = async (req, res, next) => {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      status: 401,
      error: 'Unauthorized',
      message: 'No token provided',
      timestamp: new Date().toISOString()
    });
  }

  const token = authHeader.substring(7);

  try {
    // Verify JWT token với secret key local
    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    // Tìm user trong database và include PTUser profile nếu có
    const user = await User.findByPk(decoded.userId, {
      include: [
        {
          model: PTUser,
          as: 'ptProfile',
          required: false // Left join, không bắt buộc phải có PT profile
        }
      ]
    });

    if (!user) {
      return res.status(401).json({
        status: 401,
        error: 'Unauthorized',
        message: 'User not found',
        timestamp: new Date().toISOString()
      });
    }

    if (!user.active) {
      return res.status(403).json({
        status: 403,
        error: 'Forbidden',
        message: 'User account is inactive',
        timestamp: new Date().toISOString()
      });
    }

    // Attach user to request
    req.user = user;
    next();
  } catch (error) {
    console.error('Auth middleware error:', error);
    return res.status(401).json({
      status: 401,
      error: 'Unauthorized',
      message: 'Invalid or expired token',
      timestamp: new Date().toISOString()
    });
  }
};

// Check user role
const checkRole = (...allowedRoles) => {
  return (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({
        status: 401,
        error: 'Unauthorized',
        message: 'Authentication required',
        timestamp: new Date().toISOString()
      });
    }

    if (!allowedRoles.includes(req.user.role)) {
      return res.status(403).json({
        status: 403,
        error: 'Forbidden',
        message: 'Insufficient permissions',
        timestamp: new Date().toISOString()
      });
    }

    next();
  };
};

module.exports = {
  verifyToken,
  checkRole
};
