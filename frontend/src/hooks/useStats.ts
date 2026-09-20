import { useQuery } from '@tanstack/react-query';
import api from '@/api/axios';

export interface FunnelStats {
  appliedCount: number;
  oaCount: number;
  interviewCount: number;
  offerCount: number;
  rejectedCount: number;
  withdrawnCount: number;
}

export const useStats = () => {
  return useQuery({
    queryKey: ['stats'],
    queryFn: async (): Promise<FunnelStats> => {
      const res = await api.get('/stats/funnel');
      return res.data;
    },
  });
};
