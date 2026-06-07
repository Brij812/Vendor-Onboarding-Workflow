export default function LoadingState({ message = 'Loading...' }: { message?: string }) {
  return (
    <div className="state-box">
      <div className="spinner" aria-hidden="true" />
      <p className="muted">{message}</p>
    </div>
  );
}
