import { NavLink, Outlet } from 'react-router-dom';

export default function Layout() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-title">Vendor Onboarding</span>
          <span className="brand-subtitle">AI Workflow Engine</span>
        </div>
        <nav className="nav">
          <NavLink
            to="/"
            end
            className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
          >
            Dashboard
          </NavLink>
          <NavLink
            to="/review-queue"
            className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
          >
            Review Queue
          </NavLink>
          <NavLink
            to="/submit"
            className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
          >
            New Submission
          </NavLink>
        </nav>
      </aside>
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
