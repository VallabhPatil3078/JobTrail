import type { LucideIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface EmptyStateProps {
  icon: LucideIcon;
  title: string;
  description?: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}

export function EmptyState({ icon: Icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center p-8 space-y-4 text-center min-h-[300px] border-2 border-dashed border-border rounded-xl bg-card">
      <div className="p-4 rounded-full bg-muted">
        <Icon className="w-8 h-8 text-muted-foreground" />
      </div>
      <div className="space-y-1">
        <h3 className="font-semibold tracking-tight text-lg">{title}</h3>
        {description && <p className="text-sm text-muted-foreground max-w-sm mx-auto">{description}</p>}
      </div>
      {action && (
        <Button onClick={action.onClick} variant="secondary" className="mt-4">
          {action.label}
        </Button>
      )}
    </div>
  );
}
