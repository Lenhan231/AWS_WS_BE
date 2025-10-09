const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const PTUser = sequelize.define('PTUser', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  bio: {
    type: DataTypes.TEXT
  },
  specializations: {
    type: DataTypes.TEXT
  },
  certifications: {
    type: DataTypes.TEXT
  },
  yearsOfExperience: {
    type: DataTypes.INTEGER
  },
  hourlyRate: {
    type: DataTypes.DECIMAL(10, 2)
  },
  profileImageUrl: {
    type: DataTypes.TEXT
  },
  active: {
    type: DataTypes.BOOLEAN,
    defaultValue: true
  },
  verified: {
    type: DataTypes.BOOLEAN,
    defaultValue: false
  },
  averageRating: {
    type: DataTypes.DECIMAL(3, 2),
    defaultValue: 0
  },
  ratingCount: {
    type: DataTypes.INTEGER,
    defaultValue: 0
  }
}, {
  tableName: 'pt_users',
  timestamps: true,
  underscored: true
});

module.exports = PTUser;

