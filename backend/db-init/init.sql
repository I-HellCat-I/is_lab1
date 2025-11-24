-- Удаляем таблицы, если они существуют, для обеспечения идемпотентности скрипта
DROP TABLE IF EXISTS movies CASCADE;
DROP TABLE IF EXISTS persons CASCADE;
DROP TABLE IF EXISTS coordinates CASCADE;
DROP TABLE IF EXISTS locations CASCADE;

CREATE TYPE mpaa_rating AS ENUM ('PG', 'R', 'NC_17');
CREATE TYPE movie_genre AS ENUM ('ACTION', 'DRAMA', 'ADVENTURE', 'TRAGEDY');
CREATE TYPE color AS ENUM ('GREEN', 'BLUE', 'YELLOW', 'WHITE', 'RED', 'BLACK', 'BROWN');
CREATE TYPE country AS ENUM ('RUSSIA', 'SOUTH_KOREA', 'NORTH_KOREA', 'USA', 'VATICAN');

-- Таблица для локаций
CREATE TABLE locations (
                           id BIGSERIAL PRIMARY KEY,
                           x DOUBLE PRECISION NOT NULL,
                           y REAL NOT NULL,
                           name VARCHAR(255)
);

-- Таблица для координат
CREATE TABLE coordinates (
                             id BIGSERIAL PRIMARY KEY,
                             x BIGINT NOT NULL,
                             y DOUBLE PRECISION NOT NULL,
    -- Ограничение из Java-класса
                             CONSTRAINT coordinates_y_check CHECK (y > -115)
);

-- Таблица для персон (режиссеры, сценаристы, операторы)
CREATE TABLE persons (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         eyeColor color,
                         hairColor color,
                         location_id BIGINT,
                         height REAL NOT NULL,
                         nationality country,
    -- Ограничения
                         CONSTRAINT persons_height_check CHECK (height > 0),
                         CONSTRAINT fk_location FOREIGN KEY (location_id) REFERENCES locations(id)
);

-- Основная таблица для фильмов
CREATE TABLE movies (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        coordinates_id BIGINT NOT NULL,
                        creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        oscars_count BIGINT NOT NULL,
                        budget DOUBLE PRECISION,
                        total_box_office REAL NOT NULL,
                        mpaarating mpaa_rating,
                        director_id BIGINT,
                        screenwriter_id BIGINT NOT NULL,
                        operator_id BIGINT,
                        length INTEGER NOT NULL,
                        golden_palm_count INTEGER NOT NULL,
                        genre movie_genre
    -- Ограничения
                        CONSTRAINT movies_oscars_count_check CHECK (oscars_count > 0),
                        CONSTRAINT movies_budget_check CHECK (budget IS NULL OR budget > 0),
                        CONSTRAINT movies_total_box_office_check CHECK (total_box_office > 0),
                        CONSTRAINT movies_length_check CHECK (length > 0),
                        CONSTRAINT movies_golden_palm_count_check CHECK (golden_palm_count > 0),
    -- Внешние ключи
                        CONSTRAINT fk_coordinates FOREIGN KEY (coordinates_id) REFERENCES coordinates(id),
                        CONSTRAINT fk_director FOREIGN KEY (director_id) REFERENCES persons(id),
                        CONSTRAINT fk_screenwriter FOREIGN KEY (screenwriter_id) REFERENCES persons(id),
                        CONSTRAINT fk_operator FOREIGN KEY (operator_id) REFERENCES persons(id)
);

--- ДОБАВЛЕНИЕ ТЕСТОВЫХ ДАННЫХ ---

-- Добавляем локации
INSERT INTO locations (x, y, name) VALUES (34.0522, -118.2437, 'Los Angeles');
INSERT INTO locations (x, y, name) VALUES (40.7128, -74.0060, 'New York');

-- Добавляем координаты
INSERT INTO coordinates (x, y) VALUES (10, 20.5);
INSERT INTO coordinates (x, y) VALUES (-50, 100.1);

-- Добавляем персон
INSERT INTO persons (name, eyeColor, hairColor, location_id, height, nationality) VALUES
                                                                                        ('Christopher Nolan', 'BLUE', 'BROWN', 1, 181.0, 'USA'),
                                                                                        ('Jonathan Nolan', 'BLUE', 'BLACK', 2, 185.0, 'USA'),
                                                                                        ('Wally Pfister', 'BROWN', 'BROWN', 1, 178.0, 'USA'),
                                                                                        ('Quentin Tarantino', 'BROWN', 'BLACK', 1, 185, 'USA'),
                                                                                        ('Roger Avary', 'BLACK', 'BROWN', 1, 180, 'USA');


-- Добавляем фильмы
INSERT INTO movies (name, coordinates_id, oscars_count, budget, total_box_office, mpaarating, director_id, screenwriter_id, operator_id, length, golden_palm_count, genre) VALUES
                                                                                                                                                                                ('Inception', 1, 4, 160000000, 836800000, 'PG', 1, 1, 3, 148, 1, 'ACTION'),
                                                                                                                                                                                ('Pulp Fiction', 2, 1, 8000000, 213900000, 'R', 4, 5, null, 154, 1, 'DRAMA');

INSERT INTO movies (name, coordinates_id, oscars_count, budget, total_box_office, mpaarating, director_id, screenwriter_id, operator_id, length, golden_palm_count, genre) VALUES
    ('The Dark Knight', 1, 2, 185000000, 1004000000, 'PG', 1, 2, 3, 152, 1, 'ACTION');