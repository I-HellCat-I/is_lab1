import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import MovieList from './components/MovieList';
import ManagementPanel from './components/ManagementPanel';
import ImportPage from "./components/ImportPage.jsx";

function App() {
    return (
        <Router>
            <div className="App">
                <main>
                    <Routes>
                        <Route path="/" element={<MovieList />} />
                        <Route path="/management" element={<ManagementPanel />} />
                        <Route path="/import" element={<ImportPage />} />
                        <Route path="*" element={<Navigate to="/" replace />} />
                    </Routes>
                </main>
            </div>
        </Router>
    );
}

export default App;