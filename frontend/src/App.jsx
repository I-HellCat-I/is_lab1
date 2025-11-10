import React, { useState } from 'react';
import MovieList from './components/MovieList';
import ManagementPanel from './components/ManagementPanel';

function App() {
    const [currentPage, setCurrentPage] = useState('movies');

    return (
        <div className="App">
            <main>
                {currentPage === 'movies' && (
                    <MovieList onNavigateToManagement={() => setCurrentPage('management')} />
                )}
                {currentPage === 'management' && (
                    <ManagementPanel onNavigateToMovies={() => setCurrentPage('movies')} />
                )}
            </main>
        </div>
    );
}

export default App;