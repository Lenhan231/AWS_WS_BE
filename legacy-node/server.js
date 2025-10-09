require('dotenv').config();
const app = require('./src/app');
const db = require('./src/models');
const seedDatabase = require('./src/config/seed');

const PORT = process.env.PORT || 8080;

// Database connection and server start
async function startServer() {
  try {
    // Test database connection
    await db.sequelize.authenticate();
    console.log('✅ Database connection established successfully.');

    // Sync database (in development)
    if (process.env.NODE_ENV === 'development') {
      // FIX: Dùng force: true để rebuild schema (chỉ dùng 1 lần)
      await db.sequelize.sync({ force: true });
      console.log('✅ Database synchronized with force rebuild.');

      // Seed sample data
      await seedDatabase();
    }

    // Start server
    app.listen(PORT, () => {
      console.log(`🚀 Server is running on port ${PORT}`);
      console.log(`📡 API Base URL: http://localhost:${PORT}/api/${process.env.API_VERSION}`);
      console.log(`🌍 Environment: ${process.env.NODE_ENV}`);
    });
  } catch (error) {
    console.error('❌ Unable to start server:', error);
    process.exit(1);
  }
}

// Handle unhandled promise rejections
process.on('unhandledRejection', (err) => {
  console.error('❌ UNHANDLED REJECTION! Shutting down...');
  console.error(err);
  process.exit(1);
});

startServer();
