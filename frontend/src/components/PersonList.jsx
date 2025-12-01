import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { FaEdit, FaTrash } from 'react-icons/fa';
import Modal from 'react-modal';
import PersonForm from './PersonForm';

const PERSONS_API = '/api/persons';

function PersonList() {
    const [persons, setPersons] = useState([]);
    const [page, setPage] = useState(0);
    const pageSize = 5;
    const [editingPerson, setEditingPerson] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const fetchPersons = async () => {
        try {
            const res = await axios.get(PERSONS_API);
            setPersons(res.data);
        } catch (e) { console.error(e); }
    };

    useEffect(() => { fetchPersons(); }, []);

    const handleDelete = async (id) => {
        if (window.confirm('Удалить персону?')) {
            try {
                await axios.delete(`${PERSONS_API}/${id}`);
                fetchPersons();
            } catch (e) { alert('Ошибка удаления (возможно, связан с фильмом)'); }
        }
    };

    const handleEdit = (person) => {
        setEditingPerson(person);
        setIsModalOpen(true);
    };

    const handleCreate = () => {
        setEditingPerson(null);
        setIsModalOpen(true);
    };

    const handleFormSave = () => {
        setIsModalOpen(false);
        fetchPersons();
    };

    const totalPages = Math.ceil(persons.length / pageSize);
    const displayedPersons = persons.slice(page * pageSize, (page + 1) * pageSize);

    return (
        <div className="list-container">
            <div className="list-header">
                <h3>Список персон</h3>
                <button onClick={handleCreate} className="add-btn">Добавить персону</button>
            </div>
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Имя</th>
                    <th>Рост</th>
                    <th>Нац-сть</th>
                    <th>Локация</th>
                    <th>Действия</th>
                </tr>
                </thead>
                <tbody>
                {displayedPersons.map(p => (
                    <tr key={p.id}>
                        <td>{p.id}</td>
                        <td>{p.name}</td>
                        <td>{p.height}</td>
                        <td>{p.nationality}</td>
                        <td>{p.location ? p.location.name : '-'}</td>
                        <td>
                            <button onClick={() => handleEdit(p)}><FaEdit /></button>
                            <button onClick={() => handleDelete(p.id)}><FaTrash /></button>
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
                <PersonForm
                    initialData={editingPerson}
                    onSave={handleFormSave}
                    onCancel={() => setIsModalOpen(false)}
                />
            </Modal>
        </div>
    );
}

export default PersonList;