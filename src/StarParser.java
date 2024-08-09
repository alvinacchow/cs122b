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

public class StarParser extends DefaultHandler {
    List<Star> stars;

    private String tempVal;
    private Star tempStar;
    private String tempMovie;

    private HashMap<String, String> starIds;
    private HashMap<String, String> moviesInDB;
    private HashMap<String, List<String>> movieStar;

    public StarParser() {
        stars = new ArrayList<>();
        starIds = new HashMap<>();
        moviesInDB = new HashMap<>();
        movieStar = new HashMap<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false", "mytestuser", "My6$Password");
            if (conn != null) {
                // maps star names (key) to star ids (value)
                mapStarIds(conn);
                // maps movie objects (key) to movie ids (value)
                mapMoviesInDB(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mapStarIds(Connection conn) {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT name, id FROM stars");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                starIds.put(rs.getString("name"), rs.getString("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void mapMoviesInDB(Connection conn) {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT title, id FROM movies");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                moviesInDB.put(rs.getString("title"), rs.getString("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void mapMovieStars(Connection conn) {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT m.id AS movie_id FROM movies m LEFT JOIN stars_in_movies sm ON m.id = sm.movieId WHERE sm.movieId IS NULL");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String movieId = rs.getString("movie_id");
                // add a null value for movie without stars
                movieStar.put(movieId, null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        parseDocument();
        writeToFile();


        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&allowPublicKeyRetrieval=true&allowLoadLocalInfile=true&useSSL=false", "mytestuser", "My6$Password");
            if (conn != null) {
                Statement statement = conn.createStatement();

                // loads new stars in movies entries into database
                String starsFilePath = "src/stars.txt";
                String loadStarsStatement = "LOAD DATA LOCAL INFILE '" + starsFilePath + "' INTO TABLE stars_in_movies fields terminated by '|' lines terminated by '\\n'";
                statement.execute(loadStarsStatement);

                mapMovieStars(conn);
                deleteInconsistencies();

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
            sp.parse("../casts124.xml", this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void printData() {
        System.out.println("Number of Stars '" + stars.size() + "'.");

//        Iterator<Star> it = stars.iterator();
//        while (it.hasNext()) {
//            System.out.println(it.next().toString());
//        }
    }

    private void writeToFile() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("src/stars.txt", false));
            PrintWriter inconsistentWriter = new PrintWriter(new FileWriter("inconsistencies.txt", true));
            inconsistentWriter.printf("Inconsistencies of 'Stars_in_Movies' Table%n");

            Iterator<Star> it = stars.iterator();
            while (it.hasNext()) {
                Star s = it.next();
                if (moviesInDB.containsKey(s.getMovie())) {
                    s.addMovie(moviesInDB.get(s.getMovie()));
                } else {
                    inconsistentWriter.printf("\t — Star '%s' could not be added to movie '%s' since the movie could not be found.%n", s.getName(), s.getMovie());
                    continue;
                }

                if (!starIds.containsKey(s.getName())) {
                    inconsistentWriter.printf("\t — Star '%s' could not be added to movie '%s' since the star could not be found.%n", s.getName(), s.getMovie());
                    continue;
                } else {
                    s.setId(starIds.get(s.getName()));
                }
                writer.printf("%s|%s%n", s.getId(), s.getMovie());
            }
            inconsistentWriter.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteInconsistencies() {
        try {
            int batchSize = 1000;
            int processedCount = 0;
            List<String> batchMovieIds = new ArrayList<>();

            Iterator<String> iterator = movieStar.keySet().iterator();
            while (iterator.hasNext()) {
                String movieId = iterator.next();
                batchMovieIds.add(movieId);
                processedCount++;

                // execute batch deletion after processing batchSize entries
                if (processedCount % batchSize == 0) {
                    deleteBatchFromDatabase(batchMovieIds);
                    batchMovieIds.clear();
                }
                iterator.remove();
            }

            // delete remaining entries in the last batch
            if (!batchMovieIds.isEmpty()) {
                deleteBatchFromDatabase(batchMovieIds);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteBatchFromDatabase(List<String> movieIds) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false", "mytestuser", "My6$Password");
            if (conn != null) {
                Statement statement = conn.createStatement();

                // delete from genres_in_movies table
                String deleteGenresStatement = "DELETE FROM genres_in_movies WHERE movieId IN ('" + String.join("','", movieIds) + "')";
                statement.executeUpdate(deleteGenresStatement);

                // delete from movie_prices table
                String deletePricesStatement = "DELETE FROM movie_prices WHERE movieId IN ('" + String.join("','", movieIds) + "')";
                statement.executeUpdate(deletePricesStatement);

                // delete from ratings table
                String deleteRatingsStatement = "DELETE FROM ratings WHERE movieId IN ('" + String.join("','", movieIds) + "')";
                statement.executeUpdate(deleteRatingsStatement);

                // delete from movies table
                String deleteMoviesStatement = "DELETE FROM movies WHERE id IN ('" + String.join("','", movieIds) + "')";
                statement.executeUpdate(deleteMoviesStatement);

                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("m")) {
            // create a new instance of employee
            tempStar = new Star();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("m")) {
            tempStar.addMovie(tempMovie);
            stars.add(tempStar);
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie = tempVal;
        } else if (qName.equalsIgnoreCase("a")) {
            tempStar.setName(tempVal);
        }
    }

    public static void main(String[] args) {
        StarParser spe = new StarParser();
        spe.run();
    }
}
