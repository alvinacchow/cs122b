import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadWrite");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        String id = request.getParameter("card_number");
        String firstName = request.getParameter("first_name");
        String lastName = request.getParameter("last_name");
        String expiration = request.getParameter("expiration");

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            java.util.Date date = formatter.parse(expiration);
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());

            try (Connection conn = dataSource.getConnection()) {
                String query = "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?";

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, id);
                statement.setString(2, firstName);
                statement.setString(3, lastName);
                statement.setDate(4, sqlDate);

                ResultSet rs = statement.executeQuery();

                JsonObject responseJsonObject = new JsonObject();

                if (rs.next()) {
                    HttpSession session = request.getSession();
                    Map<String, Integer> shoppingCart = (Map<String, Integer>) session.getAttribute("shoppingCart");
                    if (shoppingCart == null || shoppingCart.isEmpty()) {
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", "Your cart is empty.");
                        out.write(responseJsonObject.toString());

                        response.setStatus(200);
                    } else {
                        String user_id = ((User) session.getAttribute("user")).getId();
                        JsonArray jsonArray = insertSalesAndGetDetails(conn, user_id, shoppingCart);

                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");
                        responseJsonObject.add("orderArray", jsonArray);

                        out.write(responseJsonObject.toString());
                        response.setStatus(200);
                    }
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "A credit card with the information provided does not exist. Please try again.");
                    out.write(responseJsonObject.toString());
                    response.setStatus(200);
                }
            } catch (Exception e) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", e.getMessage());
                out.write(jsonObject.toString());
                response.setStatus(500);
            }
        } catch (ParseException e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    private JsonArray insertSalesAndGetDetails(Connection conn, String userId, Map<String, Integer> shoppingCart) throws SQLException {
        JsonArray jsonArray = new JsonArray();

        String query = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES(?, ?, CURDATE(), ?);";
        try (PreparedStatement insertStatement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) { // Add Statement.RETURN_GENERATED_KEYS
            for (String movieId : shoppingCart.keySet()) {
                int quantity = shoppingCart.get(movieId);
                insertStatement.setString(1, userId);
                insertStatement.setString(2, movieId);
                insertStatement.setInt(3, quantity);
                insertStatement.executeUpdate();

                // Retrieve the generated keys (sales IDs)
                ResultSet generatedKeys = insertStatement.getGeneratedKeys();

                // Process each generated key
                while (generatedKeys.next()) {
                    int saleId = generatedKeys.getInt(1); // Assuming the ID column is auto-generated
                    // Retrieve sales details for the current sale ID and add to the JSON array
                    System.out.println("saleid " + saleId);
                    System.out.println("getSalesDetails " + getSalesDetails(conn, saleId));
                    jsonArray.addAll(getSalesDetails(conn, saleId));
                }
            }
                return jsonArray;
        }
    }

    private JsonArray getSalesDetails(Connection conn, int saleId) throws SQLException {
        String query = "SELECT * FROM sales WHERE id = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, saleId);
            ResultSet resultSet = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (resultSet.next()) {
                String customerId = resultSet.getString("customerId");
                String saleDate = resultSet.getString("saleDate");
                String movieId = resultSet.getString("movieId");
                int quantity = resultSet.getInt("quantity");

                // Create a JsonObject for the sales information
                JsonObject saleObject = new JsonObject();
                saleObject.addProperty("sale_id", saleId);
                saleObject.addProperty("customer_id", customerId);
                saleObject.addProperty("sale_date", saleDate);
                saleObject.addProperty("movie_id", movieId);
                saleObject.addProperty("movie_quantity", quantity);

                // Retrieve movie details
                String movieQuery = "SELECT m.title, mp.price FROM movies m, movie_prices mp WHERE m.id = mp.movieId AND m.id = ?";
                try (PreparedStatement movieStatement = conn.prepareStatement(movieQuery)) {
                    movieStatement.setString(1, movieId);
                    ResultSet rs = movieStatement.executeQuery();

                    // Create a JsonArray to hold movie details
                    JsonArray movieArray = new JsonArray();

                    while (rs.next()) {
                        String title = rs.getString("title");
                        int price = rs.getInt("price");

                        // Create a JsonObject for each movie
                        JsonObject movieObject = new JsonObject();
                        movieObject.addProperty("movie_title", title);
                        movieObject.addProperty("price", price);
                        movieArray.add(movieObject); // Add movie details to the movieArray
                    }

                    // Close the result set for movie details
                    rs.close();

                    // Add the movie details array to the saleObject
                    saleObject.add("movie_details", movieArray);
                }

                // Add the saleObject to the jsonArray
                jsonArray.add(saleObject);
            }

            // Close the result set for sales
            resultSet.close();

            return jsonArray;
        }
    }
}