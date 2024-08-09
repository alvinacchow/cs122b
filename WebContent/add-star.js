// add-movie.js
$(document).ready(function() {
    $("#submit-btn").click(function(event) {
        event.preventDefault();
        var formData = $("#add_form").serialize();

        var name = $("input[name='name']").val();

        // Check if any required field is empty
        if (name.trim() === "") {
            alert("Please enter the star's name.");
            return;
        }

        $.ajax({
            type: "POST",
            url: "api/add-record", // Make sure this URL matches your servlet mapping
            data: formData,
            success: function(response) {
                alert(response);
                if (! response.toString().includes("Error")) {
                    document.getElementById('add_form').reset();
                }
            }
        });
    });
});