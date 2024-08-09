DROP PROCEDURE IF EXISTS add_movie;
DROP PROCEDURE IF EXISTS add_star;

DELIMITER $$

CREATE PROCEDURE add_movie(
    IN title VARCHAR(100),
    IN year INTEGER,
    IN director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN star_birth_year INTEGER,
    IN genre_name VARCHAR(32),
    OUT status VARCHAR(1000)
)
BEGIN
    DECLARE movie_exists INT;
    DECLARE star_exists_in_movie INT;
    DECLARE max_movie_id INT;
    DECLARE new_movie_id VARCHAR(10);
    DECLARE movie_id VARCHAR(10);
    DECLARE max_star_id INT;
    DECLARE new_star_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE max_genre_id INT;
    DECLARE new_genre_id INT;
    DECLARE genre_exists_in_movie INT;
    DECLARE genre_id INT;
    DECLARE new_price INT;

    -- Check if the movie already exists based on (title, year, director)
    SELECT COUNT(*) INTO movie_exists FROM movies m WHERE m.title = title AND m.year = year AND m.director = director;

    -- If the movie already exists, check if the star is already in the movie
    IF movie_exists > 0 THEN

        -- Check if the star exists in the stars table
        SELECT id INTO star_id FROM stars WHERE name = star_name AND (birthYear = star_birth_year OR star_birth_year IS NULL) LIMIT 1;

        IF star_id IS NULL THEN
            -- No star with the same name and birth year exists. Create a new star into the stars table
            SELECT MAX(CONVERT(SUBSTRING(id, 3), UNSIGNED)) INTO max_star_id FROM stars WHERE id LIKE 'nm%';
            SET max_star_id = max_star_id + 1;
            SET new_star_id = CONCAT('nm', LPAD(max_star_id, 7, '0'));
            IF star_birth_year IS NULL THEN
                INSERT INTO stars (id, name) VALUES (new_star_id, star_name);
            ELSE
                INSERT INTO stars (id, name, birthYear) VALUES (new_star_id, star_name, star_birth_year);
            END IF;
            SET star_id = new_star_id;
        END IF;
        -- Star is either now created or existing.

        SELECT m.id INTO movie_id FROM movies m WHERE m.title = title AND m.year = year AND m.director = director LIMIT 1;

        SELECT COUNT(*) INTO star_exists_in_movie
            FROM stars_in_movies s
            JOIN movies mv ON s.movieId = mv.id
            JOIN stars st ON s.starId = st.id
            WHERE st.name = star_name
            AND (st.birthYear = star_birth_year OR star_birth_year IS NULL OR st.birthYear IS NULL)
            AND mv.title = title
            AND mv.year = year
            AND mv.director = director;

        IF star_exists_in_movie > 0 THEN
            SET status = 'Error: This star already exists in the movie.';
        ELSE
        -- This movie already exists, but we want to insert a new star

            -- Insert the star into the stars_in_movies table
            INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);
            SET status = CONCAT('Success! Star ID: ', star_id, ' added to existing movie.');

        END IF;

        SELECT id INTO genre_id FROM genres WHERE name = genre_name LIMIT 1;
        IF genre_id IS NULL THEN
            SELECT MAX(id) INTO max_genre_id FROM genres;
            SET new_genre_id = max_genre_id + 1;
            INSERT INTO genres (id, name) VALUES (new_genre_id, genre_name);
            SET genre_id = new_genre_id;
        END IF;

        SELECT COUNT(*) INTO genre_exists_in_movie FROM genres_in_movies gim WHERE gim.genreId = genre_id AND gim.movieId = movie_id;

        IF genre_exists_in_movie = 0 THEN
            INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);
            SET status = CONCAT(status, ' New genre ID: ', genre_id, ' associated with the movie.');
        ELSE
            SET status = CONCAT(status, ' Genre already associated with the movie.');
        END IF;

    ELSE
    -- This movie does not exist, and need to insert the star into this movie

        -- Get the maximum numeric ID from the movies table
        SELECT MAX(CONVERT(SUBSTRING(id, 3), UNSIGNED)) INTO max_movie_id FROM movies WHERE id LIKE 'tt%';
        -- Increment the maximum numeric ID
        SET max_movie_id = max_movie_id + 1;
        -- Generate the new movie ID
        SET new_movie_id = CONCAT('tt', LPAD(max_movie_id, 7, '0'));
        -- Insert the new movie record
        INSERT INTO movies (id, title, year, director) VALUES (new_movie_id, title, year, director);

        -- Check if the star exists, and insert it if not
        SELECT id INTO star_id FROM stars WHERE name = star_name LIMIT 1;

        IF star_id IS NULL THEN
            SELECT MAX(CONVERT(SUBSTRING(id, 3), UNSIGNED)) INTO max_star_id FROM stars WHERE id LIKE 'nm%';
            SET max_star_id = max_star_id + 1;
            SET new_star_id = CONCAT('nm', LPAD(max_star_id, 7, '0'));
            IF star_birth_year IS NULL THEN
                INSERT INTO stars (id, name) VALUES (new_star_id, star_name);
            ELSE
                INSERT INTO stars (id, name, birthYear) VALUES (new_star_id, star_name, star_birth_year);
            END IF;
            SET star_id = new_star_id;
        END IF;

        -- Insert the star for the movie
        INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, new_movie_id);
        -- Check if the genre exists, and insert it if not
        SELECT id INTO genre_id FROM genres WHERE name = genre_name LIMIT 1;

        IF genre_id IS NULL THEN
            SELECT MAX(id) INTO max_genre_id FROM genres;
            SET new_genre_id = max_genre_id + 1;
            INSERT INTO genres (id, name) VALUES (new_genre_id, genre_name);
            SET genre_id = new_genre_id;
        END IF;

        -- Insert the genre for the movie
        INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, new_movie_id);

        SET new_price = FLOOR(1 + RAND() * 100);
        INSERT INTO movie_prices (movieId, price) VALUES (new_movie_id, new_price);

        SET status = CONCAT('Success! Movie ID: ', new_movie_id, ', Star ID: ', star_id, ', Genre ID: ', genre_id, ', Price: ', new_price);

    END IF;

END$$

CREATE PROCEDURE add_star (
    IN star_name VARCHAR(100),
    IN star_birth_year INTEGER,
    OUT status VARCHAR(1000)
)
BEGIN
    DECLARE star_exists INT;
    DECLARE max_star_id INT;
    DECLARE new_star_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);

    -- Check if the star already exists based on name and birth year
    SELECT COUNT(*) INTO star_exists FROM stars s WHERE s.name = star_name AND s.birthYear = star_birth_year;

    IF star_exists > 0 THEN
        SET status = 'Error: This star already exists.';
    ELSE
        SELECT MAX(CONVERT(SUBSTRING(id, 3), UNSIGNED)) INTO max_star_id FROM stars WHERE id LIKE 'nm%';
        SET max_star_id = max_star_id + 1;
        SET new_star_id = CONCAT('nm', LPAD(max_star_id, 7, '0'));
        INSERT INTO stars (id, name, birthYear) VALUES (new_star_id, star_name, star_birth_year);
        SET star_id = new_star_id;

        SET STATUS = CONCAT('Success! Star ID: ', star_id);
    END IF;

END $$

DELIMITER ;