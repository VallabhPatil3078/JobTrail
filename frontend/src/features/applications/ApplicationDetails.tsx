import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription } from '@/components/ui/sheet';
import { Button, buttonVariants } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { StatusChip } from './StatusChip';
import { useApplicationHistory, useUpdateApplicationStatus, useDeleteApplication, useUpdateApplication } from '@/hooks/useApplications';
import type { Application, ApplicationStatus } from '@/hooks/useApplications';
import { format } from 'date-fns';
import { Calendar, Building2, Briefcase, Mail, Trash2, Bell, Check } from 'lucide-react';
import { toast } from 'sonner';
import { useReminders, useCompleteReminder } from '@/hooks/useReminders';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { useMatchResult } from '@/hooks/useMatch';
import { RadialBarChart, RadialBar, ResponsiveContainer } from 'recharts';
import api from '@/api/axios';
import { useQuery } from '@tanstack/react-query';

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
  const [createReminder, setCreateReminder] = useState(false);
  
  const historyQuery = useApplicationHistory(application?.id ?? 0, !!application && open);
  const updateStatusMutation = useUpdateApplicationStatus();
  const deleteMutation = useDeleteApplication();
  const { data: reminders } = useReminders();
  const completeMutation = useCompleteReminder();
  
  const [isDeleting, setIsDeleting] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editCompany, setEditCompany] = useState('');
  const [editRole, setEditRole] = useState('');
  const [editJobUrl, setEditJobUrl] = useState('');
  const [editJobDescription, setEditJobDescription] = useState('');
  const updateMutation = useUpdateApplication();

  const { data: profile } = useQuery({
    queryKey: ['user-profile'],
    queryFn: async () => {
      const res = await api.get('/users/me');
      return res.data;
    },
  });

  const { data: matchResult, isLoading: isMatchLoading } = useMatchResult(
    application?.id ?? 0, 
    !!application && !!application.jobDescription && !!profile?.hasResumeText && !isEditing
  );

  if (!application) return null;

  const allowedNextStatuses = VALID_TRANSITIONS[application.status] || [];

  const handleStatusUpdate = () => {
    if (!newStatus) return;
    updateStatusMutation.mutate(
      { id: application.id, status: newStatus, createReminder },
      {
        onSuccess: () => {
          toast.success('Status updated');
          setNewStatus(null);
        },
        onError: (err: any) => {
          toast.error(JSON.stringify(err.response?.data) || 'Failed to update status');
        }
      }
    );
  };

  const handleDelete = () => {
    deleteMutation.mutate(application.id, {
      onSuccess: () => {
        toast.success('Application deleted');
        onOpenChange(false);
      }
    });
  };

  const handleEditToggle = () => {
    if (!isEditing) {
      setEditCompany(application.company || '');
      setEditRole(application.role || '');
      setEditJobUrl(application.jobUrl || '');
      setEditJobDescription(application.jobDescription || '');
      setIsEditing(true);
    } else {
      setIsEditing(false);
    }
  };

  const handleSaveEdit = () => {
    updateMutation.mutate(
      { 
        id: application.id, 
        data: { 
          company: editCompany, 
          role: editRole, 
          jobUrl: editJobUrl, 
          jobDescription: editJobDescription,
          dateApplied: application.dateApplied,
          source: application.source
        } 
      },
      {
        onSuccess: () => {
          toast.success('Application updated');
          setIsEditing(false);
        },
        onError: (err: any) => {
          toast.error(err.response?.data?.message || 'Failed to save application');
        }
      }
    );
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-full sm:max-w-2xl data-[side=right]:sm:max-w-2xl overflow-y-auto p-0 pb-6">
        <SheetHeader className="px-6 pt-6">
          <SheetTitle className="flex justify-between items-start">
            <div>
              {isEditing ? (
                <div className="space-y-2">
                  <Input value={editCompany} onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEditCompany(e.target.value)} placeholder="Company" className="text-xl font-bold h-8 w-48" />
                  <Input value={editRole} onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEditRole(e.target.value)} placeholder="Role" className="text-sm h-7 w-48" />
                </div>
              ) : (
                <>
                  <div className="text-2xl font-bold">{application.company}</div>
                  <div className="text-muted-foreground font-normal text-lg">{application.role}</div>
                </>
              )}
            </div>
            <div className="flex flex-col items-end gap-2">
              <StatusChip status={application.status} />
              <Button variant="outline" size="sm" onClick={isEditing ? handleSaveEdit : handleEditToggle}>
                {isEditing ? 'Save' : 'Edit'}
              </Button>
            </div>
          </SheetTitle>
          <SheetDescription>
            Applied on {format(new Date(application.dateApplied), 'PPP')}
          </SheetDescription>
        </SheetHeader>

        <div className="mt-8 space-y-8 px-6">
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
              <div className="space-y-1 col-span-2">
                <span className="text-xs text-muted-foreground uppercase flex items-center gap-1"><Building2 className="w-3 h-3"/> Job URL</span>
                {isEditing ? (
                  <Input value={editJobUrl} onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEditJobUrl(e.target.value)} placeholder="https://..." className="mt-1 text-sm h-8" />
                ) : (
                  <p className="text-sm font-medium">
                    {application.jobUrl ? (
                      <a href={application.jobUrl} target="_blank" rel="noreferrer" className="text-primary hover:underline">
                        {application.jobUrl}
                      </a>
                    ) : 'Not specified'}
                  </p>
                )}
              </div>
              <div className="space-y-1 col-span-2">
                <span className="text-xs text-muted-foreground uppercase flex items-center gap-1"><Briefcase className="w-3 h-3"/> Job Description</span>
                {isEditing ? (
                  <Textarea value={editJobDescription} onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setEditJobDescription(e.target.value)} placeholder="Paste job description here..." className="mt-1 min-h-[100px] text-sm" />
                ) : (
                  <p className="text-sm font-medium whitespace-pre-wrap max-h-32 overflow-y-auto bg-muted/30 p-2 rounded-md">
                    {application.jobDescription || 'Not specified'}
                  </p>
                )}
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

          {/* Keyword Overlap Section */}
          {!isEditing && (
            <div className="space-y-4 p-4 bg-muted/20 rounded-lg border border-border">
              <h3 className="font-semibold text-lg flex items-center justify-between">
                Keyword Overlap
              </h3>
              {!application.jobDescription?.trim() ? (
                <div className="text-sm text-muted-foreground">
                  <p className="mb-3">Add a job description to enable keyword matching.</p>
                  <Button variant="outline" size="sm" onClick={handleEditToggle}>
                    Add Job Description
                  </Button>
                </div>
              ) : !profile?.hasResumeText ? (
                <div className="text-sm text-muted-foreground">
                  <p className="mb-3">Add your resume in Settings to enable keyword matching.</p>
                  <Link to="/settings" className={buttonVariants({ variant: "outline", size: "sm" })}>
                    Go to Settings
                  </Link>
                </div>
              ) : isMatchLoading ? (
                <div className="text-sm text-muted-foreground">Calculating match...</div>
              ) : matchResult ? (
                matchResult.noKeywordsFound ? (
                  <div className="text-sm text-muted-foreground">
                    No relevant technical keywords found to match against.
                  </div>
                ) : (
                  <div className="flex flex-col md:flex-row gap-6 items-center">
                    <div className="w-32 h-32 relative flex-shrink-0">
                      <ResponsiveContainer width="100%" height="100%">
                        <RadialBarChart 
                          cx="50%" 
                          cy="50%" 
                          innerRadius="70%" 
                          outerRadius="100%" 
                          barSize={10} 
                          data={[{ name: 'match', value: matchResult.matchPercentage, fill: 'var(--primary)' }]}
                          startAngle={90}
                          endAngle={-270}
                        >
                          <RadialBar background dataKey="value" cornerRadius={10} />
                        </RadialBarChart>
                      </ResponsiveContainer>
                      <div className="absolute inset-0 flex items-center justify-center flex-col">
                        <span className="text-2xl font-bold">{matchResult.matchPercentage}%</span>
                      </div>
                    </div>
                    <div className="flex-1 space-y-4">
                      {matchResult.matchedKeywords.length > 0 && (
                        <div>
                          <p className="text-sm font-medium text-green-600 dark:text-green-500 mb-1">Matched Keywords</p>
                          <div className="flex flex-wrap gap-1">
                            {matchResult.matchedKeywords.map(kw => (
                              <span key={kw} className="bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 text-xs px-2 py-1 rounded-md">{kw}</span>
                            ))}
                          </div>
                        </div>
                      )}
                      {matchResult.missingKeywords.length > 0 && (
                        <div>
                          <p className="text-sm font-medium text-red-600 dark:text-red-500 mb-1">Missing Keywords</p>
                          <div className="flex flex-wrap gap-1">
                            {matchResult.missingKeywords.map(kw => (
                              <span key={kw} className="bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200 text-xs px-2 py-1 rounded-md">{kw}</span>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                )
              ) : (
                <div className="text-sm text-destructive">Failed to calculate match.</div>
              )}
            </div>
          )}

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
              <div className="flex items-center gap-2 mt-2">
                <input 
                  type="checkbox" 
                  id="status-reminder" 
                  checked={createReminder} 
                  onChange={(e) => setCreateReminder(e.target.checked)} 
                  className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary"
                />
                <Label htmlFor="status-reminder" className="text-sm font-medium cursor-pointer">
                  Create follow-up reminder
                </Label>
              </div>
            </div>
          )}

          {/* Reminders Section */}
          {reminders?.filter(r => r.applicationId === application.id).length ? (
            <div className="space-y-4">
              <h3 className="font-semibold text-lg flex items-center gap-2">
                <Bell className="w-5 h-5 text-amber-500" />
                Active Reminders
              </h3>
              <div className="space-y-2">
                {reminders.filter(r => r.applicationId === application.id).map(reminder => (
                  <div key={reminder.id} className="flex justify-between items-center p-3 border rounded-md bg-amber-50/50 dark:bg-amber-950/20">
                    <div>
                      <div className="font-medium text-sm flex items-center gap-2">
                        {reminder.type === 'INTERVIEW_PREP' ? 'Interview Prep' : 'Follow Up'}
                        {new Date(reminder.dueDate) < new Date() && (
                          <span className="text-xs bg-red-100 text-red-600 px-1.5 rounded-full uppercase tracking-wider font-bold">Overdue</span>
                        )}
                      </div>
                      <div className="text-xs text-muted-foreground mt-0.5">
                        Due: {new Date(reminder.dueDate).toLocaleDateString()}
                      </div>
                    </div>
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => completeMutation.mutate(reminder.id)}
                      disabled={completeMutation.isPending}
                    >
                      <Check className="w-4 h-4 mr-1" />
                      Complete
                    </Button>
                  </div>
                ))}
              </div>
            </div>
          ) : null}

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

          <div className="pt-8 border-t border-border flex gap-4">
            {isDeleting ? (
              <>
                <Button variant="outline" className="flex-1" onClick={() => setIsDeleting(false)} disabled={deleteMutation.isPending}>
                  Cancel
                </Button>
                <Button variant="destructive" className="flex-1" onClick={handleDelete} disabled={deleteMutation.isPending}>
                  Confirm Delete
                </Button>
              </>
            ) : (
              <Button variant="destructive" className="w-full" onClick={() => setIsDeleting(true)}>
                <Trash2 className="w-4 h-4 mr-2" />
                Delete Application
              </Button>
            )}
          </div>
        </div>
      </SheetContent>
    </Sheet>
  );
}
