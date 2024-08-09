$(document).ready(function () {
    // Load the cart when the page is ready
    updateConfirmationDisplay();
});

function updateConfirmationDisplay() {
    var confirmationTableBody = $('#confirmation_table_body');
    confirmationTableBody.empty(); // Clear existing cart items

    let orderArray = JSON.parse(sessionStorage.getItem("orderArray"));

    $.each(orderArray, function (index, orderItem) {
        var row = $('<tr>');

        row.append($('<td>').text(orderItem.sale_id));

        $.each(orderItem.movie_details, function (i, movieDetail) {
            var titleLink = $('<a>')
                .attr('href', 'single-movie.html?id=' + encodeURIComponent(orderItem.movie_id))
                .text(movieDetail.movie_title);
            row.append($('<td>').append(titleLink));

            var quantityCell = $('<td>').append($('<span>').text(' ' + orderItem.movie_quantity + ' '))
            row.append(quantityCell);

            row.append($('<td>').text('$' + movieDetail.price.toFixed(2)));
            row.append($('<td>').text('$' + (orderItem.movie_quantity * movieDetail.price).toFixed(2)));

            confirmationTableBody.append(row);
        });
    });
    updateTotalSum(orderArray);
}

function updateTotalSum(orderArray) {
    var total = 0;
    $.each(orderArray, function (index, orderItem) {
        var movieDetail = orderItem.movie_details[0];
        total += orderItem.movie_quantity * movieDetail.price;
    });
    $('#total-amount').text(total.toFixed(2));
}
