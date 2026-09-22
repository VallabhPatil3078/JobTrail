import { useReminders, useCompleteReminder } from '@/hooks/useReminders';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Check, Calendar, Bell, Building2, Briefcase } from 'lucide-react';

export function RemindersPanel() {
  const { data: reminders, isLoading } = useReminders();
  const completeMutation = useCompleteReminder();

  if (isLoading || !reminders || reminders.length === 0) return null;

  return (
    <Card className="absolute top-4 right-4 w-80 shadow-lg z-50 overflow-hidden max-h-[80vh] flex flex-col border-amber-200 dark:border-amber-900/50">
      <CardHeader className="p-4 border-b bg-amber-50/50 dark:bg-amber-950/20 pb-4">
        <div className="flex items-center gap-2">
          <Bell className="w-5 h-5 text-amber-500" />
          <CardTitle className="text-base font-semibold">Active Reminders</CardTitle>
          <span className="bg-primary text-primary-foreground text-xs font-bold px-2 py-0.5 rounded-full ml-auto">
            {reminders.length}
          </span>
        </div>
      </CardHeader>
      <CardContent className="p-0 overflow-y-auto">
        <div className="divide-y divide-border">
          {reminders.map((reminder) => {
            const isOverdue = new Date(reminder.dueDate) < new Date();
            return (
              <div key={reminder.id} className="p-4 flex flex-col gap-2 hover:bg-muted/50 transition-colors">
                <div className="flex justify-between items-start gap-2">
                  <div>
                    <div className="flex items-center gap-1 font-medium">
                      <Building2 className="w-3.5 h-3.5 text-muted-foreground" />
                      <span className="text-sm">{reminder.company}</span>
                    </div>
                    {reminder.role && (
                      <div className="flex items-center gap-1 text-xs text-muted-foreground mt-0.5">
                        <Briefcase className="w-3.5 h-3.5" />
                        <span>{reminder.role}</span>
                      </div>
                    )}
                  </div>
                  <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded uppercase tracking-wider ${
                    reminder.type === 'INTERVIEW_PREP' ? 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400' : 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
                  }`}>
                    {reminder.type === 'INTERVIEW_PREP' ? 'Prep' : 'Follow up'}
                  </span>
                </div>
                
                <div className="flex items-center justify-between mt-2">
                  <div className={`flex items-center gap-1 text-xs ${isOverdue ? 'text-red-500 font-medium' : 'text-muted-foreground'}`}>
                    <Calendar className="w-3 h-3" />
                    {isOverdue ? 'Overdue' : new Date(reminder.dueDate).toLocaleDateString()}
                  </div>
                  
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="h-7 px-2 text-xs"
                    onClick={() => completeMutation.mutate(reminder.id)}
                    disabled={completeMutation.isPending}
                  >
                    <Check className="w-3 h-3 mr-1" />
                    Complete
                  </Button>
                </div>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}
