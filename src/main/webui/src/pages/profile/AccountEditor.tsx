import { useState } from 'react';
import { Card, CardHeader, CardTitle } from '../../components/Card';
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
    <Card className="mb-2">
      <CardHeader>
        <CardTitle>Account</CardTitle>
      </CardHeader>
      <div className="card-body">
        <label htmlFor="profileNewPassword" className="form-label">
          New Password
        </label>
        <input
          type="password"
          id="profileNewPassword"
          placeholder="New password"
          autoComplete="new-password"
          className="form-control"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
        />
        <label htmlFor="profileConfirmPassword" className="form-label mt-2">
          Confirm Password
        </label>
        <input
          type="password"
          id="profileConfirmPassword"
          placeholder="Confirm password"
          autoComplete="new-password"
          className="form-control"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />
        <button className="btn btn-outline-secondary btn-sm mt-2" onClick={submit}>
          Change Password
        </button>
        {message && (
          <div id="profilePasswordError" className={`alert alert-${message.kind} py-2 mt-2 mb-0`}>
            {message.text}
          </div>
        )}
      </div>
    </Card>
  );
}
