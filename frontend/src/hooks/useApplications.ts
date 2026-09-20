import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/api/axios';

export type ApplicationStatus = 'APPLIED' | 'OA' | 'INTERVIEW' | 'OFFER' | 'REJECTED' | 'WITHDRAWN';

export interface StatusHistory {
  id: number;
  fromStatus: ApplicationStatus;
  toStatus: ApplicationStatus;
  note: string;
  source: string;
  changedAt: string;
}

export interface Application {
  id: number;
  company: string;
  role: string;
  status: ApplicationStatus;
  dateApplied: string;
  source: string;
  createdAt: string;
  history?: StatusHistory[];
}

export const useApplications = () => {
  return useQuery({
    queryKey: ['applications'],
    queryFn: async (): Promise<Application[]> => {
      const res = await api.get('/applications');
      return res.data;
    },
  });
};

export const useApplicationHistory = (id: number, enabled: boolean) => {
  return useQuery({
    queryKey: ['applications', id, 'history'],
    queryFn: async (): Promise<StatusHistory[]> => {
      const res = await api.get(`/applications/${id}/history`);
      return res.data;
    },
    enabled,
  });
};

export const useCreateApplication = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (data: { company: string; role: string; dateApplied: string; source: string }) => {
      const res = await api.post('/applications', data);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] });
      queryClient.invalidateQueries({ queryKey: ['stats'] });
    },
  });
};

export const useUpdateApplicationStatus = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, status, note }: { id: number; status: ApplicationStatus; note?: string }) => {
      const res = await api.patch(`/applications/${id}/status`, { status, note });
      return res.data;
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['applications'] });
      queryClient.invalidateQueries({ queryKey: ['applications', variables.id, 'history'] });
      queryClient.invalidateQueries({ queryKey: ['stats'] });
    },
  });
};

export const useDeleteApplication = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/applications/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] });
      queryClient.invalidateQueries({ queryKey: ['stats'] });
    },
  });
};
