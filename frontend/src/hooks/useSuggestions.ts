import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/api/axios';

export interface SuggestedApplication {
  id: number;
  extractedCompany: string;
  extractedRole: string | null;
  extractedDate: string;
  confidenceScore: number;
  status: 'PENDING' | 'CONFIRMED' | 'REJECTED';
  createdAt: string;
  rawEmail: {
    id: number;
    subject: string;
    sender: string;
    snippet: string;
    receivedAt: string;
  };
}

export const useSuggestions = () => {
  return useQuery({
    queryKey: ['suggestions'],
    queryFn: async (): Promise<SuggestedApplication[]> => {
      const res = await api.get('/suggestions');
      return res.data;
    },
  });
};

export const useConfirmSuggestion = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, company, role, createReminder }: { id: number; company: string; role?: string; createReminder?: boolean }) => {
      const res = await api.post(`/suggestions/${id}/confirm`, { company, role, createReminder });
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['suggestions'] });
      queryClient.invalidateQueries({ queryKey: ['applications'] });
      queryClient.invalidateQueries({ queryKey: ['stats'] });
      queryClient.invalidateQueries({ queryKey: ['reminders'] });
    },
  });
};

export const useRejectSuggestion = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const res = await api.post(`/suggestions/${id}/reject`);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['suggestions'] });
    },
  });
};
