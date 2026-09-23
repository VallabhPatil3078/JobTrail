import { useQuery } from '@tanstack/react-query';
import api from '@/api/axios';

export interface MatchResult {
  matchPercentage: number;
  matchedKeywords: string[];
  missingKeywords: string[];
  noKeywordsFound: boolean;
}

export const useMatchResult = (applicationId: number, enabled: boolean) => {
  return useQuery({
    queryKey: ['match', applicationId],
    queryFn: async (): Promise<MatchResult> => {
      const res = await api.get(`/applications/${applicationId}/match`);
      return res.data;
    },
    enabled,
  });
};
