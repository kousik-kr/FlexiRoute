import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import MapCanvas from './components/MapCanvas';
import QueryForm from './components/QueryForm';
import QueryHistory from './components/QueryHistory';
import RouteStats from './components/RouteStats';
export default function App() {
    return (_jsxs("div", { className: "grid min-h-screen grid-cols-1 lg:grid-cols-[380px,1fr]", children: [_jsx("aside", { className: "bg-slate-900/70 p-6 backdrop-blur", children: _jsxs("div", { className: "flex flex-col gap-6", children: [_jsx("h1", { className: "text-2xl font-semibold text-white", children: "FlexRoute Planner" }), _jsx(QueryForm, {}), _jsx(RouteStats, {}), _jsx(QueryHistory, {})] }) }), _jsx("section", { className: "relative", children: _jsx(MapCanvas, {}) })] }));
}
