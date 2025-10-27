import React, { useState, useEffect } from 'react';
import axios from 'axios';

const MOVIES_API = '/api/movies';
const PERSONS_API = '/api/persons';
const ENUMS_API = '/api/meta/enums';

const emptyMovieForm = {
    name: '',
    coordinatesX: '',
    coordinatesY: '',
    oscarsCount: '',
    budget: '',
    totalBoxOffice: '',
    mpaaRating: '',
    directorId: '',
    screenwriterId: '',
    operatorId: '',
    length: '',
    goldenPalmCount: '',
    genre: '',
};

function MovieForm({ movie, onSave, onCancel }) {
    const [formData, setFormData] = useState(emptyMovieForm);
    const [errors, setErrors] = useState({});
    const [persons, setPersons] = useState([]);
    const [enums, setEnums] = useState({ mpaaRatings: [], movieGenres: [] });
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (movie) {
            setFormData({
                name: movie.name || '',
                coordinatesX: movie.coordinatesX || '',
                coordinatesY: movie.coordinatesY || '',
                oscarsCount: movie.oscarsCount || '',
                budget: movie.budget || '',
                totalBoxOffice: movie.totalBoxOffice || '',
                mpaaRating: movie.mpaaRating || '',
                directorId: movie.director ? movie.director.id : '',
                screenwriterId: movie.screenwriter ? movie.screenwriter.id : '',
                operatorId: movie.operator ? movie.operator.id : '',
                length: movie.length || '',
                goldenPalmCount: movie.goldenPalmCount || '',
                genre: movie.genre || '',
            });
        } else {
            setFormData(emptyMovieForm);
        }

        const fetchPrerequisites = async () => {
            try {
                const [personsRes, enumsRes] = await Promise.all([
                    axios.get(PERSONS_API),
                    axios.get(ENUMS_API)
                ]);
                setPersons(personsRes.data);
                setEnums(enumsRes.data);
            } catch (error) {
                setErrors({ general: 'Не могу загрузить вспомогательные данные (режиссеры, жанры).' });
            }
        };

        fetchPrerequisites();
    }, [movie]);

    const validate = () => {
        const newErrors = {};
        if (!formData.name.trim()) newErrors.name = 'Название не может быть пустым.';
        const oscarsCount = Number(formData.oscarsCount);
        if (isNaN(oscarsCount) || oscarsCount <= 0) newErrors.oscarsCount = 'Количество Оскаров должно быть целым числом > 0.';
        if (formData.budget && (isNaN(Number(formData.budget)) || Number(formData.budget) <= 0)) newErrors.budget = 'Бюджет должен быть числом > 0.';
        if (formData.coordinatesY && Number(formData.coordinatesY) <= -158) newErrors.coordinatesY = 'Координата Y должна быть > -158.';
        if (!formData.screenwriterId) newErrors.screenwriterId = 'Сценарист - обязательное поле!';
        //... другие правила

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validate()) return;

        setIsSubmitting(true);
        setErrors({});

        // Готовим DTO. Преобразуем строки в числа, где это необходимо.
        const movieDTO = {
            name: formData.name.trim(),
            coordinatesX: parseFloat(formData.coordinatesX) || 0,
            coordinatesY: parseFloat(formData.coordinatesY) || 0,
            oscarsCount: parseInt(formData.oscarsCount, 10),
            budget: formData.budget ? parseFloat(formData.budget) : null,
            totalBoxOffice: parseInt(formData.totalBoxOffice, 10),
            mpaaRating: formData.mpaaRating || null,
            length: parseInt(formData.length, 10),
            goldenPalmCount: parseInt(formData.goldenPalmCount, 10),
            genre: formData.genre || null,
            director: formData.directorId ? { id: parseInt(formData.directorId, 10) } : null,
            screenwriter: { id: parseInt(formData.screenwriterId, 10) },
            operator: formData.operatorId ? { id: parseInt(formData.operatorId, 10) } : null,
        };

        try {
            if (movie) {
                await axios.put(`${MOVIES_API}/${movie.id}`, movieDTO);
            } else {
                await axios.post(MOVIES_API, movieDTO);
            }
            onSave(); // Вызываем коллбэк (закроет модалку и т.д.)
        } catch (err) {
            const errorMsg = err.response?.data?.message || err.response?.data || 'Неизвестная ошибка сервера.';
            setErrors({ general: `Провал операции: ${errorMsg}` });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="movie-form">
            <h2>{movie ? 'Редактирование фильма' : 'Создание нового фильма'}</h2>
            {errors.general && <p className="error-message">{errors.general}</p>}

            {/* Поля формы. Используем .map для DRY-принципа */}
            {[
                { name: 'name', label: 'Название', type: 'text' },
                { name: 'oscarsCount', label: 'Кол-во Оскаров', type: 'number' },
                { name: 'budget', label: 'Бюджет', type: 'number' },
                { name: 'totalBoxOffice', label: 'Кассовые сборы', type: 'number' },
                { name: 'length', label: 'Длительность (мин)', type: 'number' },
                { name: 'goldenPalmCount', label: 'Кол-во Золотых пальм', type: 'number' },
                { name: 'coordinatesX', label: 'Координата X', type: 'number' },
                { name: 'coordinatesY', label: 'Координата Y (> -158)', type: 'number' },
            ].map(field => (
                <div className="form-group" key={field.name}>
                    <label>{field.label}</label>
                    <input type={field.type} name={field.name} value={formData[field.name]} onChange={handleChange} step={field.type === 'number' ? 'any' : undefined} />
                    {errors[field.name] && <span className="error-text">{errors[field.name]}</span>}
                </div>
            ))}

            {/* Выпадающие списки */}
            {[
                { name: 'screenwriterId', label: 'Сценарист', options: persons },
                { name: 'directorId', label: 'Режиссер', options: persons },
                { name: 'operatorId', label: 'Оператор', options: persons },
            ].map(field => (
                <div className="form-group" key={field.name}>
                    <label>{field.label}</label>
                    <select name={field.name} value={formData[field.name]} onChange={handleChange}>
                        <option value="">-- Не выбрано --</option>
                        {field.options.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                    </select>
                    {errors[field.name] && <span className="error-text">{errors[field.name]}</span>}
                </div>
            ))}

            {[
                { name: 'genre', label: 'Жанр', options: enums.movieGenres },
                { name: 'mpaaRating', label: 'MPAA Рейтинг', options: enums.mpaaRatings },
            ].map(field => (
                <div className="form-group" key={field.name}>
                    <label>{field.label}</label>
                    <select name={field.name} value={formData[field.name]} onChange={handleChange}>
                        <option value="">-- Не выбрано --</option>
                        {(field.options || []).map(opt => <option key={opt} value={opt}>{opt}</option>)}
                    </select>
                </div>
            ))}

            <div className="form-actions">
                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Сохранение...' : 'Сохранить'}
                </button>
                <button type="button" onClick={onCancel} className="cancel-button">
                    Отмена
                </button>
            </div>
        </form>
    );
}

export default MovieForm;