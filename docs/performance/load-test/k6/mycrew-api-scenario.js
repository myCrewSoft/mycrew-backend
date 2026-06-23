import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost';
const EMP_ID = Number(__ENV.EMP_ID || '1234');
const PASSWORD = __ENV.PASSWORD || '1234';
const VUS = Number(__ENV.K6_VUS || '50');
const DURATION = __ENV.K6_DURATION || '5m';
const THINK_TIME_SECONDS = Number(__ENV.K6_THINK_TIME_SECONDS || '1');

export const options = {
  scenarios: {
    mycrew_api_load: {
      executor: 'constant-vus',
      vus: VUS,
      duration: DURATION,
      gracefulStop: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
    checks: ['rate>0.99'],
  },
};

const apiHeaders = (token) => ({
  Authorization: `Bearer ${token}`,
  'Content-Type': 'application/json',
  Accept: 'application/json',
});

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({
      empId: EMP_ID,
      password: PASSWORD,
    }),
    {
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      tags: {
        name: 'POST /api/v1/auth/login',
      },
    },
  );

  check(loginRes, {
    'login status is 200': (res) => res.status === 200,
    'login returns access token': (res) => Boolean(res.json('data.accessToken')),
  });

  const accessToken = loginRes.json('data.accessToken');
  if (!accessToken) {
    throw new Error('Login failed. Set valid EMP_ID and PASSWORD environment variables.');
  }

  return {
    accessToken,
  };
}

export default function (data) {
  const headers = apiHeaders(data.accessToken);

  group('dashboard widgets', () => {
    const responses = http.batch([
      ['GET', `${BASE_URL}/api/dashboard/widgets/attendance`, null, { headers, tags: { name: 'GET /api/dashboard/widgets/attendance' } }],
      ['GET', `${BASE_URL}/api/dashboard/widgets/approval`, null, { headers, tags: { name: 'GET /api/dashboard/widgets/approval' } }],
      ['GET', `${BASE_URL}/api/dashboard/widgets/schedule`, null, { headers, tags: { name: 'GET /api/dashboard/widgets/schedule' } }],
      ['GET', `${BASE_URL}/api/dashboard/widgets/mail`, null, { headers, tags: { name: 'GET /api/dashboard/widgets/mail' } }],
    ]);

    responses.forEach((res) => {
      check(res, {
        'dashboard response is 200': (response) => response.status === 200,
        'dashboard response success': (response) => response.json('success') === true,
      });
    });
  });

  group('attendance', () => {
    getJson('/api/attendance/today', headers, 'GET /api/attendance/today');
    getJson('/api/attendance/stats?period=WEEK', headers, 'GET /api/attendance/stats');
    getJson('/api/attendance/history?days=30', headers, 'GET /api/attendance/history');
  });

  group('mail', () => {
    getJson('/api/mails/account/status', headers, 'GET /api/mails/account/status');
    getJson('/api/mails/unread-count', headers, 'GET /api/mails/unread-count');
    getJson('/api/mails?type=inbox&page=0&size=20', headers, 'GET /api/mails');
  });

  group('approval', () => {
    getJson('/api/approval/counts', headers, 'GET /api/approval/counts');
    getJson('/api/approval/drafts?page=0&size=10', headers, 'GET /api/approval/drafts');
    getJson('/api/approval/requests?page=0&size=10', headers, 'GET /api/approval/requests');
  });

  sleep(THINK_TIME_SECONDS);
}

function getJson(path, headers, name) {
  const res = http.get(`${BASE_URL}${path}`, {
    headers,
    tags: {
      name,
    },
  });

  check(res, {
    [`${name} status is 200`]: (response) => response.status === 200,
    [`${name} success is true`]: (response) => response.json('success') === true,
  });
}
