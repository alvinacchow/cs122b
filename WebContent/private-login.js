let employee_form = $("#employee_login");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    try {
        console.log("result: ", resultDataString);
        let resultDataJson = JSON.parse(resultDataString);

        console.log("handle login response");
        console.log(resultDataJson);
        console.log(resultDataJson["status"]);

        // If login succeeds, it will redirect the user to index.html
        if (resultDataJson["status"] === "success") {
            window.location.replace("metadata.html");
        } else {
            // If login fails, the web page will display
            // error messages on <div> with id "login_error_message"
            console.log("show error message");
            console.log(resultDataJson["message"]);
            $("#employee_login_error").text(resultDataJson["message"]);
        }
    } catch (error) {
        console.log("result: ", resultDataString);
        console.error("Error handling login result:", error);
        // Display a generic error message to the user
        $("#employee_login_error").text("An error occurred. Please try again later.");
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit employee login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    // var recaptchaResponse = grecaptcha.getResponse();

    // Check if reCAPTCHA response is empty
    // if (!recaptchaResponse) {
    //     console.log("reCAPTCHA response is empty");
    //     // Display error message to the user
    //     $("#employee_login_error").text("Please complete the CAPTCHA");
    //     return; // Exit the function without submitting the form
    // }

    $.ajax({
        url: "api/dashboard_login",
        method: "POST",
        data: employee_form.serialize(),
        success: handleLoginResult,
        error: function(xhr, status, error) {
            console.error("AJAX Error:", error);
            // Display a generic error message to the user
            $("#employee_login_error").text("An error occurred while processing your request. Please try again later.");
        }
    });

}

// Bind the submit action of the form to a handler function
employee_form.submit(submitLoginForm);