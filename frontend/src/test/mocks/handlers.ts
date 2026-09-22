import { http, HttpResponse } from 'msw';

const API_BASE = 'http://localhost:8080/api';

export const handlers = [
  http.get(`${API_BASE}/suggestions`, () => {
    return HttpResponse.json([
      {
        id: 1,
        extractedCompany: 'Acme Corp',
        extractedRole: 'Software Engineer',
        extractedDate: '2023-10-01',
        confidenceScore: 90.0,
        status: 'PENDING',
        rawEmail: {
          id: 101,
          subject: 'Application to Acme Corp',
          sender: 'no-reply@acmecorp.com',
          receivedAt: '2023-10-01T10:00:00Z',
          snippet: 'Thank you for applying...',
          processed: true,
        },
      }
    ]);
  }),

  http.post(`${API_BASE}/suggestions/:id/confirm`, () => {
    return HttpResponse.json({
      id: 1,
      company: 'Acme Corp',
      role: 'Software Engineer',
      status: 'APPLIED',
      dateApplied: '2023-10-01',
    });
  }),
  
  http.post(`${API_BASE}/suggestions/:id/reject`, () => {
    return HttpResponse.json({ success: true });
  })
];
