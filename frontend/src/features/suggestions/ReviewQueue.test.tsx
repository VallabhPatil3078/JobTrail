import '@testing-library/jest-dom/vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { describe, it, expect, beforeEach } from 'vitest';
import ReviewQueue from './ReviewQueue';
import { server } from '@/test/mocks/server';
import { http, HttpResponse } from 'msw';
import { vi } from 'vitest';
import { toast } from 'sonner';

vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn()
  }
}));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
  },
});

const renderWithProviders = (component: React.ReactNode) => {
  return render(
    <QueryClientProvider client={queryClient}>
      {component}
    </QueryClientProvider>
  );
};

describe('ReviewQueue', () => {
  beforeEach(() => {
    queryClient.clear();
    vi.clearAllMocks();
  });

  it('renders suggestions and allows confirmation', async () => {
    const user = userEvent.setup();
    renderWithProviders(<ReviewQueue />);

    // Wait for data to load
    expect(await screen.findByText('Acme Corp')).toBeInTheDocument();
    expect(screen.getByText('Software Engineer')).toBeInTheDocument();

    // Find the confirm button
    const confirmBtn = screen.getByRole('button', { name: /Confirm/i });
    expect(confirmBtn).toBeInTheDocument();

    // Click confirm
    await user.click(confirmBtn);

    // Verify loading state or success toast
    // Because we mock the backend to return success, the item should eventually disappear
    // but the component might still show loading or success message depending on our UI
    expect(toast.success).toHaveBeenCalledWith(expect.stringContaining('Confirmed'));
  });

  it('handles 409 conflict during confirmation', async () => {
    const user = userEvent.setup();
    server.use(
      http.post('http://localhost:8080/api/suggestions/:id/confirm', () => {
        return HttpResponse.json({ title: 'Application already exists' }, { status: 409 });
      })
    );
    
    renderWithProviders(<ReviewQueue />);
    expect(await screen.findByText('Acme Corp')).toBeInTheDocument();
    
    const confirmBtn = screen.getByRole('button', { name: /Confirm/i });
    await user.click(confirmBtn);
    
    expect(toast.error).toHaveBeenCalledWith('Application already exists');
  });
});
