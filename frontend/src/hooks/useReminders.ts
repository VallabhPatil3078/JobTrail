import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/api/axios';

export interface Reminder {
  id: number;
  applicationId: number;
  company: string;
  role: string;
  type: 'FOLLOW_UP' | 'INTERVIEW_PREP';
  dueDate: string;
  createdAt: string;
}

export const useReminders = () => {
  return useQuery({
    queryKey: ['reminders'],
    queryFn: async (): Promise<Reminder[]> => {
      const res = await api.get('/reminders');
      return res.data;
    },
  });
};

export const useCompleteReminder = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.patch(`/reminders/${id}/complete`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reminders'] });
      queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
};
