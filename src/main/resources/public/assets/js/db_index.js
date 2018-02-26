
$(document).ready(function() {

	initTableActions('div');
});

function initTableActions(tabPanel) {

	$(tabPanel + ' .conn-info-btn').click(function(){

		dbConnId = $(this).data('id');
	  	$.get("/db/info", {connId: dbConnId}, function(result){

	  		result = JSON.parse(result);
	  		$('#conn-info-dialog input[name=connectionName]').val(result.connectionName);
	  		$('#conn-info-dialog input[name=driver]').val(result.driver);
	  		$('#conn-info-dialog input[name=url]').val(result.url);
	  		$('#conn-info-dialog input[name=username]').val(result.username);
	  		$('#conn-info-dialog input[name=connId]').val(result.connId);
	  		$('#conn-info-dialog #parent-tabpanel').val(tabPanel);
			$('#conn-info-dialog').modal('show');
	  	});
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
