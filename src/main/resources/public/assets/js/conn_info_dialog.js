$(document).ready(function() {

	$('#conn-info-dialog input[name="url"]').prop('readonly', true);
	$('#conn-info-dialog input[name="username"]').prop('readonly', true);
	$('#conn-info-dialog input[name="password"]').prop('readonly', true);
	$('#update-dbinfo-btn').click(function(e){
		e.preventDefault();
		$('#conn-info-form span.conn-loading-icon').show();
		var dbInfoParams = $('#conn-info-form').serialize();
		var dbInfo = objectifyForm($('#conn-info-form').serializeArray());
	  	$.post("/db/infoupdate", dbInfoParams, function(result){

			$('#conn-info-form span.conn-loading-icon').hide();
	  		if (JSON.parse(result)) {
	  		    var tabId = '#header_' + dbInfo.connId;
	  		    $(tabId).html(dbInfo.connectionName);
				$('#conn-info-dialog').modal('hide');
				location.reload();
	  		} else {
	  			$("#conn-info-dialog .alert-danger").show();
	    		setTimeout(function() {
	    			$("#conn-info-dialog .alert-danger").slideUp(500); 
	    	    }, 5000);
	  		}
	  	});
	});
});
