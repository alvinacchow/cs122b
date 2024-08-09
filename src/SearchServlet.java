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


// Declaring a WebServlet called SearchServlet, which maps to url "/api/search"
@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
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
        // Retrieve form data
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String sort = request.getParameter("sort");
        String limit = request.getParameter("limit");
        String offset = request.getParameter("offset");

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating " +
                        "FROM movies m " +
                        "LEFT JOIN ratings r ON m.id = r.movieId "
        );

        if (star != null && !star.trim().isEmpty()) {
            queryBuilder.append("JOIN stars_in_movies sm ON m.id = sm.movieId ")
                    .append("JOIN stars s ON s.id = sm.starId ");
        }

        queryBuilder.append("WHERE true");

        String new_title = "";
        // Append conditions based on form input
        if (title != null && !title.trim().isEmpty()) {
            String [] tokens = title.split(" ");
            for (String word : tokens) {
                new_title += "+" + word + "* ";
            }
            queryBuilder.append(" AND MATCH (m.title) AGAINST (? IN BOOLEAN MODE) ");
        } else {
            new_title = "%";
            queryBuilder.append(" AND m.title LIKE ? ");
        }

        if (year != null && !year.trim().isEmpty()) {
            queryBuilder.append(" AND m.year = ?");
        }
        if (director != null && !director.trim().isEmpty()) {
            queryBuilder.append(" AND m.director LIKE ?");
        }
        if (star != null && !star.trim().isEmpty()) {
            queryBuilder.append(" AND s.name LIKE ?");
        }

        String[] sortBy;
        if (sort != null) {
            sortBy = sort.split(" ");
        } else {
            // Handle the case when 'sort' parameter is null
            // For example, you might want to provide a default sorting mechanism
            sortBy = new String[]{"title", "asc", "rating", "desc"};
        }

        if ("rating".equals(sortBy[0])) {
            queryBuilder.append(" ORDER BY r.rating");
        } else {
            queryBuilder.append(" ORDER BY m.title");
        }

        if ("asc".equals(sortBy[1])) {
            queryBuilder.append(" ASC,");
        } else {
            queryBuilder.append(" DESC,");
        }

        if ("title".equals(sortBy[2])) {
            queryBuilder.append(" m.title");
        } else {
            queryBuilder.append(" r.rating");
        }

        if ("asc".equals(sortBy[3])) {
            queryBuilder.append(" ASC");
        } else {
            queryBuilder.append(" DESC");
        }

        if (limit != null) {
            queryBuilder.append(" LIMIT ").append(limit);
        } else {
            queryBuilder.append(" LIMIT 10");
        }

        if (offset != null) {
            queryBuilder.append(" OFFSET ").append(offset);
        } else {
            queryBuilder.append(" OFFSET 0");
        }

        System.out.println("search query: " + queryBuilder.toString());

        response.setContentType("application/json"); // Response mime type
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(queryBuilder.toString());

            // Set parameters for the prepared statement to prevent SQL injection
            int paramIndex = 1;
            preparedStatement.setString(paramIndex++, new_title);
            if (year != null && !year.trim().isEmpty()) {
                preparedStatement.setInt(paramIndex++, Integer.parseInt(year));
            }
            if (director != null && !director.trim().isEmpty()) {
                preparedStatement.setString(paramIndex++, "%" + director + "%");
            }
            if (star != null && !star.trim().isEmpty()) {
                preparedStatement.setString(paramIndex++, "%" + star + "%");
            }
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