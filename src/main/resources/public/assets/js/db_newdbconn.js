$(document).ready(function() {
	$("#db-connect-form").submit(function(e){
        e.preventDefault();
        $('#conn-loading-icon img').show();
        $.post("/db/connectdb", $(this).serialize(), function(result){

            $('#conn-loading-icon img').hide();
        	result = JSON.parse(result);
        	if (result.error) {
        		alert(result.message);
        	} else {
        		window.location.href = '/db/dbinfo?id=' + result.connId
        	}
        });
    });
});