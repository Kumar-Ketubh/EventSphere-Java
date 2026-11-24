import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '@/components/AuthLayout.jsx';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { loginUser, saveSession } from '@/services/api.js';

/** Login page: email and password, then on to the dashboard. */
export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('student@college.edu');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const response = await loginUser(email, password);
      saveSession(response.data);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'We could not log you in. Check your email and password, then try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Log in"
      intro="Use your college email to see what is on."
      error={error}
      footer={
        <>
          <p className="rounded-lg border border-line bg-surface px-4 py-3 text-mist">
            Demo account: <code className="tabular-nums whitespace-nowrap">student@college.edu</code> /{' '}
            <code className="tabular-nums whitespace-nowrap">password123</code>
          </p>
          <p>
            New here?{' '}
            <Link to="/register" className="font-semibold text-sand underline underline-offset-4">
              Create account
            </Link>
          </p>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="space-y-2">
          <Label htmlFor="login-email">Email</Label>
          <Input
            id="login-email"
            type="email"
            className="h-11 bg-surface text-mist"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
            required
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="login-password">Password</Label>
          <Input
            id="login-password"
            type="password"
            className="h-11 bg-surface text-mist"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </div>
        <Button type="submit" variant="accent" size="xl" className="w-full" disabled={loading}>
          {loading ? 'Logging in...' : 'Log in'}
        </Button>
      </form>
    </AuthLayout>
  );
}
