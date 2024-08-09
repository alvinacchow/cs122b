$(document).ready(function () {
    // Load the cart when the page is ready
    loadCart();
});


document.getElementById("checkout-button").addEventListener("click", function() {
    window.location.href = "payment.html";
});


function updateCartDisplay(cartData) {
    var cartTableBody = $('#cart_table_body');
    cartTableBody.empty(); // Clear existing cart items

    $.each(cartData, function (index, movieDetails) {
        var row = $('<tr>');
        // Create an anchor element with a hyperlink to the single movie page
        var titleLink = $('<a>')
            .attr('href', 'single-movie.html?id=' + encodeURIComponent(movieDetails.movieId))
            .text(movieDetails.title);

        row.append($('<td>').append(titleLink)); // Append the anchor element to the table cell
        var minusButton = $('<button>')
            .text('-')
            .click(function () {
                // Call the updateCartItem function with the new quantity
                updateCartItem(movieDetails.movieId, movieDetails.quantity - 1);
            });

        var plusButton = $('<button>')
            .text('+')
            .click(function () {
                // Call the updateCartItem function with the new quantity
                updateCartItem(movieDetails.movieId, movieDetails.quantity + 1);
            });

        var quantityCell = $('<td>')
            .append(minusButton)
            .append($('<span>').text(' ' + movieDetails.quantity + ' '))
            .append(plusButton);

        row.append(quantityCell);

        var deleteButton = $('<button>')
            .text('Delete')
            .attr('data-movie-id', movieDetails.movieId) // Store the movie ID in the button
            .click(function () {
                // Call the deleteCartItem function when the button is clicked
                deleteCartItem(movieDetails.movieId);
            });

        row.append($('<td>').append(deleteButton));

        row.append($('<td>').text('$' + movieDetails.price.toFixed(2)));
        row.append($('<td>').text('$' + (movieDetails.quantity * movieDetails.price).toFixed(2)));
        cartTableBody.append(row);
    });

    // Update the total sum after all movies are added to the table
    updateTotalSum(cartData);
}


function loadCart() {
    $.ajax({
        method: "GET",
        url: "api/cart"
    }).done(function(response) {
        updateCartDisplay(response);
    }).fail(function(jqXHR, textStatus, errorThrown) {
        // Log the error to the console for debugging
        console.error("Failed to load cart:", textStatus, errorThrown);
        alert("Failed to load cart.");
    });
}

function deleteCartItem(movieId) {
    $.ajax({
        method: "POST",
        url: "api/cart",
        data: {
            movieId: movieId,
            action: 'delete'
        }
    }).done(function(response) {
        console.log("Delete Response:", response); // Log the response
        var message = response;
        if (message.status === "success") {
            console.log("trying to delete: " + movieId);
            $('#cart_table_body tr[data-movie-id="' + movieId + '"]').remove();
            loadCart();
        } else {
            alert("Error: " + message.message);
        }
    }).fail(function(jqXHR, textStatus, errorThrown) {
        alert("Failed to delete item from cart. Error: " + textStatus);
    });
}


function updateCartItem(movieId, newQuantity) {
    // If the new quantity is zero or less, directly delete the item
    if (newQuantity <= 0) {
        deleteCartItem(movieId);
        return;
    }

    // Otherwise, proceed with updating the quantity
    $.ajax({
        method: "POST",
        url: "api/cart",
        data: {
            movieId: movieId,
            quantity: newQuantity,
            action: 'update'
        }
    }).done(function(response) {
        var message = response;
        if (message.status === "success") {
            // If the update was successful, reload the cart to reflect the changes
            loadCart();
        } else {
            alert("Error: " + message.message);
        }
    }).fail(function(jqXHR, textStatus, errorThrown) {
        alert("Failed to update item in cart. Error: " + textStatus);
    });
}


function updateTotalSum(cartData) {
    var total = 0;
    $.each(cartData, function (index, movieDetails) {
        total += movieDetails.quantity * movieDetails.price;
    });
    $('#total-amount').text(total.toFixed(2));
}

