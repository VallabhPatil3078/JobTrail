import { useState, useCallback } from 'react';
import { useSuggestions, useConfirmSuggestion, useRejectSuggestion } from '@/hooks/useSuggestions';
import { SuggestionCard } from './SuggestionCard';
import { useKeyboardShortcuts } from '@/hooks/useKeyboardShortcuts';
import { LoadingState } from '@/components/ui/LoadingState';
import { ErrorState } from '@/components/ui/ErrorState';
import { EmptyState } from '@/components/ui/EmptyState';
import { Inbox, Keyboard } from 'lucide-react';
import { toast } from 'sonner';

export default function ReviewQueue() {
  const { data: suggestions, isLoading, isError, refetch } = useSuggestions();
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

  const handleConfirm = useCallback(() => {
    if (!suggestions || suggestions.length === 0) return;
    const current = suggestions[selectedIndex];
    confirmMutation.mutate(
      { id: current.id, company: current.extractedCompany, role: current.extractedRole || undefined },
      {
        onSuccess: () => {
          toast.success(`Confirmed application for ${current.extractedCompany}`);
          // Adjust index if we are at the end
          setSelectedIndex((prev) => (prev >= maxIndex ? Math.max(0, maxIndex - 1) : prev));
        },
        onError: (err: any) => {
          toast.error(err.response?.data?.title || 'Failed to confirm suggestion');
        }
      }
    );
  }, [suggestions, selectedIndex, maxIndex, confirmMutation]);

  const handleReject = useCallback(() => {
    if (!suggestions || suggestions.length === 0) return;
    const current = suggestions[selectedIndex];
    rejectMutation.mutate(current.id, {
      onSuccess: () => {
        toast.success(`Rejected suggestion for ${current.extractedCompany}`);
        setSelectedIndex((prev) => (prev >= maxIndex ? Math.max(0, maxIndex - 1) : prev));
      },
      onError: () => {
        toast.error('Failed to reject suggestion');
      }
    });
  }, [suggestions, selectedIndex, maxIndex, rejectMutation]);

  // Handle direct confirms/rejects from the card (bypassing keyboard)
  const onCardConfirm = (id: number, company: string, role?: string) => {
    confirmMutation.mutate(
      { id, company, role },
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
  };

  const onCardReject = (id: number) => {
    rejectMutation.mutate(id, {
      onSuccess: () => {
        toast.success(`Rejected suggestion`);
        setSelectedIndex((prev) => (prev >= maxIndex ? Math.max(0, maxIndex - 1) : prev));
      }
    });
  };

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
