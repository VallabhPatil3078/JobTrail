import { useQuery } from '@tanstack/react-query';
import api from '@/api/axios';

export interface FunnelStats {
  total: number;
  active: number;
  responseRate: number;
  emailDetectedPct: number;
  stages: {
    OA: number;
    INTERVIEW: number;
    OFFER: number;
  };
}

export const useStats = () => {
  return useQuery({
    queryKey: ['stats'],
    queryFn: async (): Promise<FunnelStats> => {
      const res = await api.get('/stats');
      return res.data;
    },
  });
};
