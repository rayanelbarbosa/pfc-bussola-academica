// Teste de carga do RNF08: 50 usuários simultâneos por 1 minuto, só leitura (não cria dados).
// Critério: 95% das requisições em até 2 s e menos de 1% de erros.
// Uso: k6 run -e BASE_URL=http://localhost:8080 -e EMAIL=<estudante de teste> -e PASSWORD=<senha> docs/evidencias/k6-rnf08.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 50,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.01'],
  },
};

// Um login só, antes da carga: o token é compartilhado (o bloqueio de 5 falhas e o custo do BCrypt não entram na medição).
export function setup() {
  const res = http.post(`${BASE}/api/auth/login`,
    JSON.stringify({ email: __ENV.EMAIL, password: __ENV.PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } });
  check(res, { 'login 200': (r) => r.status === 200 });
  return { token: res.json('token') };
}

export default function (data) {
  const auth = { headers: { Authorization: `Bearer ${data.token}` } };
  check(http.get(`${BASE}/api/admission-methods`), { 'formas de ingresso 200': (r) => r.status === 200 });
  check(http.get(`${BASE}/api/terms/current`), { 'termos 200': (r) => r.status === 200 });
  check(http.get(`${BASE}/api/vocational-test/questions`, auth), { 'perguntas 200': (r) => r.status === 200 });
  check(http.get(`${BASE}/api/vocational-test/interest-areas`, auth), { 'áreas 200': (r) => r.status === 200 });
  check(http.get(`${BASE}/api/users/me/results`, auth), { 'meus resultados 200': (r) => r.status === 200 });
  sleep(1);
}
