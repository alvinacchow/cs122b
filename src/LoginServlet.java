import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 5L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("start doPost");
        PrintWriter out = response.getWriter();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try (Connection conn = dataSource.getConnection()) {
            String query = String.format("SELECT * FROM customers WHERE email='%s'", email);

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();
            if (rs.next()) {
                String id = rs.getString("id");
                String customer_email = rs.getString("email");
                String customer_password = rs.getString("password");

                if (password.equals(customer_password)) {
                    request.getSession().setAttribute("user", new User(id, customer_email, customer_password));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                } else {
                    // login fail because of invalid password
                    responseJsonObject.addProperty("status", "fail");
                    // log to localhost log
                    request.getServletContext().log("Login failed");
                    responseJsonObject.addProperty("message", "The password entered is incorrect. Please try again.");
                }
            } else {
                // login fail because of invalid email
                responseJsonObject.addProperty("status", "fail");
                // log to localhost log
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", "An account with the email address, " + email + ", does not exist. Please try again.");
            }

            rs.close();
            statement.close();
            out.write(responseJsonObject.toString());
            System.out.println("1\n" + responseJsonObject.toString());

            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            System.out.println("2\n" + jsonObject.toString());
            System.out.println("3\n" + e.getMessage());
            e.printStackTrace();

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);

        } finally {
            out.close();
        }
    }
}