// navbar.js

document.addEventListener("DOMContentLoaded", function() {
    if (window.location.pathname.endsWith('metadata.html') ||
        window.location.pathname.endsWith('add-movie.html') ||
        window.location.pathname.endsWith('add-star.html') ||
        window.location.pathname.endsWith('add-genre.html')) {
        fetch("private-nav.html")
            .then(response => response.text())
            .then(data => {
                // Populate the navbar placeholder with the fetched HTML
                document.getElementById("private-navbar-placeholder").innerHTML = data;
            })
            .catch(error => console.error(error));
    }
    else {
        fetch("navbar.html")
            .then(response => response.text())
            .then(data => {
                // Populate the navbar placeholder with the fetched HTML
                document.getElementById("navbar-placeholder").innerHTML = data;

                const storedFormData = sessionStorage.getItem('searchFormData');
                if (storedFormData) {
                    updateNavbarResultLink(storedFormData);
                }

                // $('#autocomplete') is to find element by the ID "autocomplete"
                $('#autocomplete').autocomplete({
                    // documentation of the lookup function can be found under the "Custom lookup function" section
                    lookup: function (query, doneCallback) {
                        handleLookup(query, doneCallback)
                    },
                    onSelect: function(suggestion) {
                        handleSelectSuggestion(suggestion)
                    },
                    // set delay time
                    deferRequestBy: 300,
                });

                // bind pressing enter key to a handler function
                $('#autocomplete').keypress(function(event) {
                    // keyCode 13 is the enter key
                    if (event.keyCode == 13) {
                        // pass the value of the input box to the handler function
                        handleNormalSearch($('#autocomplete').val())
                    }
                })

                // Attach the event listener for the search form after the navbar is loaded
                $("#search_form").submit(function (event) {
                    console.log("search is reached javascript");
                    event.preventDefault(); // Prevent the default form submission

                    // Serialize the form data for the AJAX request
                    let formData = $(this).serialize();
                    sessionStorage.setItem('searchFormData', formData);
                    console.log('formdata', formData);

                    // Make the AJAX request to the backend search endpoint
                    $.ajax({
                        dataType: "json", // Expecting JSON data in response
                        method: "GET", // Request method
                        url: "api/search", // Backend search endpoint
                        data: formData, // Data to be sent in the request
                        success: function (resultData) {
                            // Update the browser's address bar with the search parameters
                            window.location.href = 'movies.html?' + formData;
                            // Call the function to populate the movie table with the search result
                            populateMovieTable(resultData);
                        },
                        error: function (errorData) {
                            // Handle any errors here
                            console.error("Search error:", errorData);
                        }
                    });
                    updateNavbarResultLink(formData);
                });
            })
            .catch(error => console.error(error));
    }

});

function updateNavbarResultLink(formData) {
    // Use the formData directly to construct the "Result" link
    const resultLink = `movies.html?${formData}`;

    const resultLinkElement = $('#resultHistory');
    if (resultLinkElement.length) {
        resultLinkElement.attr('href', resultLink);
    } else {
        console.error('Navbar Result link element not found');
    }
}


/*
 * this function is called by the library when it needs to lookup a query.
 *
 * the parameter query is the query string.
 * the doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    if (query.length < 3) return;

    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")
    const data = localStorage.getItem(query)

    if (data) {
        console.log("cached results found in local storage")
        handleLookupAjaxSuccess(JSON.parse(data), query, doneCallback)
        return;
    }

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        method: "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        url: "api/autocomplete?query=" + escape(query),
        success: function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        error: function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * this function is used to handle the ajax success callback function.
 * it is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON
    var jsonData = $.parseJSON(JSON.stringify(data));
    console.log(jsonData)

    localStorage.setItem(query, JSON.stringify(data))

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * this function is the select suggestion handler function.
 * when a suggestion is selected, this function is called by the library.
 *
 * you can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["id"])
    window.location.href = `single-movie.html?id=${suggestion["data"]["id"]}`
}


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
}