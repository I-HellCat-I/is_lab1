import React, { useState, useEffect } from 'react';
import axios from 'axios';

const PERSONS_API = '/api/persons';
const LOCATIONS_API = '/api/locations';

const emptyPersonForm = {
    name: '',
    eyeColor: '',
    hairColor: '',
    weight: '',
    nationality: '',
    locationId: '',
};

const colors = ['GREEN', 'BLUE', 'YELLOW', 'WHITE'];
const nationalities = ['RUSSIA', 'SOUTH_KOREA', 'NORTH_KOREA'];

function PersonForm({ onSave, onCancel, onAddLocation }) {
    const [formData, setFormData] = useState(emptyPersonForm);
    const [errors, setErrors] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [locations, setLocations] = useState([]);

    useEffect(() => {
        const fetchLocations = async () => {
            try {
                const response = await axios.get(LOCATIONS_API);
                setLocations(response.data);
            } catch (error) {
                setErrors(prev => ({ ...prev, general: 'Не могу загрузить локации.' }));
            }
        };

        fetchLocations();
    }, []);

    const validate = () => {
        const newErrors = {};
        if (!formData.name.trim()) newErrors.name = 'Имя персоны не может быть пустым.';
        if (!formData.eyeColor) newErrors.eyeColor = 'Необходимо выбрать цвет глаз.';
        if (!formData.hairColor) newErrors.hairColor = 'Необходимо выбрать цвет волос.';
        if (!formData.weight || isNaN(Number(formData.weight)) || Number(formData.weight) <= 0)
            newErrors.weight = 'Вес должен быть положительным числом.';
        if (!formData.nationality) newErrors.nationality = 'Необходимо выбрать национальность.';
        if (!formData.locationId) newErrors.locationId = 'Необходимо выбрать локацию.';

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleAddLocation = () => {
        onAddLocation();
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validate()) return;

        setIsSubmitting(true);
        setErrors({});

        const personDTO = {
            name: formData.name.trim(),
            eyeColor: formData.eyeColor,
            hairColor: formData.hairColor,
            weight: parseInt(formData.weight, 10),
            nationality: formData.nationality,
            location: {
                id: parseInt(formData.locationId, 10),
            },
        };

        try {
            const response = await axios.post(PERSONS_API, personDTO);
            onSave(response.data);
            setFormData(emptyPersonForm);
        } catch (err) {
            const errorMsg = err.response?.data || 'Неизвестная ошибка сервера.';
            setErrors({ general: `Провал операции: ${errorMsg}` });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="person-form">
            <h2>Создание новой персоны</h2>
            {errors.general && <p className="error-message">{errors.general}</p>}

            <div className="form-group">
                <label>Имя персоны</label>
                <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    placeholder="Введите имя"
                />
                {errors.name && <span className="error-text">{errors.name}</span>}
            </div>

            <div className="form-group-row">
                <div className="form-group">
                    <label>Цвет глаз</label>
                    <select name="eyeColor" value={formData.eyeColor} onChange={handleChange}>
                        <option value="">-- Не выбрано --</option>
                        {colors.map(color => <option key={color} value={color}>{color}</option>)}
                    </select>
                    {errors.eyeColor && <span className="error-text">{errors.eyeColor}</span>}
                </div>

                <div className="form-group">
                    <label>Цвет волос</label>
                    <select name="hairColor" value={formData.hairColor} onChange={handleChange}>
                        <option value="">-- Не выбрано --</option>
                        {colors.map(color => <option key={color} value={color}>{color}</option>)}
                    </select>
                    {errors.hairColor && <span className="error-text">{errors.hairColor}</span>}
                </div>
            </div>

            <div className="form-group-row">
                <div className="form-group">
                    <label>Вес (кг)</label>
                    <input
                        type="number"
                        name="weight"
                        value={formData.weight}
                        onChange={handleChange}
                        placeholder="Введите вес"
                    />
                    {errors.weight && <span className="error-text">{errors.weight}</span>}
                </div>

                <div className="form-group">
                    <label>Национальность</label>
                    <select name="nationality" value={formData.nationality} onChange={handleChange}>
                        <option value="">-- Не выбрано --</option>
                        {nationalities.map(nat => <option key={nat} value={nat}>{nat}</option>)}
                    </select>
                    {errors.nationality && <span className="error-text">{errors.nationality}</span>}
                </div>
            </div>

            <div className="form-group">
                <label>Локация</label>
                <div className="location-select-wrapper">
                    <select name="locationId" value={formData.locationId} onChange={handleChange}>
                        <option value="">-- Не выбрано --</option>
                        {locations.map(loc => <option key={loc.id} value={loc.id}>{loc.name}</option>)}
                    </select>
                    <button type="button" onClick={handleAddLocation} className="add-button">
                        + Добавить локацию
                    </button>
                </div>
                {errors.locationId && <span className="error-text">{errors.locationId}</span>}
            </div>

            <div className="form-actions">
                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Сохранение...' : 'Создать персону'}
                </button>
                <button type="button" onClick={onCancel} className="cancel-button">
                    Отмена
                </button>
            </div>
        </form>
    );
}

export default PersonForm;
