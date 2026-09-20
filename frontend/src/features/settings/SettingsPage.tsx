import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/api/axios';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { LoadingState } from '@/components/ui/LoadingState';
import { ErrorState } from '@/components/ui/ErrorState';
import { Mail, CheckCircle2, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';

interface GmailStatus {
  connected: boolean;
  email: string | null;
}

export default function SettingsPage() {
  const queryClient = useQueryClient();
  
  const { data: status, isLoading, isError, refetch } = useQuery({
    queryKey: ['gmail-status'],
    queryFn: async (): Promise<GmailStatus> => {
      const res = await api.get('/gmail/status');
      return res.data;
    },
  });

  const syncMutation = useMutation({
    mutationFn: async () => {
      await api.post('/gmail/sync');
    },
    onSuccess: () => {
      toast.success('Started syncing emails from Gmail');
      queryClient.invalidateQueries({ queryKey: ['suggestions'] });
    },
    onError: () => {
      toast.error('Failed to sync emails');
    }
  });

  const handleConnect = () => {
    // The backend provides a redirect endpoint for OAuth
    window.location.href = 'http://localhost:8080/api/gmail/connect';
  };

  if (isLoading) return <LoadingState message="Loading settings..." />;
  if (isError) return <ErrorState onRetry={() => refetch()} />;

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Settings</h1>
        <p className="text-muted-foreground mt-1">
          Manage your integrations and account preferences.
        </p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Mail className="w-5 h-5" />
            <CardTitle>Gmail Integration</CardTitle>
          </div>
          <CardDescription>
            Connect your Gmail account to automatically scan for job application emails.
            We only request read-only access.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="p-4 bg-muted/50 rounded-lg border border-border">
            {status?.connected ? (
              <div className="space-y-4">
                <div className="flex items-center gap-2 text-green-600 dark:text-green-500 font-medium">
                  <CheckCircle2 className="w-5 h-5" />
                  Connected as {status.email}
                </div>
                <Button 
                  onClick={() => syncMutation.mutate()} 
                  disabled={syncMutation.isPending}
                >
                  {syncMutation.isPending ? 'Syncing...' : 'Sync Now'}
                </Button>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="flex items-center gap-2 text-amber-600 dark:text-amber-500 font-medium">
                  <AlertCircle className="w-5 h-5" />
                  Not connected
                </div>
                <Button onClick={handleConnect}>
                  Connect Gmail
                </Button>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
