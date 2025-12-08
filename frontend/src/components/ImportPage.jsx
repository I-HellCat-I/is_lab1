import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const IMPORT_API = '/api/import';

function ImportPage() {
    const navigate = useNavigate();
    const [files, setFiles] = useState(null);
    const [history, setHistory] = useState([]);
    const [uploading, setUploading] = useState(false);

    const fetchHistory = async () => {
        try {
            const res = await axios.get(`${IMPORT_API}/history`);
            setHistory(res.data);
        } catch (e) {
            console.error("Failed to fetch history", e);
        }
    };

    // Опрос истории каждые 2 секунды для наблюдения за прогрессом и логами
    useEffect(() => {
        fetchHistory();
        const interval = setInterval(fetchHistory, 2000);
        return () => clearInterval(interval);
    }, []);

    const handleFileChange = (e) => {
        setFiles(e.target.files);
    };

    const handleUpload = async () => {
        if (!files || files.length === 0) return;

        const formData = new FormData();
        // ВАЖНО: Добавляем все файлы
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }

        setUploading(true);
        try {
            await axios.post(IMPORT_API, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            alert('Импорт запущен!');
            setFiles(null);
            // Сбрасываем input
            document.getElementById('fileInput').value = "";
            fetchHistory();
        } catch (e) {
            console.error("Import failed:", e);
            const msg = e.response?.data || e.message;
            alert(`Ошибка запуска импорта: ${msg}`);
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="import-page">
            <div className="management-header">
                <h1>Массовый импорт фильмов</h1>
                <button onClick={() => navigate('/')} className="back-button">На главную</button>
            </div>

            <div className="import-controls" style={{ padding: '20px', background: '#333', borderRadius: '8px', marginBottom: '20px' }}>
                <h3>Выберите файлы (JSON или YAML)</h3>
                <p>Минимальный размер для тестирования воркеров: 3000+ записей.</p>
                <input
                    id="fileInput"
                    type="file"
                    multiple
                    accept=".json,.yaml,.yml"
                    onChange={handleFileChange}
                    style={{ marginBottom: '10px' }}
                />
                <br/>
                <button onClick={handleUpload} disabled={uploading || !files}>
                    {uploading ? 'Запуск...' : 'Начать импорт'}
                </button>
            </div>

            <h3>История импорта (Real-time)</h3>
            <table className="history-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Файл</th>
                    <th>Статус</th>
                    <th>Добавлено</th>
                    <th>Ошибок</th>
                    <th>Лог воркеров</th>
                </tr>
                </thead>
                <tbody>
                {history.map(h => (
                    <tr key={h.id}>
                        <td>{h.id}</td>
                        <td>{h.fileName}</td>
                        <td style={{
                            color: h.status === 'SUCCESS' ? 'lightgreen' : (h.status === 'FAILED' ? 'red' : 'orange')
                        }}>
                            {h.status}
                        </td>
                        <td>{h.addedCount}</td>
                        <td>{h.failedCount}</td>
                        <td>
                                <pre style={{
                                    maxHeight: '200px', // Увеличим высоту
                                    overflowY: 'auto',
                                    fontSize: '11px',
                                    background: '#222',
                                    padding: '5px',
                                    whiteSpace: 'pre-wrap', // Чтобы переносились длинные строки
                                    color: '#0f0' // Зеленый текст как в терминале
                                }}>
                                    {h.logInfo || 'Waiting for logs...'} {/* Текст по умолчанию */}
                                </pre>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default ImportPage;