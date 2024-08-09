let payment_form = $("#payment_form");

$(document).ready(function () {
    loadCart();
});

function loadCart() {
    $.ajax({
        method: "GET",
        url: "api/cart"
    }).done(function(response) {
        updateTotalSum(response);
    }).fail(function(jqXHR, textStatus, errorThrown) {
        // Log the error to the console for debugging
        console.error("Failed to load cart:", textStatus, errorThrown);
        alert("Failed to load cart.");
    });
}

function updateTotalSum(cartData) {
    var total = 0;
    $.each(cartData, function (index, movieDetails) {
        total += movieDetails.quantity * movieDetails.price;
    });
    $('#total-amount').text('Final Payment: $' + total.toFixed(2));
}

function handlePaymentResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle payment response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        sessionStorage.setItem("orderArray", JSON.stringify(resultDataJson["orderArray"]));
        window.location.replace("confirmation.html");
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#payment_error").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitPaymentForm(formSubmitEvent) {
    console.log("submit payment form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax({
        url: "api/payment",
        method: "POST",
        data: payment_form.serialize(), // Serialize the login form to the data sent by POST request
        success: handlePaymentResult,
        error: function(errorData) {
            console.error("Error placing order: ", errorData);
        }
    });
}

payment_form.submit(submitPaymentForm);