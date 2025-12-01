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
                        CONSTRAINT movies_oscars_count_check CHECK (oscars_count >= 0),
                        CONSTRAINT movies_budget_check CHECK (budget IS NULL OR budget > 0),
                        CONSTRAINT movies_total_box_office_check CHECK (total_box_office >= 0),
                        CONSTRAINT movies_length_check CHECK (length > 0),
                        CONSTRAINT movies_golden_palm_count_check CHECK (golden_palm_count >= 0),
    -- Внешние ключи
                        CONSTRAINT fk_coordinates FOREIGN KEY (coordinates_id) REFERENCES coordinates(id),
                        CONSTRAINT fk_director FOREIGN KEY (director_id) REFERENCES persons(id),
                        CONSTRAINT fk_screenwriter FOREIGN KEY (screenwriter_id) REFERENCES persons(id),
                        CONSTRAINT fk_operator FOREIGN KEY (operator_id) REFERENCES persons(id)
);

--- ДОБАВЛЕНИЕ ТЕСТОВЫХ ДАННЫХ ---

-- Добавляем локации
INSERT INTO locations (x, y, name) VALUES
                                       (55.7558, 37.6173, 'Mosfilm Studios, Moscow'),
                                       (34.0522, -118.2437, 'Hollywood, LA'),
                                       (40.7128, -74.0060, 'New York City'),
                                       (48.8566, 2.3522, 'Paris, France'),
                                       (39.0392, 125.7625, 'Pyongyang, North Korea'),
                                       (41.9029, 12.4534, 'Vatican City Archives');

-- Добавляем координаты
INSERT INTO coordinates (x, y) VALUES (10, 20.0), (15, 25.5), (50, -50.0), (100, 100.0), (-10, -10.0),
                                      (12, 13.0), (99, 1.0), (1, 99.0), (45, 45.0), (-100, -100.0),
                                      (77, 77.0), (33, 33.0), (22, 22.0), (11, 11.0), (55, 55.0),
                                      (66, 66.0), (88, 88.0), (44, 44.0), (123, 123.0), (321, -100.0),
                                      (5, 5.0), (6, 6.0), (7, 7.0);

-- Добавляем персон
INSERT INTO persons (name, eyeColor, hairColor, location_id, height, nationality) VALUES
                                                                                      ('Андрей Тарковский', 'BROWN', 'BROWN', 1, 175.0, 'RUSSIA'),
                                                                                      ('Stanley Kubrick', 'BROWN', 'BLACK', 2, 170.0, 'USA'),
                                                                                      ('Christopher Nolan', 'BLUE', 'BROWN', 2, 181.0, 'USA'),
                                                                                      ('Quentin Tarantino', 'BROWN', 'BLACK', 2, 185.0, 'USA'),
                                                                                      ('Bong Joon-ho', 'BLACK', 'BLACK', 2, 182.0, 'SOUTH_KOREA');


-- 3. Персоны (Persons)
-- id 1-5
INSERT INTO persons (name, eyeColor, hairColor, location_id, height, nationality) VALUES
                                                                                      ('Андрей Тарковский', 'BROWN', 'BROWN', 1, 175.0, 'RUSSIA'),
                                                                                      ('Stanley Kubrick', 'BROWN', 'BLACK', 2, 170.0, 'USA'),
                                                                                      ('Christopher Nolan', 'BLUE', 'BROWN', 2, 181.0, 'USA'),
                                                                                      ('Quentin Tarantino', 'BROWN', 'BLACK', 2, 185.0, 'USA'),
                                                                                      ('Bong Joon-ho', 'BLACK', 'BLACK', 2, 182.0, 'SOUTH_KOREA');

-- id 6-10
INSERT INTO persons (name, eyeColor, hairColor, location_id, height, nationality) VALUES
                                                                                        ('Сергей Эйзенштейн', 'BLUE', 'WHITE', 1, 168.0, 'RUSSIA'),
                                                                                        ('Steven Spielberg', 'BLUE', 'WHITE', 2, 172.0, 'USA'),
                                                                                        ('Kim Jong-il', 'BROWN', 'BLACK', 5, 160.0, 'NORTH_KOREA'), -- Великий руководитель тоже любил кино
                                                                                        ('Francis Ford Coppola', 'BROWN', 'WHITE', 3, 182.0, 'USA'),
                                                                                        ('Giuseppe Tornatore', 'BROWN', 'WHITE', 6, 176.0, 'VATICAN'); -- Условно

-- 4. Фильмы (Movies)
-- Всего 22 фильма разных жанров и рейтингов

