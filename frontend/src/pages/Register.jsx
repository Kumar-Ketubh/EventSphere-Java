import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '@/components/AuthLayout.jsx';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { registerUser, saveSession } from '@/services/api.js';

const FIELDS = [
  { name: 'name', label: 'Full name', type: 'text', placeholder: 'Alex Johnson', autoComplete: 'name' },
  { name: 'rollNumber', label: 'Roll number or faculty ID', type: 'text', placeholder: 'CS2024001' },
  { name: 'email', label: 'College email', type: 'email', placeholder: 'alex@college.edu', autoComplete: 'email' },
  { name: 'password', label: 'Password', type: 'password', autoComplete: 'new-password' },
];

/** Sign-up page: creates the account and logs straight in. */
export default function Register() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ name: '', email: '', password: '', rollNumber: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const response = await registerUser(formData);
      saveSession(response.data);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'We could not create your account. Check the details and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Create account"
      intro="For students and faculty. It takes a minute."
      error={error}
      footer={
        <p>
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-sand underline underline-offset-4">
            Log in
          </Link>
        </p>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-5">
        {FIELDS.map((f) => (
          <div key={f.name} className="space-y-2">
            <Label htmlFor={`reg-${f.name}`}>{f.label}</Label>
            <Input
              id={`reg-${f.name}`}
              name={f.name}
              type={f.type}
              className="h-11 bg-surface text-mist"
              value={formData[f.name]}
              onChange={handleChange}
              placeholder={f.placeholder}
              autoComplete={f.autoComplete}
              required
            />
          </div>
        ))}
        <Button type="submit" variant="accent" size="xl" className="w-full" disabled={loading}>
          {loading ? 'Creating account...' : 'Create account'}
        </Button>
      </form>
    </AuthLayout>
  );
}
