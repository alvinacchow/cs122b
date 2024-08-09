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

let currentPage = sessionStorage.getItem('currentPage') || 1;
let limit = sessionStorage.getItem('limit') || 10;
let hasMorePages = sessionStorage.getItem('hasMorePages') || 'true';
let sortBy = sessionStorage.getItem('sortBy') || 'title asc rating desc';

function updateSessionStorage() {
    sessionStorage.setItem('currentPage', currentPage);
    sessionStorage.setItem('limit', limit);
    sessionStorage.setItem('hasMorePages', hasMorePages);
    sessionStorage.setItem('sortBy', sortBy);
}

function getMoviesByGenre(genreId, sort, limit, offset) {
    const genreQuery = {
        genre_id: genreId,
        sort: sort,
        limit: limit,
        offset: offset
    };

    console.log('genreQuery:', genreQuery);

    $.ajax({
        method: "GET",
        url: "api/genre-movies",
        data: genreQuery,
        success: function (resultData) {
            // Call function to populate the movie table with the JSON data from the servlet
            populateMovieTable(resultData);
            updateDisplay();

            if (resultData.length < limit) {
                hasMorePages = false;
            } else {
                hasMorePages = true;
            }
        },
        error: function (errorData) {
            // Handle any errors here
            console.error("Error fetching movies by genre:", errorData);
        }
    });
}

// Add a new function to fetch the top 20 movies
function getTop20Movies() {
    $.ajax({
        dataType: "json",
        method: "GET",
        url: "api/top20-movies", // Assuming your servlet will handle this endpoint
        success: function (resultData) {
            populateMovieTable(resultData);
        },
        error: function (errorData) {
            console.error("Error fetching top 20 movies:", errorData);
        }
    });
}

// Add a new function to fetch by alpha character
function getByAlpha(character, sort, limit, offset) {
    const alphaQuery = {
        character: character,
        sort: sort,
        limit: limit,
        offset: offset
    };

    console.log('alphaQuery:', alphaQuery);

    $.ajax({
        method: "GET",
        url: "api/alpha-movies?character=" + character, // Assuming your servlet will handle this endpoint
        data: alphaQuery,
        success: function (resultData) {
            populateMovieTable(resultData);
            updateDisplay();

            if (resultData.length < limit) {
                hasMorePages = false;
            } else {
                hasMorePages = true;
            }
        },
        error: function (errorData) {
            console.error("Error fetching movies by alpha:", errorData);
        }
    });
}

