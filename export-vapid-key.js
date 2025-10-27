import { createPublicKey } from 'crypto';
import fs from 'fs';

const pem = fs.readFileSync('vapid_public.pem', 'utf8');

// export as raw point (uncompressed)
const keyObject = createPublicKey(pem);
const raw = keyObject.export({ format: 'der', type: 'spki' });
const publicKeyBytes = raw.slice(-65);

console.log('Length:', publicKeyBytes.length); // must be 65
console.log('Starts with 0x04:', publicKeyBytes[0] === 4);

// Base64URL encode for VAPID
const base64url = publicKeyBytes.toString('base64')
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
console.log('Base64URL public key:', base64url);