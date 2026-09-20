import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useCreateApplication } from '@/hooks/useApplications';
import { toast } from 'sonner';

interface ApplicationFormProps {
  onSuccess?: () => void;
}

export function ApplicationForm({ onSuccess }: ApplicationFormProps) {
  const [company, setCompany] = useState('');
  const [role, setRole] = useState('');
  const [dateApplied, setDateApplied] = useState(() => new Date().toISOString().split('T')[0]);
  
  const createMutation = useCreateApplication();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate(
      { company, role, dateApplied },
      {
        onSuccess: () => {
          toast.success('Application added manually');
          if (onSuccess) onSuccess();
        },
        onError: (err: any) => {
          toast.error(err.response?.data?.message || 'Failed to add application');
        }
      }
    );
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 pt-4">
      <div className="space-y-2">
        <Label htmlFor="company">Company</Label>
        <Input 
          id="company" 
          required 
          value={company} 
          onChange={(e) => setCompany(e.target.value)} 
          placeholder="e.g. Acme Corp" 
        />
      </div>
      <div className="space-y-2">
        <Label htmlFor="role">Role</Label>
        <Input 
          id="role" 
          required 
          value={role} 
          onChange={(e) => setRole(e.target.value)} 
          placeholder="e.g. Software Engineer" 
        />
      </div>
      <div className="space-y-2">
        <Label htmlFor="dateApplied">Date Applied</Label>
        <Input 
          id="dateApplied" 
          type="date"
          required 
          value={dateApplied} 
          onChange={(e) => setDateApplied(e.target.value)} 
        />
      </div>
      <Button type="submit" className="w-full" disabled={createMutation.isPending}>
        {createMutation.isPending ? 'Adding...' : 'Add Application'}
      </Button>
    </form>
  );
}
