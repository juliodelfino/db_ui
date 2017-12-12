$(document).ready(function() {
	
	$('#conn-info-dialog input[name="username"]').prop('disabled', true);
	$('#conn-info-dialog input[name="password"]').prop('disabled', true);
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
	  		} else {
	  			$("#conn-info-dialog .alert-danger").show();
	    		setTimeout(function() {
	    			$("#conn-info-dialog .alert-danger").slideUp(500); 
	    	    }, 5000);
	  		}
	  	});
	});
	
	$('#delete-dbinfo-btn').click(function(){
		var confirmDelete = confirm("Deleting this database cannot be undone. Click OK to continue.");
		if (confirmDelete) {
			
			$('#conn-info-dialog .conn-loading-icon').show();

			var dbInfoParams = $('#conn-info-form').serialize();
			var dbInfo = objectifyForm($('#conn-info-form').serializeArray());
			$.ajax({
			    url: '/db/info?' + dbInfoParams,
			    type: 'DELETE',
			    success: function(result) {
					$('#conn-info-dialog .conn-loading-icon').hide();

		  		    var tabId = '#header_' + dbInfo.connId;
		  		    var tabPanel = $(tabId).attr('href');
			        $(tabPanel).remove();
			        $(tabId).parent().remove();
			        $(".nav-tabs li").last().children('a').first().click();
					
					$('#conn-info-dialog').modal('hide');
			    }
			});
		}
	});
});
