import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function main() {
  const adminEmail = 'admin@tgmc.app';
  const adminPassword = 'admin';

  // Check if admin exists
  const existingAdmin = await prisma.admin.findUnique({
    where: { email: adminEmail }
  });

  if (!existingAdmin) {
    const passwordHash = await bcrypt.hash(adminPassword, 10);
    await prisma.admin.create({
      data: {
        email: adminEmail,
        passwordHash,
        displayName: 'Platform Admin'
      }
    });
    console.log(`✅ Default admin created: ${adminEmail} / ${adminPassword}`);
  } else {
    console.log(`✅ Admin user already exists: ${adminEmail}`);
  }

  // ── Seed Parent Account for the Android App ──
  const parentEmail = 'parent@tgmc.app';
  const parentPassword = 'password';

  const existingParent = await prisma.parent.findUnique({
    where: { email: parentEmail }
  });

  if (!existingParent) {
    const passwordHash = await bcrypt.hash(parentPassword, 12);
    await prisma.parent.create({
      data: {
        email: parentEmail,
        passwordHash,
        displayName: 'Test Parent'
      }
    });
    console.log(`✅ Default parent created: ${parentEmail} / ${parentPassword}`);
  } else {
    console.log(`✅ Parent user already exists: ${parentEmail}`);
  }
}

main()
  .catch(e => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
