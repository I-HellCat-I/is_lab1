import React, {useEffect, useState} from 'react';
import axios from 'axios';

const LOCATIONS_API = '/api/locations';

const emptyLocationForm = {
    name: '',
    x: '',
    y: '',
};

function LocationForm({ onSave, onCancel, initialData }) {
    const [formData, setFormData] = useState(emptyLocationForm);
    const [errors, setErrors] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (initialData) {
            setFormData({
                name: initialData.name,
                x: initialData.x,
                y: initialData.y
            });
        }
    }, [initialData]);

    const validate = () => {
        const newErrors = {};
        if (!formData.name.trim()) newErrors.name = 'Название локации не может быть пустым.';
        if (formData.name.length > 824) newErrors.name = 'Название локации не может быть длиннее 824 символов.';
        if (formData.x === '' || isNaN(Number(formData.x))) newErrors.x = 'Координата X должна быть числом.';
        if (formData.y === '' || isNaN(Number(formData.y))) newErrors.y = 'Координата Y должна быть числом.';

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

        const locationDTO = {
            name: formData.name.trim(),
            x: parseFloat(formData.x),
            y: parseFloat(formData.y),
        };

        try {
            const response = await axios.post(LOCATIONS_API, locationDTO);
            onSave(response.data);
            setFormData(emptyLocationForm);
        } catch (err) {
            const errorMsg = err.response?.data || 'Неизвестная ошибка сервера.';
            setErrors({ general: `Провал операции: ${errorMsg}` });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="location-form">
            <h2>{initialData ? 'Редактирование локации' : 'Создание локации'}</h2>
            {errors.general && <p className="error-message">{errors.general}</p>}

            <div className="form-group">
                <label>Название локации</label>
                <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    placeholder="Введите название локации"
                />
                {errors.name && <span className="error-text">{errors.name}</span>}
            </div>

            <div className="form-group-row">
                <div className="form-group">
                    <label>Координата X</label>
                    <input
                        type="number"
                        name="x"
                        value={formData.x}
                        onChange={handleChange}
                        step="any"
                        placeholder="Введите X"
                    />
                    {errors.x && <span className="error-text">{errors.x}</span>}
                </div>

                <div className="form-group">
                    <label>Координата Y</label>
                    <input
                        type="number"
                        name="y"
                        value={formData.y}
                        onChange={handleChange}
                        step="any"
                        placeholder="Введите Y"
                    />
                    {errors.y && <span className="error-text">{errors.y}</span>}
                </div>
            </div>

            <div className="form-actions">
                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Сохранение...' : 'Создать локацию'}
                </button>
                <button type="button" onClick={onCancel} className="cancel-button">
                    Отмена
                </button>
            </div>
        </form>
    );
}

export default LocationForm;
