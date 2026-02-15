import axios from 'axios';

const PING_INTERVAL = 4 * 60 * 1000; // 4 minutes

export function startSelfPing() {
  const selfUrl = process.env.SELF_PING_URL;
  
  if (!selfUrl) {
    console.log('⚠️ SELF_PING_URL not set, skipping self-ping');
    return;
  }

  console.log(`🏓 Starting self-ping to ${selfUrl}/ping every 4 minutes`);

  setInterval(async () => {
    try {
      await axios.get(`${selfUrl}/ping`, { timeout: 5000 });
      console.log('🏓 Self-ping successful');
    } catch (error) {
      console.error('🏓 Self-ping failed:', error.message);
    }
  }, PING_INTERVAL);
}
