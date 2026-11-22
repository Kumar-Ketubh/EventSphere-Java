import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { MotionConfig } from 'motion/react';
import Landing from '@/pages/Landing.jsx';
import Login from '@/pages/Login.jsx';
import Register from '@/pages/Register.jsx';
import Dashboard from '@/pages/Dashboard.jsx';
import Profile from '@/pages/Profile.jsx';
import Ticket from '@/pages/Ticket.jsx';
import PageGrid from '@/components/PageGrid.jsx';

/** Sends anyone without a login token to the login page. */
function ProtectedRoute({ children }) {
  return localStorage.getItem('token') ? children : <Navigate to="/login" replace />;
}

/** Routes: landing and auth pages are public; dashboard and bookings need a login. */
export default function App() {
  return (
    <MotionConfig reducedMotion="user">
      <PageGrid />
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
          <Route path="/tickets/:id" element={<ProtectedRoute><Ticket /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </MotionConfig>
  );
}
