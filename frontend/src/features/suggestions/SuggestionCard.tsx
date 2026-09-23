import { useState, useRef, useEffect, useCallback } from 'react';
import { Check, X, Pencil, Mail, Building2, Briefcase, Calendar } from 'lucide-react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import type { SuggestedApplication } from '@/hooks/useSuggestions';

interface SuggestionCardProps {
  suggestion: SuggestedApplication;
  isSelected: boolean;
  onConfirm: (id: number, company: string, role?: string, createReminder?: boolean) => void;
  onReject: (id: number) => void;
  isConfirming: boolean;
  isRejecting: boolean;
  onSelect: () => void;
}

export function SuggestionCard({
  suggestion,
  isSelected,
  onConfirm,
  onReject,
  isConfirming,
  isRejecting,
  onSelect,
}: SuggestionCardProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [company, setCompany] = useState(suggestion.extractedCompany);
  const [role, setRole] = useState(suggestion.extractedRole || '');
  const [createReminder, setCreateReminder] = useState(false);
  const cardRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isSelected && cardRef.current) {
      cardRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }, [isSelected]);

  // Expose editing state to parent or handle editing entirely here
  // Actually we need to handle "E" shortcut to toggle edit from parent.
  // We'll manage local edit state. If isSelected is true and 'e' is pressed, parent needs to tell card to edit.
  // Instead of complex imperative handles, we can just pass `isEditing` from parent, but keeping it local is simpler.
  // Let's let the parent pass an `isEditing` prop, or we can listen to window keydown if `isSelected`.
  
  const handleConfirm = useCallback(() => {
    onConfirm(suggestion.id, company, role, createReminder);
    setIsEditing(false);
  }, [onConfirm, suggestion.id, company, role, createReminder]);

  useEffect(() => {
    const handleKey = (e: KeyboardEvent) => {
      if (!isSelected) return;
      const target = e.target as HTMLElement;
      if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA') {
        if (e.key === 'Escape') {
          setIsEditing(false);
        }
        if (e.key === 'Enter') {
          handleConfirm();
        }
        return;
      }
      
      if (e.key === 'e' || e.key === 'E') {
        e.preventDefault();
        setIsEditing(true);
      }
    };
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, [isSelected, company, role, handleConfirm]);

  const confidenceLevel = suggestion.confidenceScore >= 80 ? 'High' : suggestion.confidenceScore >= 50 ? 'Medium' : 'Low';
  const confidenceColor = suggestion.confidenceScore >= 80 ? 'text-green-500' : suggestion.confidenceScore >= 50 ? 'text-amber-500' : 'text-red-500';

  return (
    <Card 
      ref={cardRef}
      className={`transition-all duration-200 cursor-pointer ${
        isSelected ? 'ring-2 ring-primary shadow-md scale-[1.01]' : 'hover:border-primary/50'
      }`}
      onClick={onSelect}
    >
      <div className="flex flex-col md:flex-row h-full">
        {/* Email Context Section */}
        <div className="w-full md:w-1/2 min-w-0 p-4 md:p-6 border-b md:border-b-0 md:border-r border-border bg-muted/20">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2 text-muted-foreground">
              <Mail className="w-4 h-4" />
              <span className="text-sm font-medium">Source Email</span>
            </div>
            <a 
              href={`https://mail.google.com/mail/u/0/#search/${encodeURIComponent(suggestion.rawEmail.subject)}`}
              target="_blank"
              rel="noopener noreferrer"
              className="text-xs text-primary hover:underline flex items-center gap-1"
              onClick={e => e.stopPropagation()}
            >
              Open in Gmail
            </a>
          </div>
          <div className="space-y-2">
            <div>
              <p className="text-xs text-muted-foreground uppercase font-semibold">Subject</p>
              <p className="text-sm font-medium truncate">{suggestion.rawEmail.subject}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground uppercase font-semibold">Sender</p>
              <p className="text-sm truncate max-w-full">{suggestion.rawEmail.sender}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground uppercase font-semibold">Snippet</p>
              <p className="text-sm text-muted-foreground line-clamp-3 italic">
                "{suggestion.rawEmail.snippet}"
              </p>
            </div>
          </div>
        </div>

        {/* Extraction Section */}
        <div className="w-full md:w-1/2 min-w-0 p-4 md:p-6 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <span className="text-sm font-medium">Extracted Data</span>
              <span className={`text-xs font-bold px-2 py-1 rounded-full bg-muted ${confidenceColor}`}>
                {confidenceLevel} Confidence
              </span>
            </div>
            
            {isEditing ? (
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label>Company</Label>
                  <Input 
                    autoFocus
                    value={company} 
                    onChange={e => setCompany(e.target.value)} 
                  />
                </div>
                <div className="space-y-2">
                  <Label>Role</Label>
                  <Input 
                    value={role} 
                    onChange={e => setRole(e.target.value)} 
                  />
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-primary/10 rounded-md">
                    <Building2 className="w-5 h-5 text-primary" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-xs text-muted-foreground uppercase font-semibold">Company</p>
                    <p className="font-medium text-lg truncate">{company}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-primary/10 rounded-md">
                    <Briefcase className="w-5 h-5 text-primary" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-xs text-muted-foreground uppercase font-semibold">Role</p>
                    <p className="font-medium truncate">{role || 'Not specified'}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-primary/10 rounded-md">
                    <Calendar className="w-5 h-5 text-primary" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground uppercase font-semibold">Date</p>
                    <p className="font-medium">{new Date(suggestion.extractedDate).toLocaleDateString()}</p>
                  </div>
                </div>
              </div>
            )}
          </div>
          
          <div className="flex items-center gap-2 mt-4 pb-2">
            <input 
              type="checkbox" 
              id={`reminder-${suggestion.id}`} 
              checked={createReminder} 
              onChange={(e) => setCreateReminder(e.target.checked)} 
              className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary"
              onClick={e => e.stopPropagation()}
            />
            <Label htmlFor={`reminder-${suggestion.id}`} className="text-sm cursor-pointer" onClick={e => e.stopPropagation()}>
              Create follow-up reminder
            </Label>
          </div>

          <div className="flex flex-col sm:flex-row flex-wrap sm:items-center justify-end gap-2 mt-4">
            {isEditing ? (
              <>
                <Button variant="ghost" size="sm" onClick={(e: React.MouseEvent) => { e.stopPropagation(); setIsEditing(false); }}>
                  Cancel
                </Button>
                <Button size="sm" onClick={(e: React.MouseEvent) => { e.stopPropagation(); handleConfirm(); }} disabled={isConfirming}>
                  Save & Confirm
                </Button>
              </>
            ) : (
              <>
                <Button 
                  variant="outline" 
                  size="sm"
                  onClick={(e: React.MouseEvent) => { e.stopPropagation(); setIsEditing(true); }}
                >
                  <Pencil className="w-4 h-4 mr-1" />
                  Edit (E)
                </Button>
                <Button 
                  variant="destructive" 
                  size="sm"
                  onClick={(e: React.MouseEvent) => { e.stopPropagation(); onReject(suggestion.id); }}
                  disabled={isRejecting}
                >
                  <X className="w-4 h-4 mr-1" />
                  Reject (R)
                </Button>
                <Button 
                  variant="default" 
                  size="sm"
                  onClick={(e: React.MouseEvent) => { e.stopPropagation(); handleConfirm(); }}
                  disabled={isConfirming}
                >
                  <Check className="w-4 h-4 mr-1 md:mr-2" />
                  Confirm (C)
                </Button>
              </>
            )}
          </div>
        </div>
      </div>
    </Card>
  );
}
