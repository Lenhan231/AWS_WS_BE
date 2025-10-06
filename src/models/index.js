const sequelize = require('../config/database');

// Import all models
const User = require('./User');
const Location = require('./Location');
const Gym = require('./Gym');
const PTUser = require('./PTUser');
const Offer = require('./Offer');
const Rating = require('./Rating');
const Report = require('./Report');
const GymPTAssociation = require('./GymPTAssociation');

// Define relationships

// User relationships
User.hasOne(PTUser, { foreignKey: 'userId', as: 'ptProfile' });
PTUser.belongsTo(User, { foreignKey: 'userId', as: 'user' });

User.hasMany(Gym, { foreignKey: 'ownerId', as: 'ownedGyms' });
Gym.belongsTo(User, { foreignKey: 'ownerId', as: 'owner' });

// Location relationships (FIX: không dùng hasOne cho nhiều gyms/pts)
Gym.belongsTo(Location, { foreignKey: 'locationId', as: 'location' });
Location.hasMany(Gym, { foreignKey: 'locationId', as: 'gyms' });

PTUser.belongsTo(Location, { foreignKey: 'locationId', as: 'location' });
Location.hasMany(PTUser, { foreignKey: 'locationId', as: 'ptUsers' });

// Gym-PT Many-to-Many relationship with approval
Gym.belongsToMany(PTUser, {
  through: GymPTAssociation,
  foreignKey: 'gymId',
  otherKey: 'ptUserId',
  as: 'personalTrainers'
});
PTUser.belongsToMany(Gym, {
  through: GymPTAssociation,
  foreignKey: 'ptUserId',
  otherKey: 'gymId',
  as: 'gyms'
});

// Direct access to association table
Gym.hasMany(GymPTAssociation, { foreignKey: 'gymId', as: 'ptAssociations' });
GymPTAssociation.belongsTo(Gym, { foreignKey: 'gymId', as: 'gym' });

PTUser.hasMany(GymPTAssociation, { foreignKey: 'ptUserId', as: 'gymAssociations' });
GymPTAssociation.belongsTo(PTUser, { foreignKey: 'ptUserId', as: 'ptUser' });

// Offer relationships
Gym.hasMany(Offer, { foreignKey: 'gymId', as: 'offers' });
Offer.belongsTo(Gym, { foreignKey: 'gymId', as: 'gym' });

PTUser.hasMany(Offer, { foreignKey: 'ptUserId', as: 'offers' });
Offer.belongsTo(PTUser, { foreignKey: 'ptUserId', as: 'ptUser' });

User.hasMany(Offer, { foreignKey: 'createdBy', as: 'createdOffers' });
Offer.belongsTo(User, { foreignKey: 'createdBy', as: 'creator' });

// Rating relationships
User.hasMany(Rating, { foreignKey: 'userId', as: 'ratings' });
Rating.belongsTo(User, { foreignKey: 'userId', as: 'user' });

Offer.hasMany(Rating, { foreignKey: 'offerId', as: 'ratings' });
Rating.belongsTo(Offer, { foreignKey: 'offerId', as: 'offer' });

// Report relationships
User.hasMany(Report, { foreignKey: 'reporterId', as: 'submittedReports' });
Report.belongsTo(User, { foreignKey: 'reporterId', as: 'reporter' });

User.hasMany(Report, { foreignKey: 'reportedUserId', as: 'receivedReports' });
Report.belongsTo(User, { foreignKey: 'reportedUserId', as: 'reportedUser' });

Offer.hasMany(Report, { foreignKey: 'offerId', as: 'reports' });
Report.belongsTo(Offer, { foreignKey: 'offerId', as: 'offer' });

User.hasMany(Report, { foreignKey: 'reviewedBy', as: 'reviewedReports' });
Report.belongsTo(User, { foreignKey: 'reviewedBy', as: 'reviewer' });

// Export all models and sequelize instance
module.exports = {
  sequelize,
  User,
  Location,
  Gym,
  PTUser,
  Offer,
  Rating,
  Report,
  GymPTAssociation
};
