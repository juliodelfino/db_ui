
$(document).ready(function() {

	$.getScript('/assets/js/conn_info_dialog.js');
	initTableActions('div');
	
});

function initTableActions(tabPanel) {

	$(tabPanel + ' .conn-info-btn').click(function(){

		dbConnId = $(this).parent().data('id');
	  	$.get("/db/info", {connId: dbConnId}, function(result){

	  		result = JSON.parse(result);
	  	    $.each(result, function(prop, value){
	  	        $("#conn-info-dialog input[name='" + prop + "']").val(value);
	  	     });
	  	    
	  		$('#conn-info-dialog .parent-tabpanel').val(tabPanel);
			$('#conn-info-dialog').modal('show');
	  	});
	});
	
	
	$('.delete-dbinfo-btn').click(function(){

		var confirmDelete = confirm("Deleting this database cannot be undone. Click OK to continue.");
		if (confirmDelete) {

			dbConnId = $(this).parent().data('id');
			$.ajax({
			    url: '/db/info?connId=' + dbConnId,
			    type: 'DELETE',
			    success: function(result) {
					window.location.href = '/db'
			    }
			});
		}
	});
	
	$(tabPanel + ' .refresh-dbconn-btn').click(function(){
//		loadSchemaView(tabPanel, true);
	});

//	$('.nav-sidebar li a').click(function(){
//        $('#table-name').html($(this).html());
//	});
	
	
}

function displayError(result, tabPanel) {

	$(tabPanel + ' .dynamic-table').html(
			'<div class="col-lg-12 alert alert-danger">' + result.message + '</div>');
}
