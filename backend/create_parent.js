const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');

const prisma = new PrismaClient();

async function main() {
  const parentEmail = 'parent@tgmc.app';
  const parentPassword = 'password';

  const existing = await prisma.parent.findUnique({ where: { email: parentEmail } });
  if (!existing) {
    const passwordHash = await bcrypt.hash(parentPassword, 12);
    await prisma.parent.create({
      data: { email: parentEmail, passwordHash, displayName: 'Test Parent' }
    });
    console.log('✅ Parent account created: ' + parentEmail + ' / ' + parentPassword);
  } else {
    console.log('✅ Parent already exists: ' + parentEmail);
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
