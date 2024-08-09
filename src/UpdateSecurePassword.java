
import java.sql.*;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;


public class UpdateSecurePassword {

    /*
     * 
     * This program updates your existing moviedb customers table to change the
     * plain text passwords to encrypted passwords.
     * 
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     * 
     */
    public static void main(String[] args) throws Exception {

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        updateCustomers(connection);
        updateEmployees(connection);
    }

    private static void updateCustomers(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        // change the customers table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = "ALTER TABLE customers MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering customers table schema completed, " + alterResult + " rows affected");

        // get the ID and password for each customer
        String query = "SELECT id, password from customers";

        ResultSet rs = statement.executeQuery(query);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<String> updateQueryList = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        while (rs.next()) {
            // get the ID and plain text password from current table
            String id = rs.getString("id");
            String password = rs.getString("password");

            // encrypt the password using StrongPasswordEncryptor
            String encryptedPassword = passwordEncryptor.encryptPassword(password);

            // generate the update query
            String updateQuery = String.format("UPDATE customers SET password='%s' WHERE id=%s;", encryptedPassword,
                    id);
            updateQueryList.add(updateQuery);
        }

        // execute the update queries to update the password
        System.out.println("updating customers password");
        int count = 0;
        for (String updateQuery : updateQueryList) {
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("updating customers password completed, " + count + " rows affected");
        System.out.println("finished customers");

    }

    private static void updateEmployees(Connection connection) throws SQLException {
        // change the employees table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
        try (Statement alterStatement = connection.createStatement()) {
            int alterResult = alterStatement.executeUpdate(alterQuery);
            System.out.println("altering employees table schema completed, " + alterResult + " rows affected");
        }

        // get the email and password for each employee
        String selectQuery = "SELECT email, password from employees";

        // we use the StrongPasswordEncryptor from jasypt library
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        // Prepare the update query
        String updateQuery = "UPDATE employees SET password=? WHERE email=?";

        // Use try-with-resources to ensure that the PreparedStatement and ResultSet are closed
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             Statement selectStatement = connection.createStatement();
             ResultSet rs = selectStatement.executeQuery(selectQuery)) {

            System.out.println("encrypting employees password (this might take a while)");

            int count = 0;
            while (rs.next()) {
                // get the email and plain text password from current table
                String email = rs.getString("email");
                String password = rs.getString("password");

                // encrypt the password using StrongPasswordEncryptor
                String encryptedPassword = passwordEncryptor.encryptPassword(password);

                // Set the parameters for the PreparedStatement and execute the update
                updateStatement.setString(1, encryptedPassword);
                updateStatement.setString(2, email);
                count += updateStatement.executeUpdate();
            }

            System.out.println("updating employees password completed, " + count + " rows affected");
        }

        System.out.println("finished employees");
    }
}
