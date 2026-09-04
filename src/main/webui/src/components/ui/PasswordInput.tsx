import { useState, type ComponentProps } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { Input } from './Input';

/**
 * A password `<Input>` with a show/hide toggle in its `right` slot. Takes the
 * same props as `Input` minus `type` / `right` (which it controls).
 */
export function PasswordInput(props: Omit<ComponentProps<typeof Input>, 'type' | 'right'>) {
  const [show, setShow] = useState(false);
  return (
    <Input
      {...props}
      type={show ? 'text' : 'password'}
      right={
        <button
          type="button"
          tabIndex={-1}
          onClick={() => setShow((s) => !s)}
          aria-label={show ? 'Hide password' : 'Show password'}
          className="text-ink-muted hover:text-ink"
        >
          {show ? <EyeOff size={15} /> : <Eye size={15} />}
        </button>
      }
    />
  );
}
