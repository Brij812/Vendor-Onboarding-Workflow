import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import NewSubmission from './pages/NewSubmission';
import ReviewQueue from './pages/ReviewQueue';
import RunDetails from './pages/RunDetails';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="review-queue" element={<ReviewQueue />} />
        <Route path="runs/:id" element={<RunDetails />} />
        <Route path="submit" element={<NewSubmission />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
