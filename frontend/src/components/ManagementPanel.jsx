import React from 'react';
import { useNavigate } from 'react-router-dom';
import LocationList from './LocationList';
import PersonList from './PersonList';

function ManagementPanel() {
    const navigate = useNavigate();

    return (
        <div className="management-panel">
            <div className="management-header">
                <h1>Управление справочниками</h1>
                <button onClick={() => navigate('/')} className="back-button">
                    &larr; Вернуться к фильмам
                </button>
            </div>

            <div className="management-content">
                <div className="management-section">
                    <PersonList />
                </div>

                <div className="management-section">
                    <LocationList />
                </div>
            </div>
        </div>
    );
}

export default ManagementPanel;