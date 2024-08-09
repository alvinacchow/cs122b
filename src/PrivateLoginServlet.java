import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@WebServlet(name = "PrivateLoginServlet", urlPatterns = {"/api/dashboard_login", "/_dashboard"})
public class PrivateLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 5L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check if the user session indicates that the user is already logged in
        HttpSession session = request.getSession(false); // don't create if it doesn't exist

        if (session != null && session.getAttribute("user") != null) {
            // User is logged in, redirect to the dashboard page
            Object userObject = session.getAttribute("user");
            if (userObject instanceof EmployeeUser) {
                response.sendRedirect("metadata.html");
            }
            else {
                // No user is logged in, redirect to the login page
                response.sendRedirect("private-login.html");
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try (Connection conn = dataSource.getConnection()) {
            String query = String.format("SELECT * FROM employees WHERE email='%s'", email);

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();
            if (rs.next()) {
                if (VerifyPassword.validPassword(email, password, "employee")) {
                    String employee_email = rs.getString("email");
                    String employee_password = rs.getString("password");
                    request.getSession().setAttribute("user", new EmployeeUser(employee_email, employee_password));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");

                }
                else {
                    // login fail because of invalid email
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
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);

        } finally {
            out.close();
        }
    }

}