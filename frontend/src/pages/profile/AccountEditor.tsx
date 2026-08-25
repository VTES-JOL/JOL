import { useState } from 'react';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import { runRequest } from '../../api/mutate';

export function AccountEditor() {
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');

  const submit = () => {
    if (!newPassword && !confirmPassword) {
      setMessage('Enter a new password.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setMessage('Password confirmation does not match.');
      return;
    }
    runRequest(api.put('/profile/password', { newPassword }), 'Failed to change password', () => {
      setMessage('Password updated.');
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
        <div id="profilePasswordError" className="mt-2">
          {message}
        </div>
      </div>
    </Card>
  );
}
