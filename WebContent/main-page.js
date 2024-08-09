/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleAlphaResult() {
    console.log("handleAlphaResult: populating alpha table with numbers 0-9");

    let alphaTableBodyElement = jQuery("#alpha_table_body");
    alphaTableBodyElement.empty();

    let letterRowHTML = "<tr>";

    // Append 26 cells to the row, one for each letter
    for (let i = 0; i < 26; i++) {
        let letter = String.fromCharCode(65 + i); // 65 is the ASCII code for 'A'
        letterRowHTML += "<td>" + '<a href = movies.html?character=' + encodeURIComponent(letter) + '>' + letter + '</a>' + "</td>";
    }

    letterRowHTML += "</tr><tr>";

    // Add empty cells before the numbers start
    for (let i = 0; i < 8; i++) {
        letterRowHTML += "<td></td>"; // Empty cell
    }

    // Append 10 cells to the row, one for each number
    for (let i = 0; i < 10; i++) {
        letterRowHTML += "<td>" + '<a href = movies.html?character=' + encodeURIComponent(i) + '>' + i + '</a>' + "</td>";
    }

    letterRowHTML += "<td>" + '<a href = movies.html?character=' + encodeURIComponent('*') + '>' + '*' + '</a>' + "</td>";

    letterRowHTML += "</tr>";

    alphaTableBodyElement.append(letterRowHTML);


    document.querySelectorAll('.alpha-link').forEach(item => {
        // Add click event listener to each genre link
        item.addEventListener('click', event => {
            event.preventDefault(); // Prevent default link behavior (e.g., page reload)

            const character = item.getAttribute('data-character');
            console.log('Clicked alphabet character:', character); // Debugging line

            // Redirect to the movies page with the genre ID as a query parameter
            window.location.href = 'api/alpha-movies?character=' + encodeURIComponent(character);
        });
    });

}

function handleGenreResult(resultData) {
    console.log("handleGenreResult: populating genre table from resultData");

    let genreTableBodyElement = jQuery("#genre_table_body");
    genreTableBodyElement.empty();

    for (let i = 0; i < resultData.length; i++) {
        if (i % 4 === 0) {
            genreTableBodyElement.append("<tr>");
        }

        let genreName = resultData[i]['genre_name'];
        let genreId = resultData[i]['genre_id'];
        let rowHTML = "<td>" +
            '<a href="movies.html?genre_id=' + encodeURIComponent(genreId) + '">' +
            genreName +
            '</a>' +
            "</td>";

        genreTableBodyElement.append(rowHTML);

        if ((i + 1) % 4 === 0 || i === resultData.length - 1) {
            genreTableBodyElement.append("</tr>");
        }
    }

    document.querySelectorAll('.genre-link').forEach(item => {
        // Add click event listener to each genre link
        item.addEventListener('click', event => {
            event.preventDefault(); // Prevent default link behavior (e.g., page reload)

            // Get the genre ID from the data-genre-id attribute
            const genreId = item.getAttribute('data-genre-id');
            console.log('Clicked genre link with ID:', genreId); // Debugging line

            // Redirect to the movies page with the genre ID as a query parameter
            window.location.href = 'api/genre-movies?genre_id=' + encodeURIComponent(genreId);
        });
    });
}

// old event listener

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/main-page", // Setting request url, which is mapped by MainPageServlet in MainPageServlet.java
    success: (resultData) => handleGenreResult(resultData) // Setting callback function to handle data returned successfully by the MainPageServlet
});

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/main-page", // Setting request url, which is mapped by MainPageServlet in MainPageServlet.java
    success: (resultData) => handleAlphaResult(resultData) // Setting callback function to handle data returned successfully by the MainPageServlet
});

