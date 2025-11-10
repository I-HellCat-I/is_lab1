import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import Modal from 'react-modal';
import { FaSort, FaSortUp, FaSortDown, FaEdit, FaTrash } from 'react-icons/fa';
import MovieForm from './MovieForm';
import PersonForm from './PersonForm';
import LocationForm from './LocationForm';

// Устанавливаем корневой элемент для модального окна
Modal.setAppElement('#root');

const API_URL = '/api/movies';
const wsHost = window.location.host;
const WS_URL = `ws://${wsHost}/notifications`;

const headers = [
    { key: 'id', name: 'ID' },
    { key: 'name', name: 'Название' },
    { key: 'oscarsCount', name: 'Оскары' },
    { key: 'director.name', name: 'Режиссер' }, // Дот-нотация для сортировки
    { key: 'length', name: 'Длительность' }
];

function MovieList({ onNavigateToManagement }) {
    // --- Состояния ---
    const [movies, setMovies] = useState([]);
    const [pageInfo, setPageInfo] = useState({ currentPage: 0, totalPages: 0 });
    const [pagination, setPagination] = useState({ page: 0, size: 10 });
    const [sort, setSort] = useState({ field: 'id', direction: 'asc' });
    const [filter, setFilter] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedMovie, setSelectedMovie] = useState(null);
    const [personModalOpen, setPersonModalOpen] = useState(false);
    const [locationModalOpen, setLocationModalOpen] = useState(false);

    // --- Загрузка данных ---
    const fetchMovies = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const params = {
                page: pagination.page,
                size: pagination.size,
                sort: `${sort.field},${sort.direction}`,
                filter: filter,
            };
            const response = await axios.get(API_URL, { params });
            setMovies(response.data.content);
            setPageInfo({
                currentPage: response.data.currentPage,
                totalPages: response.data.totalPages,
            });
        } catch (err) {
            setError('Не могу получить список фильмов от ЦК.');
        } finally {
            setLoading(false);
        }
    }, [pagination, sort, filter]);

    const openCreateModal = () => {
        setSelectedMovie(null); // Очищаем выбор для создания нового
        setIsModalOpen(true);
    };

    const openEditModal = (movie) => {
        setSelectedMovie(movie); // Устанавливаем фильм для редактирования
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
    };

    const handleSave = () => {
        closeModal();
        // Нам НЕ нужно вызывать fetchMovies() здесь!
        // Бэкенд пришлет WebSocket-сообщение, и наш useEffect
        // сам обработает обновление. Это и есть реактивность!
    };

    const handleAddPerson = () => {
        setPersonModalOpen(true);
    };

    const handleAddLocation = () => {
        setLocationModalOpen(true);
    };

    const handlePersonSave = (person) => {
        setPersonModalOpen(false);
    };

    const handleLocationSave = (location) => {
        setLocationModalOpen(false);
    };

    // --- Эффекты ---
    useEffect(() => {
        fetchMovies();
    }, [fetchMovies]);

    useEffect(() => {
        const ws = new WebSocket(WS_URL);
        ws.onopen = () => console.log('Прямая линия с Кремлём установлена!');
        ws.onclose = () => console.log('Связь с Кремлём потеряна! Контрреволюция!');
        ws.onerror = (err) => setError('Прямая линия с Кремлём повреждена саботажниками!');

        // --- ВОТ ОНА, БЛЯДЬ, МАГИЯ! ---
        ws.onmessage = (event) => {
            try {
                // 1. Дешифруем приказ
                const message = JSON.parse(event.data);
                console.log('Получена шифрограмма:', message);

                // 2. Действуем в соответствии с приказом
                switch (message.type) {
                    case 'CREATED':
                        // При создании нового фильма, самый простой путь - перезапросить текущую страницу.
                        // Он может появиться на ней, если сортировка подходит.
                        console.log('Приказ CREATED: Перезапрашиваю текущую страницу...');
                        fetchMovies();
                        break;

                    case 'UPDATED':
                        // Точечное обновление! Находим бойца в строю и даем ему новые данные.
                        console.log(`Приказ UPDATED для ID ${message.payload.id}: Обновляю бойца в строю...`);
                        setMovies(currentMovies =>
                            currentMovies.map(movie =>
                                movie.id === message.payload.id ? message.payload : movie
                            )
                        );
                        break;

                    case 'DELETED':
                        // Точечное удаление! Расстреливаем врага народа прямо в строю.
                        console.log(`Приказ DELETED для ID ${message.payload.id}: Ликвидирую предателя...`);
                        setMovies(currentMovies =>
                            currentMovies.filter(movie => movie.id !== message.payload.id)
                        );
                        // Тут можно было бы запросить один новый элемент, чтобы страница не уменьшалась, но это усложнение.
                        break;

                    case 'BULK_UPDATE':
                    case 'BULK_DELETE':
                        // При массовых операциях мы не знаем, что именно изменилось.
                        // Здесь оправдана полная перезагрузка.
                        console.log('Приказ о массовой операции: Полная боевая готовность! Перезапрашиваю всё...');
                        fetchMovies();
                        break;

                    default:
                        console.warn('Получен непонятный приказ:', message.type);
                }
            } catch (e) {
                console.error("Не могу расшифровать приказ!", e);
            }
        };

        return () => ws.close();
    }, [fetchMovies]); // Зависимость от fetchMovies, чтобы при его изменении переподключаться

    // --- Обработчики ---
    const handleSort = (field) => {
        const direction = (sort.field === field && sort.direction === 'asc') ? 'desc' : 'asc';
        setSort({ field, direction });
        setPagination(p => ({ ...p, page: 0 }));
    };

    const handleFilterChange = (e) => {
        setFilter(e.target.value);
        setPagination(p => ({ ...p, page: 0 }));
    };

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < pageInfo.totalPages) {
            setPagination(p => ({ ...p, page: newPage }));
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm(`Товарищ, ты уверен, что хочешь расстрелять фильм с ID ${id}?`)) {
            try {
                await axios.delete(`${API_URL}/${id}`);
                // Нам не нужно делать fetchMovies()! WebSocket сделает это за нас.
                // Когда бэкенд удалит фильм, он пришлет сообщение, и onmessage() сам обновит UI.
                // Это и есть декларативный, реактивный подход!
            } catch (err) {
                setError(`Ошибка при выполнении расстрельного приговора для ID ${id}`);
            }
        }
    };

    // --- Функции для рендеринга ---
    const renderSortIcon = (field) => {
        if (sort.field !== field) return <FaSort />;
        if (sort.direction === 'asc') return <FaSortUp />;
        return <FaSortDown />;
    };

    return (
        <div>
            <h1>Штаб Управления Советским Кинематографом</h1>
            <div className="controls">
                <input
                    type="text"
                    placeholder="Фильтр по названию, режиссеру..."
                    value={filter}
                    onChange={handleFilterChange}
                />
                <button onClick={openCreateModal}>Добавить фильм</button>
                <button onClick={onNavigateToManagement} className="management-link-button">Управление данными</button>
            </div>

            {loading && <div>Загрузка данных с Политбюро...</div>}
            {error && <div style={{color: 'red'}}>{error}</div>}

            <table>
                {/* ... thead с renderSortIcon ... */}
                <tbody>
                {movies.map(movie => (
                    <tr key={movie.id}>
                        <td>{movie.id}</td>
                        <td>{movie.name}</td>
                        <td>{movie.oscarsCount}</td>
                        <td>{movie.director ? movie.director.name : 'Н/Д'}</td>
                        <td>{movie.length}</td>
                        <td>
                            <button onClick={() => openEditModal(movie)}><FaEdit /></button>
                            <button onClick={() => handleDelete(movie.id)}><FaTrash /></button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            <div className="pagination">
                <button onClick={() => handlePageChange(pageInfo.currentPage - 1)} disabled={pageInfo.currentPage === 0}>
                    Назад
                </button>
                <span>Страница {pageInfo.currentPage + 1} из {pageInfo.totalPages}</span>
                <button onClick={() => handlePageChange(pageInfo.currentPage + 1)} disabled={pageInfo.currentPage >= pageInfo.totalPages - 1}>
                    Вперед
                </button>
            </div>

            <Modal
                isOpen={isModalOpen}
                onRequestClose={closeModal}
                contentLabel="Форма Фильма">
                <MovieForm
                    movie={selectedMovie}
                    onSave={handleSave}
                    onCancel={closeModal}
                    onAddPerson={handleAddPerson}
                    onAddLocation={handleAddLocation}
                />
            </Modal>

            <Modal
                isOpen={personModalOpen}
                onRequestClose={() => setPersonModalOpen(false)}
                contentLabel="Форма персоны">
                <PersonForm
                    onSave={handlePersonSave}
                    onCancel={() => setPersonModalOpen(false)}
                    onAddLocation={handleAddLocation}
                />
            </Modal>

            <Modal
                isOpen={locationModalOpen}
                onRequestClose={() => setLocationModalOpen(false)}
                contentLabel="Форма локации">
                <LocationForm
                    onSave={handleLocationSave}
                    onCancel={() => setLocationModalOpen(false)}
                />
            </Modal>
        </div>
    );
}

export default MovieList;