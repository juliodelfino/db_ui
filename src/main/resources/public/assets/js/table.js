//TO DELETE
var databases = null;
var connLoaderHtml = '<div><img class="center-block" src="/assets/images/fluid-loader.gif"/></div>';
var tableLoaderHtml = '<div><img class="center-block" src="/assets/images/loader1.gif"/></div>';
//TO DELETE - END

var dbConnId = null;

$(document).ready(function() {

	dbConnId = getUrlVars()["id"];
	initTableActions('div');

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

	var tableName = getUrlVars()["table"];
	$(tabPanel + ' .q-columns-btn').click(function(){
		$(tabPanel + ' .qbox').val('');
		getColumns(tableName, tabPanel);
	});
	
	$(tabPanel + ' .q-rowcount-btn').click(function(){
		var sql = 'SELECT COUNT(*) AS "COUNT" FROM ' + tableName;
		$(tabPanel + ' .qbox').val(sql);
		$(tabPanel + ' .exec-sql-form').trigger('submit');
	});
	
	$(tabPanel + ' .q-alldata-btn').click(function(){
		var sql = 'SELECT * FROM ' + tableName;
		$(tabPanel + ' .qbox').val(sql);
		$(tabPanel + ' .exec-sql-form').trigger('submit');
	});

	$(tabPanel + ' .exec-btn').click(function(){
		$(tabPanel + ' .exec-sql-form').trigger('submit');
	});
	
	$(tabPanel + ' .exec-sql-form').submit(function(e){
        e.preventDefault();
		executeQuery($(tabPanel + ' .qbox').val(), tabPanel);
	});
	
	$(tabPanel + ' .refresh-dbconn-btn').click(function(){
		loadSchemaView(tabPanel, true);
	});

//	$('.nav-sidebar li a').click(function(){
//        $('#table-name').html($(this).html());
//	});
	
	
}

function getColumns(tableName, tabPanel) {

	$(tabPanel + ' .dynamic-table').html(tableLoaderHtml);
	var params = {
		connId: dbConnId,
		table: tableName
	};
  	$.get("/table/columns", params, function(result){
  		result = JSON.parse(result);
  		updateDynamicTable(result, tabPanel);
  	});
}

function executeQuery(sql, tabPanel) {

	$(tabPanel + ' .dynamic-table').html(tableLoaderHtml);
	var params = {
		connId: dbConnId,
		q: sql
	};
  	$.get("/table/query", params, function(result){
  		result = JSON.parse(result);
  		if (result.error) {
  			displayError(result, tabPanel);
  		} else {
  	  		updateDynamicTable(result, tabPanel);
  		}
  	});
}

function updateDynamicTable(result, tabPanel) {

	var tableHeaders = '';
	$.each(result.columns, function(i, val) {
	    tableHeaders = tableHeaders + '<th>' + val.title + '</th>';
	});

	$(tabPanel + ' .dynamic-table').html(
			'<table><thead><tr>' + tableHeaders + '</tr></thead></table>');

	table = $(tabPanel + ' .dynamic-table table').DataTable({
		destroy: true,
		dom: 'Bfrtip',
		columns: result.columns,
		data: result.data,
        buttons: ['copy','csv'],
        iDisplayLength: 20
	});
	
	$(tabPanel + ' .dynamic-table tbody tr td').dblclick(function(){
		if (hasOverflow($(this).get(0))) {
			$('#myModal .modal-body').html($(this).html());
			$('#myModal').modal('show');
		}
	});
}

function displayError(result, tabPanel) {

	$(tabPanel + ' .dynamic-table').html(
			'<div class="col-lg-12 alert alert-danger">' + result.message + '</div>');
}
