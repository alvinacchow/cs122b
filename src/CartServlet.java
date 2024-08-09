import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;


// Declaring a WebServlet called CartServlet, which maps to url "/api/cart"
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
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
        // Set the response type to JSON

        response.setContentType("application/json"); // Response mime type
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Retrieve the current session
        HttpSession session = request.getSession(true);

        // Retrieve the shopping cart from the session
        Map<String, Integer> shoppingCart = (Map<String, Integer>) session.getAttribute("shoppingCart");
        if (shoppingCart == null) {
            shoppingCart = new LinkedHashMap<>();
            session.setAttribute("shoppingCart", shoppingCart);
        }

        // Prepare the JSON response array
        JsonArray cartItems = new JsonArray();

        // For each item in the shopping cart, retrieve the movie details
        try (Connection connection = dataSource.getConnection()) {
            for (String movieId : shoppingCart.keySet()) {
                String query = "SELECT m.title, mp.price FROM movies m, movie_prices mp WHERE m.id = mp.movieId AND m.id = ?";

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, movieId);

                    try (ResultSet resultSet = statement.executeQuery()) {

                        while (resultSet.next()) {
                            String title = resultSet.getString("title");
                            int price = resultSet.getInt("price");
                            int quantity = shoppingCart.get(movieId);

                            // Create a JSON object for each movie and add it to the JSON array
                            JsonObject movieDetails = new JsonObject();
                            movieDetails.addProperty("movieId", movieId);
                            movieDetails.addProperty("title", title);
                            movieDetails.addProperty("price", price);
                            movieDetails.addProperty("quantity", quantity);
                            cartItems.add(movieDetails);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
            // Prepare an error response if there's an issue
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Failed to retrieve the cart.");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        // Write JSON string to output
        out.write(cartItems.toString());
        // Set response status to 200 (OK)
        response.setStatus(200);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movieId = request.getParameter("movieId");
        String action = request.getParameter("action"); // Get the action parameter from the request

        HttpSession session = request.getSession(true);
        Map<String, Integer> shoppingCart = (Map<String, Integer>) session.getAttribute("shoppingCart");

        if (shoppingCart == null) {
            shoppingCart = new LinkedHashMap<>();
            session.setAttribute("shoppingCart", shoppingCart);
        }

        JsonObject responseJsonObject = new JsonObject();

        try {
            // Check the action parameter to determine what action to perform
            if ("add".equals(action)) {
                // Add item logic
                shoppingCart.put(movieId, shoppingCart.getOrDefault(movieId, 0) + 1);
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "Movie added to cart successfully.");
            } else if ("delete".equals(action)) {
                // Delete item logic
                if (shoppingCart.containsKey(movieId)) {
                    shoppingCart.remove(movieId);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Movie removed from cart successfully.");
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Movie not found in cart.");
                }
            } else if ("update".equals(action)) {
                int newQuantity = Integer.parseInt(request.getParameter("quantity"));
                if (newQuantity >= 0) { // Updated condition to include 0
                    if (shoppingCart.containsKey(movieId)) {
                        if (newQuantity > 0) {
                            // Update the quantity in the cart
                            shoppingCart.put(movieId, newQuantity);
                            responseJsonObject.addProperty("status", "success");
                            responseJsonObject.addProperty("message", "Quantity updated successfully.");
                        } else {
                            // If the new quantity is 0 or less, remove the movie from the cart
                            shoppingCart.remove(movieId);
                            responseJsonObject.addProperty("status", "success");
                            responseJsonObject.addProperty("message", "Movie removed from cart.");
                        }
                    } else {
                        // If the movieId doesn't exist in the cart, send an error message
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", "Movie not found in cart.");
                    }
                } else {
                    // Handle invalid quantity
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Invalid quantity.");
                }
            } else {
                // Handle other actions or bad requests
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid action.");
            }
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "An error occurred: " + e.getMessage());
        }

        response.setContentType("application/json"); // Ensure response type is set to JSON
        response.setCharacterEncoding("UTF-8"); // Set encoding to UTF-8
        PrintWriter out = response.getWriter();
        out.write(responseJsonObject.toString());
    }


}

