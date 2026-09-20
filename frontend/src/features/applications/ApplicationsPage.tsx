import { useState } from 'react';
import { useApplications } from '@/hooks/useApplications';
import type { Application } from '@/hooks/useApplications';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { StatusChip } from './StatusChip';
import { ApplicationDetails } from './ApplicationDetails';
import { ApplicationForm } from './ApplicationForm';
import { LoadingState } from '@/components/ui/LoadingState';
import { ErrorState } from '@/components/ui/ErrorState';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Plus } from 'lucide-react';

export default function ApplicationsPage() {
  const { data: applications, isLoading, isError, refetch } = useApplications();
  const [selectedApp, setSelectedApp] = useState<Application | null>(null);
  const [isAddOpen, setIsAddOpen] = useState(false);

  if (isLoading) return <LoadingState message="Loading applications..." />;
  if (isError) return <ErrorState onRetry={() => refetch()} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold tracking-tight">Applications</h1>
        <Dialog open={isAddOpen} onOpenChange={setIsAddOpen}>
          <DialogTrigger className="inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0 bg-primary text-primary-foreground shadow hover:bg-primary/90 h-9 px-4 py-2">
            <Plus className="w-4 h-4 mr-2" />
            Add Application
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Add Manual Application</DialogTitle>
            </DialogHeader>
            <ApplicationForm onSuccess={() => setIsAddOpen(false)} />
          </DialogContent>
        </Dialog>
      </div>

      <div className="bg-card border border-border rounded-lg shadow-sm overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Company</TableHead>
              <TableHead>Role</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Date Applied</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {applications?.map((app) => (
              <TableRow 
                key={app.id} 
                className="cursor-pointer hover:bg-muted/50"
                onClick={() => setSelectedApp(app)}
              >
                <TableCell className="font-medium">{app.company}</TableCell>
                <TableCell>{app.role}</TableCell>
                <TableCell>
                  <StatusChip status={app.status} />
                </TableCell>
                <TableCell className="text-muted-foreground">
                  {new Date(app.dateApplied).toLocaleDateString()}
                </TableCell>
              </TableRow>
            ))}
            {applications?.length === 0 && (
              <TableRow>
                <TableCell colSpan={4} className="h-24 text-center text-muted-foreground">
                  No applications found.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      <ApplicationDetails 
        application={selectedApp} 
        open={!!selectedApp} 
        onOpenChange={(open) => !open && setSelectedApp(null)} 
      />
    </div>
  );
}
