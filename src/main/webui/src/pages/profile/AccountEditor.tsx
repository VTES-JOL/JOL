import { useState } from 'react';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { InlineAlert } from '../../components/ui/FormFeedback';
import { api } from '../../api/client';
import { runRequest } from '../../api/mutate';

export function AccountEditor() {
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState<{ text: string; kind: 'success' | 'danger' } | null>(null);

  const submit = () => {
    if (!newPassword && !confirmPassword) {
      setMessage({ text: 'Enter a new password.', kind: 'danger' });
      return;
    }
    if (newPassword !== confirmPassword) {
      setMessage({ text: 'Password confirmation does not match.', kind: 'danger' });
      return;
    }
    runRequest(api.put('/profile/password', { newPassword }), 'Failed to change password', () => {
      setMessage({ text: 'Password updated.', kind: 'success' });
      setNewPassword('');
      setConfirmPassword('');
    });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Account</CardTitle>
      </CardHeader>
      <CardBody className="jt:flex jt:flex-col jt:gap-3">
        <Input
          type="password"
          id="profileNewPassword"
          label="New Password"
          placeholder="New password"
          autoComplete="new-password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
        />
        <Input
          type="password"
          id="profileConfirmPassword"
          label="Confirm Password"
          placeholder="Confirm password"
          autoComplete="new-password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />
        <div className="jt:flex jt:flex-col jt:gap-2 jt:items-start">
          <Button variant="secondary" size="sm" onClick={submit}>
            Change Password
          </Button>
          {message && <InlineAlert kind={message.kind}>{message.text}</InlineAlert>}
        </div>
      </CardBody>
    </Card>
  );
}
