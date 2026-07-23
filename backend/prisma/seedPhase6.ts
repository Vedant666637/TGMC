import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function main() {
  console.log('Seeding Phase 6 Educational Data...');

  // 1. Create a mock Admin if none exists
  let admin = await prisma.admin.findFirst();
  if (!admin) {
    admin = await prisma.admin.create({
      data: {
        email: 'admin@tgmc.com',
        passwordHash: await bcrypt.hash('admin123', 10),
      }
    });
    console.log('Created mock Admin user.');
  }

  // 2. Seed Educational Content (Statuses, Posts, Reels, Videos)
  const count = await prisma.educationalContent.count();
  if (count === 0) {
    await prisma.educationalContent.createMany({
      data: [
        {
          type: 'STATUS',
          title: 'Daily Tech Tip',
          description: 'Always think before you click unknown links!',
          url: 'https://example.com/status1.jpg',
          category: 'CYBER_SECURITY',
          expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000), // 24h
          adminId: admin.id
        },
        {
          type: 'POST',
          title: 'How to handle cyberbullying',
          description: 'A comprehensive guide for kids on what to do if they or a friend are being bullied online.',
          url: 'https://example.com/post1.png',
          thumbnailUrl: 'https://example.com/thumb1.png',
          category: 'MENTAL_HEALTH',
          adminId: admin.id
        },
        {
          type: 'REEL',
          title: '5 Minute Focus Hack',
          description: 'Try the Pomodoro technique to finish homework faster.',
          url: 'https://example.com/reel1.mp4',
          durationSecs: 45,
          category: 'PRODUCTIVITY',
          adminId: admin.id
        },
        {
          type: 'VIDEO',
          title: 'Understanding Digital Footprints',
          description: 'Everything you post online stays there. Learn how to manage your digital footprint.',
          url: 'https://example.com/video1.mp4',
          durationSecs: 320,
          category: 'DIGITAL_CITIZENSHIP',
          adminId: admin.id
        }
      ]
    });
    console.log('Seeded Educational Content.');
  } else {
    console.log('Educational Content already seeded.');
  }

  // 3. Seed Store Items
  const storeCount = await prisma.storeItem.count();
  if (storeCount === 0) {
    await prisma.storeItem.createMany({
      data: [
        {
          name: 'Kid-Safe GPS Smartwatch',
          description: 'A durable smartwatch with SOS and location tracking built-in.',
          price: 49.99,
          imageUrl: 'https://example.com/watch.png',
          adminId: admin.id
        },
        {
          name: 'Digital Safety Course (Kids)',
          description: 'An interactive 4-part video course on internet safety.',
          price: 19.99,
          imageUrl: 'https://example.com/course.png',
          adminId: admin.id
        },
        {
          name: 'Screen Time Blocking Glasses',
          description: 'Blue-light filtering glasses for evening screen use.',
          price: 14.50,
          imageUrl: 'https://example.com/glasses.png',
          adminId: admin.id
        }
      ]
    });
    console.log('Seeded Educational Store.');
  } else {
    console.log('Educational Store already seeded.');
  }

  console.log('Phase 6 Seeding Complete!');
}

main()
  .catch(e => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
