import MapCanvas from './components/MapCanvas';
import QueryForm from './components/QueryForm';
import QueryHistory from './components/QueryHistory';
import RouteStats from './components/RouteStats';

export default function App() {
  return (
    <div className="grid min-h-screen grid-cols-1 lg:grid-cols-[380px,1fr]">
      <aside className="bg-slate-900/70 p-6 backdrop-blur">
        <div className="flex flex-col gap-6">
          <h1 className="text-2xl font-semibold text-white">FlexRoute Planner</h1>
          <QueryForm />
          <RouteStats />
          <QueryHistory />
        </div>
      </aside>
      <section className="relative">
        <MapCanvas />
      </section>
    </div>
  );
}
