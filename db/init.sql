-- Устанавливаем часовой пояс для заседаний Политбюро
SET TIME ZONE 'UTC';

-- СОЗДАЕМ НАШИ ПЕРЕЧИСЛЕНИЯ (ENUMs) - ЭТО НАШИ ПАРТИЙНЫЕ ДИРЕКТИВЫ
CREATE TYPE mpaa_rating AS ENUM ('G', 'PG', 'NC_17');
CREATE TYPE movie_genre AS ENUM ('WESTERN', 'TRAGEDY', 'THRILLER', 'FANTASY');
CREATE TYPE color AS ENUM ('GREEN', 'BLUE', 'YELLOW', 'WHITE');
CREATE TYPE country AS ENUM ('RUSSIA', 'SOUTH_KOREA', 'NORTH_KOREA');

-- СОЗДАЕМ ТАБЛИЦЫ В ПОРЯДКЕ ЗАВИСИМОСТЕЙ. СНАЧАЛА НЕЗАВИСИМЫЕ.

-- Локации для наших агентов
CREATE TABLE locations (
                           id SERIAL PRIMARY KEY,
                           x REAL NOT NULL,
                           y DOUBLE PRECISION NOT NULL,
                           name VARCHAR(824) NOT NULL CHECK (name <> '')
);

-- Персоналии (режиссеры, сценаристы, операторы)
CREATE TABLE persons (
                         id SERIAL PRIMARY KEY,
                         name TEXT NOT NULL CHECK (name <> ''),
                         eye_color color NOT NULL,
                         hair_color color NOT NULL,
                         location_id INTEGER NOT NULL,
                         weight BIGINT NOT NULL CHECK (weight > 0),
                         nationality country,

                         CONSTRAINT fk_location FOREIGN KEY (location_id) REFERENCES locations (id)
);

-- Фильмы - наша основная производственная единица
CREATE TABLE movies (
                        id BIGSERIAL PRIMARY KEY, -- BIGSERIAL для long, автоматически > 0, unique
                        name TEXT NOT NULL CHECK (name <> ''),
                        coordinates_x DOUBLE PRECISION NOT NULL,
                        coordinates_y REAL NOT NULL CHECK (coordinates_y > -158),
                        creation_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        oscars_count BIGINT NOT NULL CHECK (oscars_count > 0),
                        budget DECIMAL(19, 4) CHECK (budget > 0),
                        total_box_office INTEGER NOT NULL CHECK (total_box_office > 0),
                        mpaa_rating mpaa_rating,
                        director_id INTEGER,
                        screenwriter_id INTEGER NOT NULL,
                        operator_id INTEGER,
                        length INTEGER NOT NULL CHECK (length > 0),
                        golden_palm_count BIGINT NOT NULL CHECK (golden_palm_count > 0),
                        genre movie_genre,

                        CONSTRAINT fk_director FOREIGN KEY (director_id) REFERENCES persons (id),
                        CONSTRAINT fk_screenwriter FOREIGN KEY (screenwriter_id) REFERENCES persons (id),
                        CONSTRAINT fk_operator FOREIGN KEY (operator_id) REFERENCES persons (id)
);

-- Устанавливаем начальное значение для ID, чтобы соответствовать > 0
-- (хотя SERIAL/BIGSERIAL и так начинается с 1)
ALTER SEQUENCE movies_id_seq RESTART WITH 1;

-- ФУНКЦИИ ДЛЯ СПЕЦОПЕРАЦИЙ

-- 1. Удалить всех, кто получил слишком много золота (чистка)
CREATE OR REPLACE FUNCTION delete_movies_by_golden_palm(palm_count BIGINT)
RETURNS void AS $$
BEGIN
DELETE FROM movies WHERE golden_palm_count = palm_count;
END;
$$ LANGUAGE plpgsql;

-- 2. Сгруппировать по режиссерам (перекличка)
CREATE OR REPLACE FUNCTION group_movies_by_director()
RETURNS TABLE(director_name TEXT, movie_count BIGINT) AS $$
BEGIN
RETURN QUERY
SELECT p.name, COUNT(m.id)
FROM movies m
         JOIN persons p ON m.director_id = p.id
GROUP BY p.name;
END;
$$ LANGUAGE plpgsql;

-- 3. Подсчитать фильмы по жанру (статистика для Госплана)
CREATE OR REPLACE FUNCTION count_movies_by_genre(genre_name movie_genre)
RETURNS BIGINT AS $$
DECLARE
movie_count BIGINT;
BEGIN
SELECT COUNT(*) INTO movie_count FROM movies WHERE genre = genre_name;
RETURN movie_count;
END;
$$ LANGUAGE plpgsql;

-- 4. Найти операторов-неудачников (выявление саботажников)
CREATE OR REPLACE FUNCTION get_operators_without_oscars()
RETURNS TABLE(operator_id INTEGER, operator_name TEXT) AS $$
BEGIN
RETURN QUERY
SELECT p.id, p.name
FROM persons p
WHERE p.id IN (SELECT DISTINCT m.operator_id FROM movies m) -- Убедимся, что это оператор
  AND NOT EXISTS (
    SELECT 1
    FROM movies m2
    WHERE m2.operator_id = p.id AND m2.oscars_count > 0
);
END;
$$ LANGUAGE plpgsql;

-- 5. Наградить достойных (премирование стахановцев)
CREATE OR REPLACE FUNCTION add_oscars_to_long_films(min_length INTEGER, award_count BIGINT)
RETURNS void AS $$
BEGIN
UPDATE movies
SET oscars_count = oscars_count + award_count
WHERE length > min_length;
END;
$$ LANGUAGE plpgsql;