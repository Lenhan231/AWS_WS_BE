const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Location = sequelize.define('Location', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  latitude: {
    type: DataTypes.DECIMAL(10, 8),
    allowNull: false,
    validate: {
      min: -90,
      max: 90
    }
  },
  longitude: {
    type: DataTypes.DECIMAL(11, 8),
    allowNull: false,
    validate: {
      min: -180,
      max: 180
    }
  },
  address: {
    type: DataTypes.STRING
  },
  city: {
    type: DataTypes.STRING
  },
  state: {
    type: DataTypes.STRING
  },
  country: {
    type: DataTypes.STRING
  },
  postalCode: {
    type: DataTypes.STRING
  },
  formattedAddress: {
    type: DataTypes.TEXT
  },
  // PostGIS point (stored as GEOGRAPHY for accurate distance calculations)
  geolocation: {
    type: DataTypes.GEOMETRY('POINT', 4326),
    allowNull: true
  }
}, {
  tableName: 'locations',
  timestamps: true,
  underscored: true,
  hooks: {
    beforeSave: (location) => {
      // Set geolocation point from lat/lng
      if (location.latitude && location.longitude) {
        location.geolocation = {
          type: 'Point',
          coordinates: [location.longitude, location.latitude]
        };
      }
    }
  }
});

module.exports = Location;

