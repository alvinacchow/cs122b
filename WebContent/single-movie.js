/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log("handleResult: populating movie info from resultData");

    let movieInfoElement = jQuery("#movie_info");
    movieInfoElement.append("<p class='movie-name'>" + resultData[0]["movie_title"] + " (" + resultData[0]["movie_year"] + ")</p>");

    console.log("handleResult: populating movie info table from resultData");

    let movieInfoTableBodyElement = jQuery("#movie_info_table_body");

    let starsString = "";
    for (let i = 0; i < resultData[0]["movie_stars"]["stars"].length; i += 2) {
        starsString += '<a href="single-star.html?id=' + encodeURIComponent(resultData[0]["movie_stars"]["stars"][i + 1]) + '">' + resultData[0]["movie_stars"]["stars"][i] + '</a>';
        if (i < resultData[0]["movie_stars"]["stars"].length - 2) {
            starsString += ', ';
        }
    }
    let genresString = "";
    for (let i = 0; i < resultData[0]["movie_genres"]["genres"].length; i += 2) {
        genresString += '<a href="movies.html?genre_id=' + encodeURIComponent(resultData[0]["movie_genres"]["genres"][i + 1]) + '">' + resultData[0]["movie_genres"]["genres"][i] + '</a>';
        if (i < resultData[0]["movie_genres"]["genres"].length - 2) {
            genresString += ', ';
        }
    }

    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th>" + resultData[0]["movie_director"] + "</th>";
    rowHTML += "<th>" + starsString + "</th>";
    rowHTML += "<th>" + genresString + "</th>";
    rowHTML += "<th>" + resultData[0]["movie_rating"] + "</th>";
    rowHTML += "<th>" +
        '<button class="add-to-cart-btn" onclick="addToCart(\'' + resultData[0]["movie_id"] + '\')">Add</button>' +
        "</th>";
    rowHTML += "</tr>";

    movieInfoTableBodyElement.append(rowHTML);
}

function addToCart(movieId) {
    $.ajax({
        method: "POST",
        url: "api/cart",
        // Include the action parameter with value 'add'
        data: {
            movieId: movieId,
            action: 'add' // Indicate the add action
        }
    }).done(function(response) {
        // Parse the JSON response
        var message = response;
        // Check the status property in the response object
        if (message.status === "success") {
            alert("Added to cart!");
            // Optionally, refresh the cart display or update the cart count
        } else {
            // If the status is not success, show the error message
            alert("Error: " + message.message);
        }
    }).fail(function(jqXHR, textStatus, errorThrown) {
        alert("Failed to add to cart. Error: " + textStatus);
    });
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by SingleMovieServlet in Single-Movie.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
});
