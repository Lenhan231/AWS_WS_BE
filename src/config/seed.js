const bcrypt = require('bcryptjs');
const { User, Location, Gym, PTUser, Offer } = require('../models');

/**
 * Seed sample data vào database để test
 * Tự động chạy khi start server nếu database trống
 */
async function seedDatabase() {
  try {
    // Check if already has data
    const userCount = await User.count();
    if (userCount > 0) {
      console.log('📊 Database already has data. Skipping seed...');
      return;
    }

    console.log('🌱 Seeding database with sample data...');

    // 1. Create sample users
    const hashedPassword = await bcrypt.hash('Password123', 10);

    const adminUser = await User.create({
      email: 'admin@easybody.com',
      password: hashedPassword,
      firstName: 'Admin',
      lastName: 'User',
      role: 'ADMIN',
      active: true
    });

    const gymStaff = await User.create({
      email: 'gym@easybody.com',
      password: hashedPassword,
      firstName: 'John',
      lastName: 'Gym Owner',
      role: 'GYM_STAFF',
      active: true
    });

    const ptUser = await User.create({
      email: 'pt@easybody.com',
      password: hashedPassword,
      firstName: 'Jane',
      lastName: 'Trainer',
      role: 'PT_USER',
      active: true
    });

    const clientUser = await User.create({
      email: 'client@easybody.com',
      password: hashedPassword,
      firstName: 'Mike',
      lastName: 'Client',
      role: 'CLIENT_USER',
      active: true
    });

    console.log('✅ Created 4 sample users');
    console.log('   - admin@easybody.com (ADMIN)');
    console.log('   - gym@easybody.com (GYM_STAFF)');
    console.log('   - pt@easybody.com (PT_USER)');
    console.log('   - client@easybody.com (CLIENT_USER)');
    console.log('   Password for all: Password123');

    // 2. Create locations
    const location1 = await Location.create({
      latitude: 10.7769,
      longitude: 106.7009,
      address: '123 Nguyen Hue Street',
      city: 'Ho Chi Minh City',
      state: 'Ho Chi Minh',
      country: 'Vietnam',
      postalCode: '700000',
      formattedAddress: '123 Nguyen Hue Street, Ho Chi Minh City, Vietnam'
    });

    const location2 = await Location.create({
      latitude: 10.8231,
      longitude: 106.6297,
      address: '456 Le Van Viet Street',
      city: 'Ho Chi Minh City',
      state: 'Ho Chi Minh',
      country: 'Vietnam',
      postalCode: '700000',
      formattedAddress: '456 Le Van Viet Street, Ho Chi Minh City, Vietnam'
    });

    console.log('✅ Created 2 sample locations');

    // 3. Create gyms
    const gym1 = await Gym.create({
      name: 'Fitness Pro Gym',
      description: 'Premium gym with modern equipment and professional trainers',
      phoneNumber: '+84901234567',
      email: 'info@fitnesspro.com',
      website: 'https://fitnesspro.com',
      ownerId: gymStaff.id,
      locationId: location1.id,
      active: true,
      verified: true,
      averageRating: 4.5,
      ratingCount: 120
    });

    const gym2 = await Gym.create({
      name: 'Power House Gym',
      description: 'Strength training focused gym with Olympic equipment',
      phoneNumber: '+84909876543',
      email: 'contact@powerhouse.com',
      website: 'https://powerhouse.com',
      ownerId: gymStaff.id,
      locationId: location2.id,
      active: true,
      verified: true,
      averageRating: 4.8,
      ratingCount: 85
    });

    console.log('✅ Created 2 sample gyms');
    console.log('   - Fitness Pro Gym');
    console.log('   - Power House Gym');

    // 4. Create PT profile
    const pt1 = await PTUser.create({
      userId: ptUser.id,
      bio: 'Certified personal trainer with 5+ years experience in weight loss and strength training',
      specializations: 'Weight Loss, Strength Training, Yoga, Nutrition',
      certifications: 'NASM-CPT, ACE Certified, Yoga Alliance RYT-200',
      yearsOfExperience: 5,
      hourlyRate: 35.00,
      locationId: location1.id,
      active: true,
      verified: true,
      averageRating: 4.9,
      ratingCount: 67
    });

    console.log('✅ Created 1 PT profile');
    console.log('   - Jane Trainer (Yoga, Weight Loss specialist)');

    // 5. Create sample offers
    const offer1 = await Offer.create({
      title: '1 Month Premium Membership',
      description: 'Full access to all equipment, classes, and facilities for 30 days',
      offerType: 'GYM_OFFER',
      price: 50.00,
      currency: 'USD',
      durationDescription: '1 month',
      gymId: gym1.id,
      createdBy: gymStaff.id,
      status: 'APPROVED',
      active: true,
      averageRating: 4.6,
      ratingCount: 45
    });

    const offer2 = await Offer.create({
      title: '3 Month Membership - Save 20%',
      description: 'Best value! 3 months of unlimited access with 20% discount',
      offerType: 'GYM_OFFER',
      price: 120.00,
      currency: 'USD',
      durationDescription: '3 months',
      gymId: gym1.id,
      createdBy: gymStaff.id,
      status: 'APPROVED',
      active: true,
      averageRating: 4.7,
      ratingCount: 38
    });

    const offer3 = await Offer.create({
      title: '10 Personal Training Sessions',
      description: 'One-on-one personal training with certified trainer. Customized workout plan included.',
      offerType: 'PT_OFFER',
      price: 300.00,
      currency: 'USD',
      durationDescription: '10 sessions',
      ptUserId: pt1.id,
      createdBy: ptUser.id,
      status: 'APPROVED',
      active: true,
      averageRating: 5.0,
      ratingCount: 28
    });

    const offer4 = await Offer.create({
      title: 'Strength Training Package',
      description: 'Specialized strength and conditioning program at Power House Gym',
      offerType: 'GYM_OFFER',
      price: 75.00,
      currency: 'USD',
      durationDescription: '1 month',
      gymId: gym2.id,
      createdBy: gymStaff.id,
      status: 'APPROVED',
      active: true,
      averageRating: 4.8,
      ratingCount: 22
    });

    console.log('✅ Created 4 sample offers');
    console.log('   - 2 Gym membership offers');
    console.log('   - 2 PT training packages');

    console.log('\n🎉 Database seeding completed successfully!');
    console.log('\n📝 You can now login with these accounts:');
    console.log('   Email: admin@easybody.com | Password: Password123 | Role: ADMIN');
    console.log('   Email: gym@easybody.com   | Password: Password123 | Role: GYM_STAFF');
    console.log('   Email: pt@easybody.com    | Password: Password123 | Role: PT_USER');
    console.log('   Email: client@easybody.com| Password: Password123 | Role: CLIENT_USER');

  } catch (error) {
    console.error('❌ Error seeding database:', error);
  }
}

module.exports = seedDatabase;

