import { useState, useCallback } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useSuggestions, useConfirmSuggestion, useRejectSuggestion } from '@/hooks/useSuggestions';
import type { SuggestedApplication } from '@/hooks/useSuggestions';
import { SuggestionCard } from './SuggestionCard';
import { useKeyboardShortcuts } from '@/hooks/useKeyboardShortcuts';
import { LoadingState } from '@/components/ui/LoadingState';
import { ErrorState } from '@/components/ui/ErrorState';
import { EmptyState } from '@/components/ui/EmptyState';
import { Inbox, Keyboard } from 'lucide-react';
import { toast } from 'sonner';

export default function ReviewQueue() {
  const { data: suggestions, isLoading, isError, refetch } = useSuggestions();
  const queryClient = useQueryClient();
  const confirmMutation = useConfirmSuggestion();
  const rejectMutation = useRejectSuggestion();
  
  const [selectedIndex, setSelectedIndex] = useState(0);

  const maxIndex = suggestions ? Math.max(0, suggestions.length - 1) : 0;

  const handleNext = useCallback(() => {
    setSelectedIndex((prev) => Math.min(prev + 1, maxIndex));
  }, [maxIndex]);

  const handlePrev = useCallback(() => {
    setSelectedIndex((prev) => Math.max(prev - 1, 0));
  }, []);

  // Handle direct confirms/rejects from the card (bypassing keyboard)
  const onCardConfirm = useCallback((id: number, company: string, role?: string, createReminder?: boolean) => {
    confirmMutation.mutate(
      { id, company, role, createReminder },
      {
        onSuccess: () => {
          toast.success(`Confirmed application for ${company}`);
          setSelectedIndex((prev) => (prev >= maxIndex ? Math.max(0, maxIndex - 1) : prev));
        },
        onError: (err: any) => {
          toast.error(err.response?.data?.title || 'Failed to confirm suggestion');
        }
      }
    );
  }, [confirmMutation, maxIndex]);

  const onCardReject = useCallback((id: number) => {
    // Optimistic hide
    const currentList = queryClient.getQueryData<SuggestedApplication[]>(['suggestions']);
    if (currentList) {
      queryClient.setQueryData(['suggestions'], currentList.filter(s => s.id !== id));
    }
    
    const timer = setTimeout(() => {
      rejectMutation.mutate(id);
    }, 5000);

    toast(`Rejected suggestion`, {
      action: {
        label: 'Undo',
        onClick: () => {
          clearTimeout(timer);
          if (currentList) queryClient.setQueryData(['suggestions'], currentList);
          toast.success('Restored suggestion');
        },
      },
      duration: 5000,
    });
    setSelectedIndex((prev) => (prev >= maxIndex ? Math.max(0, maxIndex - 1) : prev));
  }, [queryClient, rejectMutation, maxIndex]);

  const handleConfirm = useCallback(() => {
    if (!suggestions || suggestions.length === 0) return;
    const current = suggestions[selectedIndex];
    onCardConfirm(current.id, current.extractedCompany, current.extractedRole || undefined);
  }, [suggestions, selectedIndex, onCardConfirm]);

  const handleReject = useCallback(() => {
    if (!suggestions || suggestions.length === 0) return;
    const current = suggestions[selectedIndex];
    onCardReject(current.id);
  }, [suggestions, selectedIndex, onCardReject]);

  useKeyboardShortcuts({
    'j': handleNext,
    'k': handlePrev,
    'J': handleNext,
    'K': handlePrev,
    'c': handleConfirm,
    'C': handleConfirm,
    'r': handleReject,
    'R': handleReject,
  });

  if (isLoading) return <LoadingState message="Loading review queue..." />;
  if (isError) return <ErrorState onRetry={() => refetch()} />;
  if (!suggestions || suggestions.length === 0) {
    return (
      <EmptyState 
        icon={Inbox}
        title="Inbox Zero!"
        description="We couldn't find any new job applications in your email. We'll keep checking."
      />
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Review Queue</h1>
          <p className="text-muted-foreground mt-1">
            We found {suggestions.length} potential {suggestions.length === 1 ? 'application' : 'applications'} in your inbox.
          </p>
        </div>
        
        <div className="hidden md:flex items-center gap-4 text-sm text-muted-foreground bg-card px-4 py-2 rounded-lg border border-border shadow-sm">
          <div className="flex items-center gap-1">
            <Keyboard className="w-4 h-4" />
            <span className="font-semibold text-foreground">J/K</span> to navigate
          </div>
          <div className="w-px h-4 bg-border"></div>
          <div className="flex items-center gap-1">
            <span className="font-semibold text-foreground">C</span> confirm
          </div>
          <div className="w-px h-4 bg-border"></div>
          <div className="flex items-center gap-1">
            <span className="font-semibold text-foreground">R</span> reject
          </div>
          <div className="w-px h-4 bg-border"></div>
          <div className="flex items-center gap-1">
            <span className="font-semibold text-foreground">E</span> edit
          </div>
        </div>
      </div>

      <div className="space-y-4 pb-12">
        {suggestions.map((s, index) => (
          <SuggestionCard 
            key={s.id} 
            suggestion={s}
            isSelected={index === selectedIndex}
            isConfirming={confirmMutation.isPending}
            isRejecting={rejectMutation.isPending}
            onSelect={() => setSelectedIndex(index)}
            onConfirm={onCardConfirm}
            onReject={onCardReject}
          />
        ))}
      </div>
    </div>
  );
}
