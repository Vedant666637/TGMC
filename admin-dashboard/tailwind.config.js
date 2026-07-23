/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      fontFamily: { sans: ['Inter', 'system-ui', 'sans-serif'] },
      colors: {
        navy:    { 900: '#0A0E1A', 800: '#0F1628', 700: '#162040', 600: '#1E2D5A' },
        cyan:    { 400: '#00E5FF', 300: '#40EEFF' },
        surface: { 900: '#111827', 800: '#1C2536', 700: '#232F45', 600: '#2D3C56' },
        indigo:  { 400: '#6C63FF' },
        success: '#00D68F',
        warning: '#FFB347',
        danger:  '#FF4D6D',
      },
    },
  },
  plugins: [],
};
