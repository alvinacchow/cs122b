# CS 122B Project
## Demonstration Video
- View our Project 1 demo video [here](https://youtu.be/TvtxoV1eB0g?si=cLpc9WOZtdkrfKxI)!
- View our Project 2 demo video [here](https://youtu.be/Nz1hB6cDofU)!
- View our Project 3 demo video [here](https://youtu.be/y5Mzh97RfAo)!
- View our Project 4 demo video [here](https://youtu.be/x8cMemdDHQE?si=DMM8HqwN2dELDZW_)!

## General
- Team Ube
- Names: Alvina Chow, Jenny Phan

## Group Member Contributions
### Project 1
#### Joint Efforts
- Writing SQL queries and creating tables for database
- Refining the front-end user interface
#### Alvina Chow
- Developed the Top 20 Movies and Single Star pages
- Established the overarching theme and styling for tables 
#### Jenny Phan
- Created the Single Movie page
- Designed the navigation bar

### Project 2
#### Joint Efforts
- Altered SQL tables to accommodate movie prices and sales quantity
#### Alvina Chow
- Created the Shopping Cart page
- Implemented searching and browsing functionalities
#### Jenny Phan
- Added pagination and sorting options
- Developed the Login and Payment Information pages

### Project 3
#### Alvina Chow
- Integrated reCAPTCHA verification
- Employed encrypted password
- Developed a dashboard using stored procedure
#### Jenny Phan
- Enabled HTTPS security
- Parsed XML files into database
#### Project 3 Miscellaneous
- Inconsistent data reports from parsing can be found in inconsistencies.txt.
- Parsing Optimization Strategies
  - Loaded data from text file into database using `LOAD DATA LOCAL INFILE`. 
    - Our method of loading our parsed data into a text file before inserting it into the database helped reduce overhead. This was more efficient than inserting each record individually.
  - In-memory hashtable to keep track of existence of records.
    - Constantly querying the database to check if a record exists can be slow and resource-intensive. Thus, we used a hashtable; not only did it allow us to minimize database queries, it also offers fast lookups and insertion operations, which improves our parser's performance.
- Filenames with Prepared Statements
  - AlphaServlet
  - CartServlet 
  - GenresServlet 
  - LoginServlet 
  - MainPageServlet 
  - MovieParser 
  - MovieServlet 
  - PaymentServlet 
  - PrivateLoginServlet 
  - SearchServlet 
  - Shared 
  - SingleMovieServlet 
  - SingleStarServlet 
  - StarParser 
  - StarsServlet
  - UpdateSecurePassword
 
### Project 4
#### Joint Efforts
- Scaled Fabflix using a MySQL and Tomcat cluster
#### Alvina Chow
- Enabled JDBC connection pooling
- Configured a MySQL cluster on AWS that includes master and slave instances
#### Jenny Phan
- Integrated fuzzy search functionality into the search feature
- Implemented full-text search on movie titles and introduced autocomplete suggestions
#### Project 4 Miscellaneous
##### Connection Pooling
- Filenames with Prepared Statements and JDBC Connection Pooling
  - WebContent/META-INF/context.xml
  - src/AlphaServlet.java
  - src/AutocompleteServlet.java
  - src/CartServlet.java 
  - src/GenresServlet.java
  - src/LoginServlet.java
  - src/MainPageServlet.java
  - src/MovieParser.java
  - src/MovieServlet.java
  - src/PaymentServlet.java
  - src/PrivateLoginServlet.java
  - src/SearchServlet.java
  - src/Shared.java
  - src/SingleMovieServlet.java
  - src/SingleStarServlet.java
  - src/StarParser.java
  - src/StarsServlet.java
  - src/UpdateSecurePassword.java
- Usage of Connection Pooling in Fabflix Code
  - Connection pooling optimizes the management of database connections. Instead of each servlet creating and closing a connection for every request, which is resource-intensive, connection pooling maintains a pool of connections. When a servlet needs a connection, the pool checks for idle connections. If available, one is provided. If not, and the maximum connection limit has not been reached, a new connection is created and provided. If all connections are in use and the maximum limit is reached, the requests waits until a connection becomes available. When the connection is no longer needed, it is returned to the pool for future use by calling the close() method. This approach significantly enhances performance by reducing the overhead of establishing connections through the reuse of cached database connections. 
  - In our `context.xml` file, we established a JDBC DataSource, facilitating a pool of database connections. Among the specified parameters are limits of 100 for active connections, 30 for idle connections, and a timeout threshold of 10,000 milliseconds for connection establishment.
- Connection Pooling and Backend SQL
  - We established connections to either the Master or Slave's database, serving the same purpose as mentioned above. 
##### Fuzzy Search Design and Implementation
- To employ the Levenshtein Edit Distance Algorithm provided, we first installed and compiled the C code. Specifically, we used the `ed` function, which calculates the edit distance between two strings. In our backend, specifically AutoCompleteServlet.java and SearchServlet.java, we set the edit distance threshold based on the length of the query. 
  - If the length of the title is under 4 characters, the maximum edit distance allowed is 1. 
  - For lengths under 7, an edit distance of 2 is permitted.
  - Otherwise, the maximum edit distance considered is 3. 
- We combined full-text search with fuzzy search using an OR query. Additionally, to ensure case-insensitivity, we converted titles to lowercase before applying the `ed` function. 
##### Master/Slave
- Filenames with Routing Queries to Master/Slave SQL
  - WebContent/META-INF/context.xml
  - src/AddRecordServlet.java
  - src/PaymentServlet.java
  - src/UpdateSecurePassword.java
- Read/Write Requests to Master/Slave SQL 
  - Requests to write data are directed to the master database, which ensures that any changes are accurately copied to the slave database. On the other hand, when retrieving data, requests can be sent to either the master or slave databases. This distribution of requests helps balance the workload on the master database and enables efficient handling of read operations. 
