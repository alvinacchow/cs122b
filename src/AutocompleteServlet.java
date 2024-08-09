import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpSession;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

//Declare WebServlet
@WebServlet(name = "AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        System.out.println("autocomplete servlet reached");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            HttpSession session = request.getSession();

            String title = request.getParameter("query") != null ?
                    request.getParameter("query") : "";

            String query = "SELECT id, title FROM movies WHERE match(title) against (? IN BOOLEAN MODE) LIMIT 10";

            PreparedStatement statement = connection.prepareStatement(query);
            String new_title = "";
            if (title != null && !title.trim().isEmpty())
            {
                String [] tokens = title.split(" ");
                for (String word : tokens)
                {
                    new_title += "+" + word + "* ";
                }
            }
            statement.setString(1, new_title);

            System.out.println("autocomplete query: " + statement.toString());

            ResultSet resultSet = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (resultSet.next()) {
                String movie_id = resultSet.getString("id");
                String movie_title = resultSet.getString("title");

                jsonArray.add(generateJsonObject(movie_id, movie_title));
            }

            out.write(jsonArray.toString());
            session.setAttribute("result", jsonArray.toString());
            response.setStatus(200);

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            System.out.println("autocomplete servlet error");

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally {
            out.close();
        }
    }

    private static JsonObject generateJsonObject(String id, String title) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", title);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("id", id);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }
}