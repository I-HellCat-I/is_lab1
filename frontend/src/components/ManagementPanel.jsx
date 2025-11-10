import React, { useState } from 'react';
import Modal from 'react-modal';
import LocationForm from './LocationForm';
import PersonForm from './PersonForm';

Modal.setAppElement('#root');

function ManagementPanel({ onNavigateToMovies }) {
    const [locationModalOpen, setLocationModalOpen] = useState(false);
    const [personModalOpen, setPersonModalOpen] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');

    const handleOpenLocationForm = () => {
        setLocationModalOpen(true);
    };

    const handleOpenPersonForm = () => {
        setPersonModalOpen(true);
    };

    const handleLocationSave = (location) => {
        setLocationModalOpen(false);
        setSuccessMessage(`Локация "${location.name}" успешно создана!`);
        setTimeout(() => setSuccessMessage(''), 3000);
    };

    const handlePersonSave = (person) => {
        setPersonModalOpen(false);
        setSuccessMessage(`Персона "${person.name}" успешно создана!`);
        setTimeout(() => setSuccessMessage(''), 3000);
    };

    const handleAddLocationFromPersonForm = () => {
        setPersonModalOpen(false);
        setLocationModalOpen(true);
    };

    return (
        <div className="management-panel">
            <h1>Управление данными</h1>

            {successMessage && <div className="success-message">{successMessage}</div>}

            <div className="management-buttons">
                <button onClick={handleOpenLocationForm} className="management-button">
                    Добавить локацию
                </button>
                <button onClick={handleOpenPersonForm} className="management-button">
                    Добавить персону
                </button>
                <button onClick={onNavigateToMovies} className="management-button back-button">
                    Вернуться к фильмам
                </button>
            </div>

            <div className="management-info">
                <p>На этой странице вы можете управлять основными данными системы:</p>
                <ul>
                    <li>Создавать новые локации для персон</li>
                    <li>Добавлять персон (режиссеров, сценаристов, операторов) с привязкой к локации</li>
                </ul>
            </div>

            <Modal
                isOpen={locationModalOpen}
                onRequestClose={() => setLocationModalOpen(false)}
                contentLabel="Форма локации">
                <LocationForm
                    onSave={handleLocationSave}
                    onCancel={() => setLocationModalOpen(false)}
                />
            </Modal>

            <Modal
                isOpen={personModalOpen}
                onRequestClose={() => setPersonModalOpen(false)}
                contentLabel="Форма персоны">
                <PersonForm
                    onSave={handlePersonSave}
                    onCancel={() => setPersonModalOpen(false)}
                    onAddLocation={handleAddLocationFromPersonForm}
                />
            </Modal>
        </div>
    );
}

export default ManagementPanel;
