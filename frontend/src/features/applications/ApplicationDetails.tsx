import { useState } from 'react';
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription } from '@/components/ui/sheet';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { StatusChip } from './StatusChip';
import { useApplicationHistory, useUpdateApplicationStatus, useDeleteApplication } from '@/hooks/useApplications';
import type { Application, ApplicationStatus } from '@/hooks/useApplications';
import { format } from 'date-fns';
import { Calendar, Building2, Briefcase, Mail, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

interface ApplicationDetailsProps {
  application: Application | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const VALID_TRANSITIONS: Record<ApplicationStatus, ApplicationStatus[]> = {
  APPLIED: ['OA', 'INTERVIEW', 'REJECTED', 'WITHDRAWN'],
  OA: ['INTERVIEW', 'REJECTED', 'WITHDRAWN'],
  INTERVIEW: ['OFFER', 'REJECTED', 'WITHDRAWN'],
  OFFER: ['WITHDRAWN'],
  REJECTED: [],
  WITHDRAWN: [],
};

export function ApplicationDetails({ application, open, onOpenChange }: ApplicationDetailsProps) {
  const [newStatus, setNewStatus] = useState<ApplicationStatus | null>(null);
  
  const historyQuery = useApplicationHistory(application?.id ?? 0, !!application && open);
  const updateStatusMutation = useUpdateApplicationStatus();
  const deleteMutation = useDeleteApplication();

  if (!application) return null;

  const allowedNextStatuses = VALID_TRANSITIONS[application.status] || [];

  const handleStatusUpdate = () => {
    if (!newStatus) return;
    updateStatusMutation.mutate(
      { id: application.id, status: newStatus },
      {
        onSuccess: () => {
          toast.success('Status updated');
          setNewStatus(null);
        },
        onError: () => {
          toast.error('Failed to update status');
        }
      }
    );
  };

  const handleDelete = () => {
    if (confirm('Are you sure you want to delete this application?')) {
      deleteMutation.mutate(application.id, {
        onSuccess: () => {
          toast.success('Application deleted');
          onOpenChange(false);
        }
      });
    }
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-full sm:max-w-md overflow-y-auto">
        <SheetHeader>
          <SheetTitle className="flex justify-between items-start">
            <div>
              <div className="text-2xl font-bold">{application.company}</div>
              <div className="text-muted-foreground font-normal text-lg">{application.role}</div>
            </div>
            <StatusChip status={application.status} />
          </SheetTitle>
          <SheetDescription>
            Applied on {format(new Date(application.dateApplied), 'PPP')}
          </SheetDescription>
        </SheetHeader>

        <div className="mt-8 space-y-8">
          {/* Metadata Section */}
          <div className="grid gap-4">
            <h3 className="font-semibold text-lg">Details</h3>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <span className="text-xs text-muted-foreground uppercase flex items-center gap-1"><Building2 className="w-3 h-3"/> Company</span>
                <p className="text-sm font-medium">{application.company}</p>
              </div>
              <div className="space-y-1">
                <span className="text-xs text-muted-foreground uppercase flex items-center gap-1"><Briefcase className="w-3 h-3"/> Role</span>
                <p className="text-sm font-medium">{application.role}</p>
              </div>
              <div className="space-y-1">
                <span className="text-xs text-muted-foreground uppercase flex items-center gap-1"><Calendar className="w-3 h-3"/> Applied</span>
                <p className="text-sm font-medium">{format(new Date(application.dateApplied), 'MMM d, yyyy')}</p>
              </div>
              <div className="space-y-1">
                <span className="text-xs text-muted-foreground uppercase flex items-center gap-1"><Mail className="w-3 h-3"/> Source</span>
                <p className="text-sm font-medium">{application.source === 'EMAIL_DETECTED' ? 'Email Detection' : 'Manual Entry'}</p>
              </div>
            </div>
          </div>

          {/* Status Update Section */}
          {allowedNextStatuses.length > 0 && (
            <div className="space-y-4 p-4 bg-muted/50 rounded-lg border border-border">
              <h3 className="font-semibold text-sm">Update Status</h3>
              <div className="flex gap-2">
                <Select value={newStatus || ''} onValueChange={(v) => setNewStatus(v as ApplicationStatus)}>
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="Select new status..." />
                  </SelectTrigger>
                  <SelectContent>
                    {allowedNextStatuses.map(status => (
                      <SelectItem key={status} value={status}>
                        {status}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Button 
                  onClick={handleStatusUpdate} 
                  disabled={!newStatus || updateStatusMutation.isPending}
                >
                  Update
                </Button>
              </div>
            </div>
          )}

          {/* Timeline Section */}
          <div className="space-y-4">
            <h3 className="font-semibold text-lg">Timeline</h3>
            {historyQuery.isLoading ? (
              <div className="text-sm text-muted-foreground">Loading history...</div>
            ) : historyQuery.isError ? (
              <div className="text-sm text-destructive">Failed to load timeline</div>
            ) : (
              <div className="relative border-l border-border ml-3 space-y-6">
                {historyQuery.data?.map((item, index) => (
                  <div key={item.id} className="relative pl-6">
                    <div className="absolute w-3 h-3 bg-primary/20 rounded-full -left-[6.5px] top-1.5 border border-primary/50" />
                    <div className="flex justify-between items-start mb-1">
                      <div className="font-medium text-sm flex items-center gap-2">
                        {index > 0 ? (
                          <>
                            <span className="text-muted-foreground">{item.fromStatus}</span>
                            <span>&rarr;</span>
                            <StatusChip status={item.toStatus} />
                          </>
                        ) : (
                          <StatusChip status={item.toStatus} />
                        )}
                      </div>
                      <span className="text-xs text-muted-foreground">
                        {format(new Date(item.changedAt), 'MMM d')}
                      </span>
                    </div>
                    {item.note && (
                      <p className="text-sm text-muted-foreground mt-1 bg-muted p-2 rounded-md">
                        {item.note}
                      </p>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="pt-8 border-t border-border">
            <Button variant="destructive" className="w-full" onClick={handleDelete} disabled={deleteMutation.isPending}>
              <Trash2 className="w-4 h-4 mr-2" />
              Delete Application
            </Button>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  );
}
