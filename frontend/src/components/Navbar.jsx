import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { clearSession } from '@/services/api.js';

const LINKS = [
  { to: '/dashboard', label: 'Events' },
  { to: '/profile', label: 'My bookings' },
];

/** Calm bar: brand, page links (sand underline on the current one), first name and log out. */
export default function Navbar() {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  let user = {};
  try {
    user = JSON.parse(localStorage.getItem('user') || '{}') || {};
  } catch {
    user = {};
  }
  const firstName = (user.name || '').trim().split(/\s+/)[0];

  const handleLogout = () => {
    clearSession();
    navigate('/');
  };

  return (
    <nav aria-label="Main" className="sticky top-0 z-40 border-b border-line bg-void/90">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-6 px-5 sm:px-8">
        <Link to="/" className="py-3 font-display text-xl font-extrabold text-mist">
          EventSphere
        </Link>
        <ul className="order-last flex w-full gap-2 sm:order-none sm:w-auto">
          {LINKS.map(({ to, label }) => (
            <li key={to}>
              <Link
                to={to}
                aria-current={pathname === to ? 'page' : undefined}
                className="inline-flex h-12 items-center border-b-2 border-transparent px-2 text-ash transition-colors hover:text-mist aria-[current=page]:border-sand aria-[current=page]:font-semibold aria-[current=page]:text-mist"
              >
                {label}
              </Link>
            </li>
          ))}
        </ul>
        <div className="ml-auto flex items-center gap-3 py-2">
          {firstName && <span className="text-sm text-ash">{firstName}</span>}
          <Button variant="outline" size="sm" onClick={handleLogout} className="border-line bg-transparent text-mist hover:bg-raised">
            Log out
          </Button>
        </div>
      </div>
    </nav>
  );
}
