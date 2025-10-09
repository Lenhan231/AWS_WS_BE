const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const GymPTAssociation = sequelize.define('GymPTAssociation', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  status: {
    type: DataTypes.ENUM('PENDING', 'APPROVED', 'REJECTED'),
    defaultValue: 'PENDING'
  },
  approvedAt: {
    type: DataTypes.DATE
  },
  rejectionReason: {
    type: DataTypes.TEXT
  }
}, {
  tableName: 'gym_pt_associations',
  timestamps: true,
  underscored: true
});

module.exports = GymPTAssociation;

