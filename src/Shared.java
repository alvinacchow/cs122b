import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.sql.*;

public class Shared {

    public static JsonObject getStarsInMovie(String movieId, int limit, Connection conn, HttpServletResponse response, PrintWriter out) throws SQLException {
        JsonObject starsInMovie = new JsonObject();

        try {
            if (conn != null && !conn.isClosed()) {
                String query;
                PreparedStatement statement;

                query = "SELECT s.id as starId, s.name as starName, COUNT(sm.movieId) as movieCount "
                        + "FROM stars s "
                        + "JOIN stars_in_movies sm ON s.id = sm.starId "
                        + "JOIN (SELECT starId FROM stars_in_movies WHERE movieId = ?) as movie_stars ON s.id = movie_stars.starId "
                        + "GROUP BY s.id "
                        + "ORDER BY movieCount DESC, s.name ASC";

                // Initialize the PreparedStatement with the query
                statement = conn.prepareStatement(query);
                statement.setString(1, movieId);

                // If limit is greater than 0, add a LIMIT clause and set the limit parameter
                if (limit > 0) {
                    query += " LIMIT ?";
                    statement = conn.prepareStatement(query); // Prepare the statement again with the new query
                    statement.setString(1, movieId); // Set the first parameter again since we're preparing the statement again
                    statement.setInt(2, limit); // Set the second parameter for the LIMIT
                }

                ResultSet rs = statement.executeQuery();
                JsonArray starsArray = new JsonArray();

                // Iterate through the result set and add stars to the stars array
                while (rs.next()) {
                    String starName = rs.getString("starName");
                    String starId = rs.getString("starId");
                    starsArray.add(starName);
                    starsArray.add(starId);
                }

                // Add stars array to the starsInMovie JsonObject
                starsInMovie.add("stars", starsArray);

                rs.close();
            }
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }

        return starsInMovie;
    }

    public static JsonObject getGenresInMovie(String movieId, int limit, Connection conn, HttpServletResponse response, PrintWriter out) throws SQLException {
        JsonObject genresInMovie = new JsonObject();

        try {
            if (conn != null && !conn.isClosed()) {
                String query;
                PreparedStatement statement;
                if (limit < 0) {
                    query = "SELECT g.name, g.id FROM genres g JOIN genres_in_movies gm ON g.id = gm.genreId WHERE gm.movieId = ? ORDER BY g.name";
                    statement = conn.prepareStatement(query);
                    statement.setString(1, movieId);
                } else {
                    query = "SELECT g.name, g.id FROM genres g JOIN genres_in_movies gm ON g.id = gm.genreId WHERE gm.movieId = ? ORDER BY g.name LIMIT ?";
                    statement = conn.prepareStatement(query);
                    statement.setString(1, movieId);
                    statement.setInt(2, limit);
                }

                ResultSet rs = statement.executeQuery();

                JsonArray genresArray = new JsonArray();
                // Iterate through the result set and add stars to the stars array
                while (rs.next()) {
                    String genreName = rs.getString("name");
                    String genreId = rs.getString("id");
                    genresArray.add(genreName);
                    genresArray.add(genreId);
                }

                // Add stars array to the starsInMovie JsonObject
                genresInMovie.add("genres", genresArray);

                rs.close();
            }
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }

        return genresInMovie;
    }
}