-- Советская и Российская классика
INSERT INTO movies (name, coordinates_id, oscars_count, budget, total_box_office, mpaarating, director_id, screenwriter_id, operator_id, length, golden_palm_count, genre) VALUES
                                                                                                                                                                                ('Stalker', 1, 1, 2000000, 5000000, 'PG', 1, 1, 1, 162, 3, 'ADVENTURE'),
                                                                                                                                                                                ('Solaris', 2, 2, 1500000, 4000000, 'PG', 1, 1, 1, 167, 2, 'DRAMA'),
                                                                                                                                                                                ('Andrei Rublev', 3, 1, 1000000, 3000000, 'R', 1, 1, 1, 205, 1, 'TRAGEDY'),
                                                                                                                                                                                ('Battleship Potemkin', 4, 1, 50000, 1000000, 'PG', 6, 6, 6, 75, 0, 'DRAMA'),
                                                                                                                                                                                ('Brother', 5, 1, 100000, 2000000, 'R', 1, 1, 1, 99, 0, 'ACTION');

-- Фильмы Нолана
INSERT INTO movies (name, coordinates_id, oscars_count, budget, total_box_office, mpaarating, director_id, screenwriter_id, operator_id, length, golden_palm_count, genre) VALUES
                                                                                                                                                                                ('Inception', 6, 4, 160000000, 836800000, 'PG', 3, 3, 3, 148, 0, 'ACTION'),
                                                                                                                                                                                ('The Dark Knight', 7, 2, 185000000, 1004000000, 'PG', 3, 3, 3, 152, 0, 'ACTION'),
                                                                                                                                                                                ('Interstellar', 8, 1, 165000000, 701000000, 'PG', 3, 3, 3, 169, 0, 'ADVENTURE'),
                                                                                                                                                                                ('Oppenheimer', 9, 7, 100000000, 950000000, 'R', 3, 3, 3, 180, 0, 'DRAMA');

-- Фильмы Тарантино
INSERT INTO movies (name, coordinates_id, oscars_count, budget, total_box_office, mpaarating, director_id, screenwriter_id, operator_id, length, golden_palm_count, genre) VALUES
                                                                                                                                                                                ('Pulp Fiction', 10, 1, 8000000, 213900000, 'R', 4, 4, 4, 154, 1, 'ACTION'),
                                                                                                                                                                                ('Django Unchained', 11, 2, 100000000, 425000000, 'R', 4, 4, 4, 165, 0, 'ADVENTURE'),
                                                                                                                                                                                ('Kill Bill: Vol. 1', 12, 1, 30000000, 180000000, 'R', 4, 4, 4, 111, 0, 'ACTION');

-- Фильмы Кубрика
INSERT INTO movies (name, coordinates_id, oscars_count, budget, total_box_office, mpaarating, director_id, screenwriter_id, operator_id, length, golden_palm_count, genre) VALUES
                                                                                                                                                                                ('2001: A Space Odyssey', 13, 1, 12000000, 190000000, 'PG', 2, 2, 2, 149, 0, 'ADVENTURE'),
                                                                                                                                                                                ('The Shining', 14, 1, 19000000, 47000000, 'R', 2, 2, 2, 146, 0, 'TRAGEDY'),
                                                                                                                                                                                ('A Clockwork Orange', 15, 1, 2200000, 26000000, 'NC_17', 2, 2, 2, 136, 0, 'DRAMA');

-- Корейское кино (Bong Joon-ho)
INSERT INTO movies (name, coordinates_id, oscars_count, budget, total_box_office, mpaarating, director_id, screenwriter_id, operator_id, length, golden_palm_count, genre) VALUES
                                                                                                                                                                                ('Parasite', 16, 4, 11400000, 263000000, 'R', 5, 5, 5, 132, 1, 'TRAGEDY'),
                                                                                                                                                                                ('Snowpiercer', 17, 1, 40000000, 86000000, 'R', 5, 5, 5, 126, 0, 'ACTION');

-- Разное
INSERT INTO movies (name, coordinates_id, oscars_count, budget, total_box_office, mpaarating, director_id, screenwriter_id, operator_id, length, golden_palm_count, genre) VALUES
                                                                                                                                                                                ('The Godfather', 18, 3, 6000000, 246000000, 'R', 9, 9, 9, 175, 0, 'DRAMA'),
                                                                                                                                                                                ('Schindlers List', 19, 7, 22000000, 322000000, 'R', 7, 7, 7, 195, 0, 'TRAGEDY'),
                                                                                                                                                                                ('Pulgasari', 20, 1, 5000000, 10000, 'PG', 8, 8, 8, 95, 0, 'ADVENTURE'), -- Северокорейский фильм про Годзиллу
                                                                                                                                                                                ('Jurassic Park', 21, 3, 63000000, 1000000000, 'PG', 7, 7, 7, 127, 0, 'ADVENTURE'),
                                                                                                                                                                                ('Apocalypse Now', 22, 2, 31000000, 150000000, 'R', 9, 9, 9, 147, 1, 'TRAGEDY');