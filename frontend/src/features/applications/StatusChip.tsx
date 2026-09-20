import { Badge } from '@/components/ui/badge';
import type { ApplicationStatus } from '@/hooks/useApplications';

interface StatusChipProps {
  status: ApplicationStatus;
  className?: string;
}

export function StatusChip({ status, className }: StatusChipProps) {
  const getStatusColor = (s: ApplicationStatus) => {
    switch (s) {
      case 'APPLIED':
        return 'bg-[var(--status-applied)] hover:bg-[var(--status-applied)]/90';
      case 'OA':
        return 'bg-[var(--status-oa)] hover:bg-[var(--status-oa)]/90';
      case 'INTERVIEW':
        return 'bg-[var(--status-interview)] hover:bg-[var(--status-interview)]/90';
      case 'OFFER':
        return 'bg-[var(--status-offer)] hover:bg-[var(--status-offer)]/90';
      case 'REJECTED':
        return 'bg-[var(--status-rejected)] hover:bg-[var(--status-rejected)]/90';
      case 'WITHDRAWN':
        return 'bg-[var(--status-withdrawn)] hover:bg-[var(--status-withdrawn)]/90';
      default:
        return 'bg-secondary';
    }
  };

  return (
    <Badge className={`${getStatusColor(status)} text-white font-medium shadow-none ${className}`}>
      {status}
    </Badge>
  );
}
