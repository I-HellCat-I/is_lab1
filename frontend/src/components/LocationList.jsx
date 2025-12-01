import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { FaEdit, FaTrash } from 'react-icons/fa';
import Modal from 'react-modal';
import LocationForm from './LocationForm';

const LOCATIONS_API = '/api/locations';

function LocationList() {
    const [locations, setLocations] = useState([]);
    const [page, setPage] = useState(0);
    const pageSize = 5;
    const [editingLocation, setEditingLocation] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const fetchLocations = async () => {
        try {
            const res = await axios.get(LOCATIONS_API);
            setLocations(res.data);
        } catch (e) { console.error(e); }
    };

    useEffect(() => { fetchLocations(); }, []);

    const handleDelete = async (id) => {
        if (window.confirm('Удалить локацию?')) {
            try {
                await axios.delete(`${LOCATIONS_API}/${id}`);
                fetchLocations();
            } catch (e) { alert('Ошибка удаления (возможно, используется)'); }
        }
    };

    const handleEdit = (loc) => {
        setEditingLocation(loc);
        setIsModalOpen(true);
    };

    const handleCreate = () => {
        setEditingLocation(null);
        setIsModalOpen(true);
    };

    const handleFormSave = () => {
        setIsModalOpen(false);
        fetchLocations();
    };

    // Client-side pagination logic
    const totalPages = Math.ceil(locations.length / pageSize);
    const displayedLocations = locations.slice(page * pageSize, (page + 1) * pageSize);

    return (
        <div className="list-container">
            <div className="list-header">
                <h3>Список локаций</h3>
                <button onClick={handleCreate} className="add-btn">Добавить локацию</button>
            </div>
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Название</th>
                    <th>X</th>
                    <th>Y</th>
                    <th>Действия</th>
                </tr>
                </thead>
                <tbody>
                {displayedLocations.map(loc => (
                    <tr key={loc.id}>
                        <td>{loc.id}</td>
                        <td>{loc.name}</td>
                        <td>{loc.x}</td>
                        <td>{loc.y}</td>
                        <td>
                            <button onClick={() => handleEdit(loc)}><FaEdit /></button>
                            <button onClick={() => handleDelete(loc.id)}><FaTrash /></button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
            <div className="pagination">
                <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>Назад</button>
                <span>{page + 1} / {totalPages || 1}</span>
                <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}>Вперед</button>
            </div>

            <Modal isOpen={isModalOpen} onRequestClose={() => setIsModalOpen(false)}>
                <LocationForm
                    initialData={editingLocation}
                    onSave={handleFormSave}
                    onCancel={() => setIsModalOpen(false)}
                />
            </Modal>
        </div>
    );
}

export default LocationList;