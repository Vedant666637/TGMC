import { Router, Response } from 'express';
import { prisma } from '../../config/database';
import { authMiddleware, parentOnlyMiddleware, AuthRequest } from '../../middleware/authMiddleware';

export const webfilterRouter = Router();
webfilterRouter.use(authMiddleware, parentOnlyMiddleware);

// Default blocked domains (safety net — always blocked)
const DEFAULT_BLOCKED_CATEGORIES = [
  'ADULT', 'GAMBLING', 'DRUGS', 'VIOLENCE', 'MALWARE'
];

// GET /api/webfilter/:deviceId/rules
webfilterRouter.get('/:deviceId/rules', async (req: AuthRequest, res: Response) => {
  const rules = await prisma.webFilterRule.findMany({
    where: { deviceId: req.params.deviceId },
    orderBy: { createdAt: 'desc' }
  });
  res.json(rules);
});

// POST /api/webfilter/:deviceId/rules  — add a new filter rule
webfilterRouter.post('/:deviceId/rules', async (req: AuthRequest, res: Response) => {
  const { ruleType, value, isBlocked } = req.body;
  // ruleType: "DOMAIN" | "KEYWORD" | "CATEGORY"
  // value: e.g. "facebook.com", "porn", "ADULT"
  const rule = await prisma.webFilterRule.upsert({
    where: {
      deviceId_ruleType_value: {
        deviceId: req.params.deviceId,
        ruleType: ruleType.toUpperCase(),
        value: value.toLowerCase()
      }
    },
    update: { isBlocked: isBlocked ?? true },
    create: {
      deviceId: req.params.deviceId,
      ruleType: ruleType.toUpperCase(),
      value: value.toLowerCase(),
      isBlocked: isBlocked ?? true
    }
  });
  res.status(201).json(rule);
});

// DELETE /api/webfilter/:deviceId/rules/:ruleId
webfilterRouter.delete('/:deviceId/rules/:ruleId', async (req: AuthRequest, res: Response) => {
  await prisma.webFilterRule.deleteMany({
    where: { id: req.params.ruleId, deviceId: req.params.deviceId }
  });
  res.status(204).send();
});

// POST /api/webfilter/:deviceId/seed-defaults — populate safety defaults
webfilterRouter.post('/:deviceId/seed-defaults', async (req: AuthRequest, res: Response) => {
  const deviceId = req.params.deviceId;

  // Seed default blocked categories
  for (const category of DEFAULT_BLOCKED_CATEGORIES) {
    await prisma.webFilterRule.upsert({
      where: { deviceId_ruleType_value: { deviceId, ruleType: 'CATEGORY', value: category.toLowerCase() } },
      update: {},
      create: { deviceId, ruleType: 'CATEGORY', value: category.toLowerCase(), isBlocked: true }
    });
  }

  // Seed common dangerous domains
  const dangerousDomains = [
    'pornhub.com', 'xvideos.com', 'xnxx.com', 'redtube.com',
    'bet365.com', 'draftkings.com', 'fanduel.com',
    'thepiratebay.org', '4chan.org'
  ];
  for (const domain of dangerousDomains) {
    await prisma.webFilterRule.upsert({
      where: { deviceId_ruleType_value: { deviceId, ruleType: 'DOMAIN', value: domain } },
      update: {},
      create: { deviceId, ruleType: 'DOMAIN', value: domain, isBlocked: true }
    });
  }

  const allRules = await prisma.webFilterRule.findMany({ where: { deviceId } });
  res.json({ seeded: true, totalRules: allRules.length, rules: allRules });
});
