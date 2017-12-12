
$(document).ready(function() {

    $('#login-btn').click(function(){

        login($('#login-form').serialize());
    });

    $("#login-form").submit(function(e){
        e.preventDefault();
        login($(this).serialize());
    });
	
});

function login(user) {

    $.post("/user/login", user).done( function(result){

    	var success = JSON.parse(result);
    	if (!success) {
    		$('#login-error-div').show();    
    		setTimeout(function() {
    			$("#login-error-div").slideUp(500); 
    	    }, 5000);
    	} else {
        	window.location.replace('/db');
    	}
    });
}
