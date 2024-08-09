import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;


@WebServlet(name = "AddRecordServlet", urlPatterns = "/api/add-record")
public class AddRecordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadWrite");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String actionType = request.getParameter("actionType");
        if ("addMovie".equals(actionType)) {
            addMovie(request, response);
        } else if ("addStar".equals(actionType)) {
            addStar(request, response);
        } else {
            // Handle unknown actionType
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action type.");
        }
    }


    private void addMovie(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        int year = Integer.parseInt(request.getParameter("year"));
        String director = request.getParameter("director");
        String starName = request.getParameter("star");
        String genreName = request.getParameter("genre");
        String starBirthYearParam = request.getParameter("starBirthYear");
        Integer starBirthYear = null;

        // Check if starBirthYearParam is not null and not empty
        if (starBirthYearParam != null && !starBirthYearParam.isEmpty()) {
            starBirthYear = Integer.parseInt(starBirthYearParam);
        }

        try (Connection conn = dataSource.getConnection()) {
            String sql = "{CALL add_movie(?, ?, ?, ?, ?, ?, ?)}";
            CallableStatement stmt = conn.prepareCall(sql);

            stmt.setString(1, title);
            stmt.setInt(2, year);
            stmt.setString(3, director);
            stmt.setString(4, starName);

            // Set starBirthYear or "N/A" if it's null
            if (starBirthYear != null) {
                stmt.setInt(5, starBirthYear);
            } else {
                stmt.setNull(5, Types.INTEGER); // Assuming the parameter type is INTEGER
            }

            stmt.setString(6, genreName);
            stmt.registerOutParameter(7, Types.VARCHAR);

            stmt.execute();
            String status = stmt.getString(7);

            // In the servlet addMovie method
            if (status != null) {
                response.getWriter().write(status);
            }
            else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to add movie");
                response.setStatus(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to add movie");
            response.setStatus(500);
        }
    }

    private void addStar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String starBirthYearParam = request.getParameter("year");
        Integer starBirthYear = null;

        // Check if starBirthYearParam is not null and not empty
        if (starBirthYearParam != null && !starBirthYearParam.isEmpty()) {
            starBirthYear = Integer.parseInt(starBirthYearParam);
        }

        try (Connection conn = dataSource.getConnection()) {
            String sql = "{CALL add_star(?, ?, ?)}";
            CallableStatement stmt = conn.prepareCall(sql);

            stmt.setString(1, name);

            // Set starBirthYear or "N/A" if it's null
            if (starBirthYear != null) {
                stmt.setInt(2, starBirthYear);
            } else {
                stmt.setNull(2, Types.INTEGER); // Assuming the parameter type is INTEGER
            }

            stmt.registerOutParameter(3, Types.VARCHAR);

            stmt.execute();
            String status = stmt.getString(3);

            // In the servlet addMovie method
            if (status != null) {
                response.getWriter().write(status);
            }
            else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to add movie");
                response.setStatus(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to add movie");
            response.setStatus(500);
        }
    }
}
