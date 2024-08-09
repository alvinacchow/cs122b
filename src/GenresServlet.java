import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called GenresServlet, which maps to url "/api/genre-movies"
@WebServlet(name = "GenresServlet", urlPatterns = "/api/genre-movies")
public class GenresServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            String genreId = request.getParameter("genre_id");
            String sort = request.getParameter("sort");
            String limit = request.getParameter("limit");
            String offset = request.getParameter("offset");

            String query =
                            "SELECT m.id, m.title, m.year, m.director, COALESCE(r.rating, 0) as rating " +
                            "FROM movies m " +
                            "LEFT JOIN ratings r ON m.id = r.movieId " +
                            "JOIN genres_in_movies gm ON m.id = gm.movieId " +
                            "JOIN genres g ON gm.genreId = g.id " +
                            "WHERE g.id = ? ";

            String sortByColumn1 = "m.title";
            String sortOrder1 = "ASC";
            String sortByColumn2 = "r.rating";
            String sortOrder2 = "DESC";

            if (sort != null && !sort.isEmpty()) {
                String[] sortBy = sort.split(" ");
                if ("rating".equals(sortBy[0])) {
                    sortByColumn1 = "r.rating";
                    sortByColumn2 = "m.title";
                }
                if ("asc".equals(sortBy[1])) {
                    sortOrder1 = "ASC";
                } else {
                    sortOrder1 = "DESC";
                }
                if ("asc".equals(sortBy[3])) {
                    sortOrder2 = "ASC";
                } else {
                    sortOrder2 = "DESC";
                }
            }

            query += "ORDER BY " + sortByColumn1 + " " + sortOrder1 + ", " + sortByColumn2 + " " + sortOrder2 + " ";

            if (limit != null) {
                query += "LIMIT " + limit + " ";
            } else {
                query += "LIMIT 10 ";
            }

            if (offset != null) {
                query += "OFFSET " + offset;
            } else {
                query += "OFFSET 0";
            }

            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, genreId);
            ResultSet rs = preparedStatement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }

            for (int i = 0; i < jsonArray.size(); ++i) {
                JsonObject movieObject = jsonArray.get(i).getAsJsonObject();
                String movieId = movieObject.get("movie_id").getAsString();

                // Call method to get additional data based on movie ID
                JsonObject movie_stars = Shared.getStarsInMovie(movieId, 3, conn, response, out);
                JsonObject movie_genres = Shared.getGenresInMovie(movieId, 3, conn, response, out);

                // Add the additional data to the movie object
                movieObject.add("movie_stars", movie_stars);
                movieObject.add("movie_genres", movie_genres);
            }
            rs.close();
            preparedStatement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);


        } catch(Exception e){

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally{
            out.close();
        }

    }
}
