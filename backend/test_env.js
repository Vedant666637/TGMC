const dotenv = require('dotenv');
dotenv.config();
console.log(process.env.FIREBASE_PRIVATE_KEY.substring(0, 40));
console.log(process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n').substring(0, 40));
