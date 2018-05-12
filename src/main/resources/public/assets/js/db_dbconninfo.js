//TO DELETE
var connLoaderHtml = '<div><img class="center-block" src="/assets/images/fluid-loader.gif"/></div>';
var tableLoaderHtml = '<div><img class="center-block" src="/assets/images/loader1.gif"/></div>';
//TO DELETE - END

var dbConnId = null;

$(document).ready(function() {

	$.getScript('/assets/js/conn_info_dialog.js');
	dbConnId = getUrlVars()["id"];
	initTableActions('div');

	$('#refresh-btn').click(function(){
		window.location.href = "/db/dbinfo?id=" + dbConnId + "&refresh=true";
	});
	
	$('#delete-dbinfo-btn').click(function(){
		var confirmDelete = confirm("Deleting this database cannot be undone. Click OK to continue.");
		if (confirmDelete) {
			$.ajax({
			    url: '/db/info?connId=' + dbConnId,
			    type: 'DELETE',
			    success: function(result) {
					window.location.href = '/db'
			    }
			});
		}
	});
});

function getUrlVars()
{
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

function initTableActions(tabPanel) {

	$(tabPanel + ' .conn-info-btn').click(function(){

	  	$.get("/db/info", {connId: dbConnId}, function(result){

	  		result = JSON.parse(result);
	  		$('#conn-info-dialog input[name=connectionName]').val(result.connectionName);
	  		$('#conn-info-dialog input[name=driver]').val(result.driver);
	  		$('#conn-info-dialog input[name=url]').val(result.url);
	  		$('#conn-info-dialog input[name=username]').val(result.username);
	  		$('#conn-info-dialog input[name=connId]').val(result.connId);
	  		$('#conn-info-dialog .parent-tabpanel').val(tabPanel);
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
