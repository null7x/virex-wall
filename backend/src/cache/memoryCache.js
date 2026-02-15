import NodeCache from 'node-cache';

// Cache with 10 minute TTL
const cache = new NodeCache({
  stdTTL: 600, // 10 minutes
  checkperiod: 120, // Check for expired keys every 2 minutes
  useClones: false // For better performance
});

export default cache;
