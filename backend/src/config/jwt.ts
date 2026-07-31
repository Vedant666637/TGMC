import jwt from 'jsonwebtoken';

const ACCESS_SECRET  = process.env.JWT_ACCESS_SECRET  || 'dev-access-secret';
const REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'dev-refresh-secret';
const ACCESS_EXPIRES  = process.env.JWT_ACCESS_EXPIRES  || '30d';
const REFRESH_EXPIRES = process.env.JWT_REFRESH_EXPIRES || '30d';

export interface JwtPayload {
  userId: string;
  email: string;
  role: 'PARENT' | 'ADMIN';
}

export const generateAccessToken = (payload: JwtPayload): string =>
  jwt.sign(payload, ACCESS_SECRET, { expiresIn: ACCESS_EXPIRES } as any);

export const generateRefreshToken = (userId: string): string =>
  jwt.sign({ userId }, REFRESH_SECRET, { expiresIn: REFRESH_EXPIRES } as any);

export const verifyAccessToken = (token: string): JwtPayload =>
  jwt.verify(token, ACCESS_SECRET) as JwtPayload;

export const verifyRefreshToken = (token: string): { userId: string } =>
  jwt.verify(token, REFRESH_SECRET) as { userId: string };
