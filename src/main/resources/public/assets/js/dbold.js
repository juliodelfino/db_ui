
var databases = null;
var connLoaderHtml = '<div><img class="center-block" src="/assets/images/fluid-loader.gif"/></div>';
var tableLoaderHtml = '<div><img class="center-block" src="/assets/images/loader1.gif"/></div>';

$(document).ready(function() {

	$.getScript('/assets/js/conn_info_dialog.js');
	initDatabases();
    $("#db-connect-form").submit(function(e){
        e.preventDefault();
        $('#conn-loading-icon img').show();
        $.post("/dbold/connectdb", $(this).serialize(), function(result){

            $('#conn-loading-icon img').hide();
        	result = JSON.parse(result);
        	if (result.error) {
        		alert(result.message);
        	} else {
        		var tabHeaderId = addTab(result);
        		$('#' + tabHeaderId).trigger('click');
        		$('#db-connect-form').trigger('reset');
        	}
        });
    });

});

function initDatabases() {

  	$.get("/dbold/alldb", function(result){
        databases = JSON.parse(result);
        $.each(databases, function(i, val) {
        	 addTab(val);
        });
  	});
}

function addTab(dbInfo) {
    var tabId = 'tab_' + dbInfo.connId;
    var headerId = 'header_' + dbInfo.connId;
    $('#myTab li:last').after(
       '<li class="nav-item"><a href="#' + tabId + '" id="' + headerId
           + '" data-toggle="tab" aria-controls="' + tabId + '" >'
           + dbInfo.connectionName + '</a></li>');
    $('.tab-content').append('<div class="tab-pane" id="' + tabId
       + '" role="tabpanel" aria-labelledby="' + headerId 
       + '" data-conn-id="' + dbInfo.connId + '"></div>');

    $('#' + headerId).click(function(){ 
    	loadSchemaView('#' + tabId); 
	});
    return headerId;
}

function loadSchemaView(tabPanel, forceLoad) {

    if ($(tabPanel).html().length == 0 || forceLoad) {
        $(tabPanel).html(connLoaderHtml);
      	$.get("/dbold/tableview", {connId: $(tabPanel).data("connId")}, function(result){
            $(tabPanel).html(result);
            initTableActions(tabPanel);
      	});
    }
}

function initTableActions(tabPanel) {
	
	$(tabPanel + ' .q-columns-btn').click(function(){
		var tableName = $(tabPanel + ' .table-select').val();
		$(tabPanel + ' .qbox').val('');
		getColumns(tableName, tabPanel);
	});
	
	$(tabPanel + ' .q-rowcount-btn').click(function(){
		var tableName = $(tabPanel + ' .table-select').val();
		var sql = 'SELECT COUNT(*) AS "COUNT" FROM ' + tableName;
		$(tabPanel + ' .qbox').val(sql);
		$(tabPanel + ' .exec-sql-form').trigger('submit');
	});
	
	$(tabPanel + ' .q-alldata-btn').click(function(){
		var tableName = $(tabPanel + ' .table-select').val();
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
	
	$(tabPanel + ' .conn-info-btn').click(function(){

	  	$.get("/dbold/info", {connId: $(tabPanel).data("connId")}, function(result){

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
		loadSchemaView(tabPanel, true);
	});

//	$('.nav-sidebar li a').click(function(){
//        $('#table-name').html($(this).html());
//	});
	
	
}

function getColumns(tableName, tabPanel) {

	$(tabPanel + ' .dynamic-table').html(tableLoaderHtml);
	var params = {
		connId: $(tabPanel).data("connId"),
		table: tableName
	};
  	$.get("/dbold/columns", params, function(result){
  		result = JSON.parse(result);
  		updateDynamicTable(result, tabPanel);
  	});
}

function executeQuery(sql, tabPanel) {

	$(tabPanel + ' .dynamic-table').html(tableLoaderHtml);
	var connId = $(tabPanel).data("connId");
	var params = {
		connId: $(tabPanel).data("connId"),
		q: sql
	};
  	$.get("/dbold/query", params, function(result){
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
