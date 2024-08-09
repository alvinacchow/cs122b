import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.io.FileWriter;
import java.io.PrintWriter;

public class MovieParser extends DefaultHandler {
    List<Movie> movies;

    private String tempVal;
    private Movie tempMovie;
    private String tempDirector;
    private Genre tempGenre;

    private Integer movieId;
    private Integer genreId;

    private HashMap<String,Integer> genreIds;
    private HashMap<Movie,String> moviesInDB;

    public MovieParser() {
        movies = new ArrayList<Movie>();
        moviesInDB = new HashMap<>();
        genreIds = new HashMap<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false", "mytestuser", "My6$Password");
            if (conn != null) {
                // maps genre names (key) to genre ids (value)
                mapGenreIds(conn);
                // maps movie objects (key) to movie ids (value)
                mapMoviesInDB(conn);

                PreparedStatement preparedStatement = conn.prepareStatement("select max(substring(id, 3)) as id from movies");
                ResultSet rs = preparedStatement.executeQuery();
                rs.next();
                movieId = Integer.parseInt(rs.getString("id"));

                preparedStatement = conn.prepareStatement("select max(id) as id from genres");
                rs = preparedStatement.executeQuery();
                rs.next();
                genreId = Integer.parseInt(rs.getString("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mapGenreIds(Connection conn) {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM genres");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                genreIds.put(rs.getString("name"), rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void mapMoviesInDB(Connection conn) {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM movies");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie(rs.getString("id"), rs.getString("title"), rs.getInt("year"), rs.getString("director"));
                moviesInDB.put(movie, rs.getString("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        parseDocument();
        writeToMovieFile();
        writeToGenreFile();
        writeToGenreInMoviesFile();


        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&allowPublicKeyRetrieval=true&allowLoadLocalInfile=true&useSSL=false", "mytestuser", "My6$Password");
            if (conn != null) {
                Statement statement = conn.createStatement();

//                // loads new movies into database
                String movieFilePath = "src/movies.txt";
                String loadMovieStatement = "LOAD DATA LOCAL INFILE '" + movieFilePath + "' INTO TABLE movies fields terminated by '|' lines terminated by '\\n'";
                statement.execute(loadMovieStatement);

//                // load new movie prices and ratings into database
                String idsFilePath = "src/movie_ids.txt";
                String loadPricesStatement = "LOAD DATA LOCAL INFILE '" + idsFilePath + "' INTO TABLE movie_prices (movieId, price) SET price = 0";
                statement.execute(loadPricesStatement);
                String loadRatingsStatement = "LOAD DATA LOCAL INFILE '" + idsFilePath + "' INTO TABLE ratings (movieId, rating, numVotes) SET rating = 0.0, numVotes = 0";
                statement.execute(loadRatingsStatement);

                // loads new genres into database
                String genreFilePath = "src/genres.txt";
                String loadGenresStatement = "LOAD DATA LOCAL INFILE '" + genreFilePath + "' INTO TABLE genres fields terminated by '|' lines terminated by '\\n'";
                statement.execute(loadGenresStatement);

                // loads new entries of genres_in_movies into database
                String genresInMoviesFilePath = "src/genres_in_movies.txt";
                String loadGenresInMoviesStatement = "LOAD DATA LOCAL INFILE '" + genresInMoviesFilePath + "' INTO TABLE genres_in_movies fields terminated by '|' lines terminated by '\\n'";
                statement.execute(loadGenresInMoviesStatement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        printData();
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("../mains243.xml", this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void writeToMovieFile() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("src/movies.txt", false));
            PrintWriter movieWriter = new PrintWriter(new FileWriter("src/movie_ids.txt", false));
            PrintWriter inconsistentWriter = new PrintWriter(new FileWriter("inconsistencies.txt", false));
            inconsistentWriter.printf("Inconsistencies of 'Movies' Table%n");

            Iterator<Movie> it = movies.iterator();
            while (it.hasNext()) {
                Movie m = it.next();
                if (m.getTitle().isEmpty()) {
                    it.remove();
                    continue;
                }

                if (m.getGenres().isEmpty()) {
                    inconsistentWriter.printf("\t — Movie '%s' could not be added since it does not have a genre.%n", m.getTitle());
                    it.remove();
                    continue;
                }

                if (m.getDirector() == null) {
                    inconsistentWriter.printf("\t — Movie '%s' could not be added since it does not have a director.%n", m.getTitle());
                    it.remove();
                    continue;
                }

                if (moviesInDB.containsKey(m)) {
                    if (!moviesInDB.get(m).equals(m.getId())) {
                        inconsistentWriter.printf("\t — Movie '%s' could not be added since it already exists in the database.%n", m.getTitle());
                        m.setId(moviesInDB.get(m));
                        it.remove();
                        continue;
                    }
                }

                ++movieId;
                m.setId(String.format("tt%07d", movieId));
                writer.printf("%s|%s|%d|%s%n", m.getId(), m.getTitle(), m.getYear(), m.getDirector());
                movieWriter.printf("%s%n", m.getId());
            }

            inconsistentWriter.println();
            inconsistentWriter.close();
            writer.close();
            movieWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToGenreFile() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("src/genres.txt", false));
            PrintWriter inconsistentWriter = new PrintWriter(new FileWriter("inconsistencies.txt", true));
            inconsistentWriter.printf("Inconsistencies of 'Genre' Table%n");

            Set<Genre> genreSet = new HashSet<Genre>();
            Set<String> writtenInconsistentGenres = new HashSet<>();

            // checks for inconsistent genres
            Iterator<Movie> it = movies.iterator();
            while (it.hasNext()) {
                Movie m = it.next();
                Iterator<Genre> genreIterator = m.getGenres().iterator();
                while (genreIterator.hasNext()) {
                    Genre genre = genreIterator.next();
                    if (genre.getName().isEmpty()) {
                        continue;
                    }
                    if (genre.isInconsistent() && !writtenInconsistentGenres.contains(genre.getName())) {
                        inconsistentWriter.printf("\t — Genre '%s' could not be added since it has an invalid name.%n", genre.getName());
                        writtenInconsistentGenres.add(genre.getName());
                    } else {
                        genreSet.add(genre);
                    }
                }
            }

            // checks for duplicate genres
            Iterator<Genre> g = genreSet.iterator();
            while (g.hasNext()) {
                Genre genre = g.next();
                if (!genreIds.containsKey(genre.getName())) {
                    ++genreId;
                    genre.setId(genreId);
                    genreIds.put(genre.getName(), genre.getId());
                    writer.printf("%s|%s%n", genre.getId(), genre.getName());
                }
            }

            inconsistentWriter.println();
            inconsistentWriter.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToGenreInMoviesFile() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("src/genres_in_movies.txt", false));
            PrintWriter inconsistentWriter = new PrintWriter(new FileWriter("inconsistencies.txt", true));
            inconsistentWriter.printf("Inconsistencies of 'Genre_in_Movies' Table%n");

            Set<String> inconsistentGenres = new HashSet<>();
            Iterator<Movie> it = movies.iterator();
            while (it.hasNext()) {
                Movie m = it.next();

                Iterator<Genre> genreIterator = m.getGenres().iterator();
                while (genreIterator.hasNext()) {
                    Genre genre = genreIterator.next();
                    if (genre.getName().isEmpty()) {
                        continue;
                    }
                    genre.setId(genreIds.get(genre.getName()));
                    if (genre.getId() == null && !inconsistentGenres.contains(genre.getName().toLowerCase().strip())) {
                        inconsistentWriter.printf("\t — Genre '%s' could not be added since genreId it could not be found.%n", genre.getName().toLowerCase().strip());
                        inconsistentGenres.add(genre.getName().toLowerCase().strip());
                    } else {
                        writer.printf("%d|%s%n", genre.getId(), m.getId());
                    }
                }
            }

            inconsistentWriter.println();
            inconsistentWriter.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printData() {
        System.out.println("Number of Movies '" + movies.size() + "'.");

//        Iterator<Movie> it = movies.iterator();
//        while (it.hasNext()) {
//            System.out.println(it.next().toString());
//        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new Movie();
        }
        if (qName.equalsIgnoreCase("cat")) {
            tempGenre = new Genre();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("directorfilms")) {
            tempDirector = null;
        } else if (qName.equalsIgnoreCase("dirname")) {
            tempDirector = tempVal;
        } else if (qName.equalsIgnoreCase("film")) {
            tempMovie.setDirector(tempDirector);
            movies.add(tempMovie);
        } else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setxmlId(tempVal);
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                tempMovie.setYear(Integer.parseInt(tempVal));
            } catch (Exception e) {;}
        } else if (qName.equalsIgnoreCase("cat")) {
            tempGenre.setName(tempVal);
            tempMovie.addGenre(tempGenre);
        }
    }

//    public static void main(String[] args) {
//        MovieParser spe = new MovieParser();
//        spe.run();
//    }
}
