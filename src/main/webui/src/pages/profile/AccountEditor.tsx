import { useState } from 'react';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { FieldHint } from '../../components/ui/FormFeedback';
import { api } from '../../api/client';
import { useSave } from './saveState';
import { SaveNote } from './SaveNote';

const MIN_LENGTH = 8;

export function AccountEditor() {
  const [current, setCurrent] = useState('');
  const [next, setNext] = useState('');
  const [confirm, setConfirm] = useState('');
  const { state, error, run, reset } = useSave();

  // Mirror the server's rules so the button only fires a request that can pass.
  const tooShort = next.length > 0 && next.length < MIN_LENGTH;
  const mismatch = confirm.length > 0 && next !== confirm;
  const sameAsCurrent = next.length > 0 && next === current;
  const canSubmit =
    current.length > 0 && next.length >= MIN_LENGTH && next === confirm && !sameAsCurrent && state !== 'saving';

  const submit = () => {
    void run(
      api
        .put('/profile/password', { currentPassword: current, newPassword: next })
        .then(() => {
          setCurrent('');
          setNext('');
          setConfirm('');
        }),
      { fallbackError: 'Failed to change password.' },
    );
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Account</CardTitle>
      </CardHeader>
      <CardBody className="flex flex-col gap-3">
        <Input
          type="password"
          id="profileCurrentPassword"
          label="Current Password"
          placeholder="Current password"
          autoComplete="current-password"
          value={current}
          onChange={(e) => {
            setCurrent(e.target.value);
            if (state === 'error') reset();
          }}
        />
        <div>
          <Input
            type="password"
            id="profileNewPassword"
            label="New Password"
            placeholder="New password"
            autoComplete="new-password"
            value={next}
            error={tooShort ? `At least ${MIN_LENGTH} characters.` : sameAsCurrent ? 'Must differ from your current password.' : undefined}
            onChange={(e) => setNext(e.target.value)}
          />
          {!tooShort && !sameAsCurrent && (
            <FieldHint>Use at least {MIN_LENGTH} characters. There is no e-mail reset yet, so keep it somewhere safe.</FieldHint>
          )}
        </div>
        <Input
          type="password"
          id="profileConfirmPassword"
          label="Confirm Password"
          placeholder="Confirm password"
          autoComplete="new-password"
          value={confirm}
          error={mismatch ? 'Passwords do not match.' : undefined}
          onChange={(e) => setConfirm(e.target.value)}
        />
        <div className="flex flex-col gap-2 items-start">
          <Button variant="primary" size="sm" disabled={!canSubmit} onClick={submit}>
            Change Password
          </Button>
          <SaveNote state={state} error={error} savedText="Password updated." />
        </div>
      </CardBody>
    </Card>
  );
}