function searchMovies(title, year, director, star, sort, limit, offset) {
    // Construct the search query object
    const searchQuery = {
        title: title,
        year: year,
        director: director,
        star: star,
        sort: sort,
        limit: limit,
        offset: offset
    };

    console.log('searchQuery:', searchQuery);

    // AJAX call to the backend search API
    $.ajax({
        method: "GET",
        url: "api/search",
        data: searchQuery,
        success: function(response) {
            // Handle the response, typically by updating the DOM
            populateMovieTable(response);
            updateDisplay();

            if (response.length < limit) {
                hasMorePages = false;
            } else {
                hasMorePages = true;
            }
        },
        error: function(error) {
            // Handle any errors
            console.error("Error during search:", error);
        }
    });
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

$(document).ready(function() {
    getMovies(getSortByValue(), getMoviesPerPage(), getOffsetValue());
});

function getMovies(sort, limit, offset) {
    const params = new URLSearchParams(window.location.search);
    const genreId = params.get('genre_id');
    const character = params.get('character');
    const isTop20 = params.get('top20') === 'true';
    const title = params.get('title');
    const year = params.get('year');
    const director = params.get('director');
    const star = params.get('star');

    // Check if any search parameter is present and not empty
    if (title || year || director || star) {
        searchMovies(title, year, director, star, sort, limit, offset);
    }
    else if (genreId) {
        getMoviesByGenre(genreId, sort, limit, offset);
    }
    else if (character) {
        getByAlpha(character, sort, limit, offset);
    }
    else if (isTop20) {
        getTop20Movies();
    }
}

document.addEventListener('DOMContentLoaded', (event) => {
    const sortBySelect = document.getElementById('sortBy');
    const moviesPerPageSelect = document.getElementById('moviesPerPage');

    if (sortBySelect) {
        sortBySelect.addEventListener('change', function() {
            sortBy = this.value;
            sessionStorage.setItem('sortBy', sortBy);
            getMovies(sortBy, getMoviesPerPage(), getOffsetValue());
        });
    }

    if (moviesPerPageSelect) {
        moviesPerPageSelect.addEventListener('change', function() {
            limit = this.value;
            sessionStorage.setItem('limit', limit);
            getMovies(getSortByValue(), limit, getOffsetValue());
        });
    }
});

function getSortByValue() {
    const storedSortBy = sessionStorage.getItem('sortBy');
    const sortBySelect = document.getElementById('sortBy');
    if (storedSortBy) {
        return storedSortBy;
    } else if (sortBySelect) {
        return sortBySelect.value;
    } else {
        return sortBy;
    }
}

function getMoviesPerPage() {
    const storedMoviesPerPage = sessionStorage.getItem('limit');
    if (storedMoviesPerPage) {
        return storedMoviesPerPage;
    } else {
        const moviesPerPageSelect = document.getElementById('moviesPerPage');
        if (moviesPerPageSelect) { // Check if moviesPerPageSelect is not null
            return moviesPerPageSelect.value;
        } else {
            // Handle the case where the element is not found, perhaps return a default value
            return limit; // You can define defaultValue according to your needs
        }
    }
}

function getOffsetValue() {
    const storedCurrentPage = sessionStorage.getItem('currentPage') || 1;
    const storedLimit = sessionStorage.getItem('limit') || 10;
    const offset = (storedCurrentPage - 1) * storedLimit;
    return offset;
}

function next() {
    if (hasMorePages) {
        currentPage++;
        updateSessionStorage();
        getMovies(getSortByValue(), limit, getOffsetValue());
        updateDisplay();
    }
}

function prev() {
    if (currentPage > 1) {
        currentPage--;
        hasMorePages = true;
        updateSessionStorage();
        getMovies(getSortByValue(), limit, getOffsetValue());
        updateDisplay();
    }
}

function updateDisplay() {
    const storedCurrentPage = sessionStorage.getItem('currentPage') || 1;
    document.getElementById('currentPage').innerText = 'Page ' + storedCurrentPage;

    const sortBySelect = document.getElementById('sortBy');
    const storedSortBy = sessionStorage.getItem('sortBy') || 'title asc rating desc';
    if (storedSortBy && sortBySelect) {
        // Loop through options to find and select the matching value
        Array.from(sortBySelect.options).forEach(option => {
            if (option.value === storedSortBy) {
                option.selected = true;
            }
        });
    }

    const moviesPerPageSelect = document.getElementById('moviesPerPage');
    const storedMoviesPerPage = sessionStorage.getItem('limit') || 10;
    if (storedMoviesPerPage && moviesPerPageSelect) {
        // Loop through options to find and select the matching value
        Array.from(moviesPerPageSelect.options).forEach(option => {
            if (option.value === storedMoviesPerPage) {
                option.selected = true;
            }
        });
    }
}

function populateMovieTable(resultData) {
    let movieTableBody = $("#movie_table_body");
    movieTableBody.empty(); // Clear the existing table contents

    if (resultData.length > 0) {
        console.log(resultData);
        for (let i = 0; i < resultData.length; ++i) {
            var star1 = {
                name: typeof resultData[i]["movie_stars"]["stars"][0] !== 'undefined' ? resultData[i]["movie_stars"]["stars"][0] : '',
                id: typeof resultData[i]["movie_stars"]["stars"][1] !== 'undefined' ? resultData[i]["movie_stars"]["stars"][1] : '',
            };
            console.log("star2: ", typeof resultData[i]["movie_stars"]["stars"][2]);
            var star2 = {
                name: typeof resultData[i]["movie_stars"]["stars"][2] !== 'undefined' ? resultData[i]["movie_stars"]["stars"][2] : '',
                id: typeof resultData[i]["movie_stars"]["stars"][3] !== 'undefined' ? resultData[i]["movie_stars"]["stars"][3] : '',

            };

            var star3 = {
                name: typeof resultData[i]["movie_stars"]["stars"][4] !== 'undefined' ? resultData[i]["movie_stars"]["stars"][4] : '',
                id: typeof resultData[i]["movie_stars"]["stars"][5] !== 'undefined' ? resultData[i]["movie_stars"]["stars"][5] : '',

            };

            var genre1 = {
                name: typeof resultData[i]["movie_genres"]["genres"][0] !== 'undefined' ? resultData[i]["movie_genres"]["genres"][0] : '',
                id: typeof resultData[i]["movie_genres"]["genres"][0] !== 'undefined' ? resultData[i]["movie_genres"]["genres"][1] : '',
            };

            var genre2 = {
                name: typeof resultData[i]["movie_genres"]["genres"][2] !== 'undefined' ? resultData[i]["movie_genres"]["genres"][2] : '',
                id: typeof resultData[i]["movie_genres"]["genres"][2] !== 'undefined' ? resultData[i]["movie_genres"]["genres"][3] : '',
            };

            var genre3 = {
                name: typeof resultData[i]["movie_genres"]["genres"][4] !== 'undefined' ? resultData[i]["movie_genres"]["genres"][4] : '',
                id: typeof resultData[i]["movie_genres"]["genres"][4] !== 'undefined' ? resultData[i]["movie_genres"]["genres"][5] : '',
            };


            // Append a comma to star1 if star2 exists and star1 is not null
            star1.name = (star2.name !== '' && star1.name !== '') ? star1.name + ", " : star1.name;
            // Append a comma to star2 if star3 exists and star2 is not null
            star2.name = (star3.name !== '' && star2.name !== '') ? star2.name + ", " : star2.name;



            // Append a comma to genre1 if genre2 exists and genre1 is not null
            genre1.name = (genre2.name !== '' && genre1.name !== '') ? genre1.name + ", " : genre1.name;
            // Append a comma to genre2 if genre3 exists and genre2 is not null
            genre2.name = (genre3.name !== '' && genre2.name !== '') ? genre2.name + ", " : genre2.name;


            // Concatenate the html tags with resultData jsonObject
            let rowHTML = "";
            rowHTML += "<tr>";
            rowHTML +=
                "<th>" +
                // Add a link to single-movie.html with id passed as a query parameter
                '<a href="single-movie.html?id=' + encodeURIComponent(resultData[i]['movie_id']) + '">' +
                resultData[i]["movie_title"] +     // display movie_name for the link text
                '</a>' +
                "</th>";

            rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
            rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
            rowHTML +=
                "<th>" +
                '<a href="single-star.html?id=' + encodeURIComponent(star1.id) + '">' + star1.name + '</a>' +
                '<a href="single-star.html?id=' + encodeURIComponent(star2.id) + '">' + star2.name + '</a>' +
                '<a href="single-star.html?id=' + encodeURIComponent(star3.id) + '">' + star3.name + '</a>' +
                "</th>";

            rowHTML +=
                "<th>" +
                '<a href="movies.html?genre_id=' + encodeURIComponent(genre1.id) + '">' + genre1.name + '</a>' +
                '<a href="movies.html?genre_id=' + encodeURIComponent(genre2.id) + '">' + genre2.name + '</a>' +
                '<a href="movies.html?genre_id=' + encodeURIComponent(genre3.id) + '">' + genre3.name + '</a>' +
                "</th>";

            rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";

            rowHTML += "<th>" +
                '<button class="add-to-cart-btn" onclick="addToCart(\'' + resultData[i]["movie_id"] + '\')">Add</button>' +
                "</th>";

            rowHTML += "</tr>";

            movieTableBody.append(rowHTML);
        }
    }
}