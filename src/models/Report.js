const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Report = sequelize.define('Report', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  reason: {
    type: DataTypes.STRING,
    allowNull: false
  },
  details: {
    type: DataTypes.TEXT
  },
  status: {
    type: DataTypes.ENUM('PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED'),
    defaultValue: 'PENDING'
  },
  adminNotes: {
    type: DataTypes.TEXT
  }
}, {
  tableName: 'reports',
  timestamps: true,
  underscored: true
});

module.exports = Report;

