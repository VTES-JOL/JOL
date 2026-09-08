import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ChatCompose } from './ChatCompose';

describe('ChatCompose', () => {
  it('sends trimmed text and clears the field', async () => {
    const onSend = vi.fn();
    const user = userEvent.setup();
    render(<ChatCompose onSend={onSend} />);

    const input = screen.getByLabelText('Chat');
    await user.type(input, '  don’t bleed me  ');
    await user.click(screen.getByRole('button', { name: 'Send' }));

    expect(onSend).toHaveBeenCalledWith('don’t bleed me');
    expect(input).toHaveValue('');
  });

  it('does not send blank / whitespace-only text', async () => {
    const onSend = vi.fn();
    const user = userEvent.setup();
    render(<ChatCompose onSend={onSend} />);

    await user.type(screen.getByLabelText('Chat'), '   ');
    await user.click(screen.getByRole('button', { name: 'Send' }));

    expect(onSend).not.toHaveBeenCalled();
  });

  it('disables input and buttons when disabled', () => {
    render(<ChatCompose onSend={vi.fn()} disabled />);

    expect(screen.getByLabelText('Chat')).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Send' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Quick chat shortcuts' })).toBeDisabled();
  });
});
