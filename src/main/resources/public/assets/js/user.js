$(document).ready(function() {
	
    $("#settings-form").submit(function(e){
        e.preventDefault();

        updateSettings($(this).serialize());
    });
});

function updateSettings(userInfo) {
	
	$('#loading-icon img').show();
    $.post("/user/password", userInfo, function(result){

        $('#loading-icon img').hide();
    	result = JSON.parse(result);
    	if (result.error) {
    		console.log(result);
    		$('#error-msg').html(result.message);
    		
    		$('#settings-error-div').show();    
    		setTimeout(function() {
    			$("#settings-error-div").slideUp(500); 
    	    }, 5000);
    	} else {
    		
    		$('#settings-success-div').show();    
    		setTimeout(function() {
    			$("#settings-success-div").slideUp(500); 
    	    }, 5000);
    	}
    });
}

