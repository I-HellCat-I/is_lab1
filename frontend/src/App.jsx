import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import MovieList from './components/MovieList';
import ManagementPanel from './components/ManagementPanel';

function App() {
    return (
        <Router>
            <div className="App">
                <main>
                    <Routes>
                        {/* Главная страница - список фильмов */}
                        <Route path="/" element={<MovieList />} />

                        {/* Страница управления данными */}
                        <Route path="/management" element={<ManagementPanel />} />

                        {/* Если ввели любой другой адрес - редирект на главную */}
                        <Route path="*" element={<Navigate to="/" replace />} />
                    </Routes>
                </main>
            </div>
        </Router>
    );
}

export default App;