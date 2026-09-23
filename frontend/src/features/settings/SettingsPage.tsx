import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/api/axios';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { LoadingState } from '@/components/ui/LoadingState';
import { ErrorState } from '@/components/ui/ErrorState';
import { Mail, CheckCircle2, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';

interface GmailStatus {
  status: 'CONNECTED' | 'DISCONNECTED' | 'NEEDS_RECONNECT';
  lastSyncedAt: string;
}

import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Textarea } from '@/components/ui/textarea';

export default function SettingsPage() {
  const queryClient = useQueryClient();
  const location = useLocation();
  const navigate = useNavigate();
  const [resumeText, setResumeText] = useState('');

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('gmail_connected') === 'true') {
      toast.success('Gmail connected successfully!');
      navigate('/settings', { replace: true });
    } else if (params.get('gmail_error')) {
      toast.error('Failed to connect Gmail');
      navigate('/settings', { replace: true });
    }
  }, [location, navigate]);
  
  const { data: status, isLoading, isError, refetch } = useQuery({
    queryKey: ['gmail-status'],
    queryFn: async (): Promise<GmailStatus> => {
      const res = await api.get('/gmail/status');
      return res.data;
    },
  });

  const { data: profile } = useQuery({
    queryKey: ['user-profile'],
    queryFn: async () => {
      const res = await api.get('/users/me');
      return res.data;
    },
  });

  const updateResumeMutation = useMutation({
    mutationFn: async (text: string) => {
      await api.put('/users/me/resume', { resumeText: text });
    },
    onSuccess: () => {
      toast.success('Resume updated successfully');
      queryClient.invalidateQueries({ queryKey: ['user-profile'] });
      queryClient.invalidateQueries({ queryKey: ['match'] });
    },
    onError: () => {
      toast.error('Failed to update resume');
    }
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

  const handleConnect = async () => {
    try {
      const res = await api.get<{url: string}>('/gmail/auth-url');
      window.location.href = res.data.url;
    } catch (err) {
      console.error(err);
      toast.error('Failed to get authorization URL');
    }
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
            {status?.status === 'CONNECTED' ? (
              <div className="space-y-4">
                <div className="flex items-center gap-2 text-green-600 dark:text-green-500 font-medium">
                  <CheckCircle2 className="w-5 h-5" />
                  Connected
                </div>
                {status.lastSyncedAt && (
                  <p className="text-sm text-muted-foreground">
                    Last synced: {new Date(status.lastSyncedAt).toLocaleString()}
                  </p>
                )}
                <Button 
                  onClick={() => syncMutation.mutate()} 
                  disabled={syncMutation.isPending}
                >
                  {syncMutation.isPending ? 'Syncing...' : 'Sync Now'}
                </Button>
              </div>
            ) : status?.status === 'NEEDS_RECONNECT' ? (
              <div className="space-y-4">
                <div className="flex items-center gap-2 text-amber-600 dark:text-amber-500 font-medium">
                  <AlertCircle className="w-5 h-5" />
                  Connection needs to be renewed
                </div>
                <p className="text-sm text-muted-foreground">
                  Your Gmail connection token has expired or failed. Please reconnect.
                </p>
                <Button onClick={handleConnect}>
                  Reconnect Gmail
                </Button>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="flex items-center gap-2 text-muted-foreground font-medium">
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

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <CardTitle>Resume Text</CardTitle>
          </div>
          <CardDescription>
            Paste your resume text here. This will be used to calculate keyword overlap with job descriptions.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="space-y-2">
              <Textarea 
                placeholder="Paste your resume text here..." 
                className="min-h-[200px]"
                value={resumeText}
                onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setResumeText(e.target.value)}
              />
              <div className="text-xs text-muted-foreground text-right">
                {resumeText.length} characters
              </div>
            </div>
            <Button 
              onClick={() => updateResumeMutation.mutate(resumeText)}
              disabled={updateResumeMutation.isPending || !resumeText}
            >
              {updateResumeMutation.isPending ? 'Saving...' : 'Save Resume'}
            </Button>
            {profile?.hasResumeText && (
              <p className="text-sm text-green-600 dark:text-green-500 font-medium flex items-center gap-1">
                <CheckCircle2 className="w-4 h-4" /> Resume is currently saved
              </p>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
