import { Agent } from 'http';
import { Agent as HttpsAgent } from 'https';

// HTTP Keep-Alive agent for better performance
export function createHttpAgent() {
  return new Agent({
    keepAlive: true,
    keepAliveMsecs: 30000,
    maxSockets: 50,
    maxFreeSockets: 10,
    timeout: 10000
  });
}

export function createHttpsAgent() {
  return new HttpsAgent({
    keepAlive: true,
    keepAliveMsecs: 30000,
    maxSockets: 50,
    maxFreeSockets: 10,
    timeout: 10000
  });
}
