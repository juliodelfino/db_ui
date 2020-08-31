
var tableLoaderHtml = '<div id="loader-div"><img class="center-block" src="/assets/images/loader1-small.gif"/>'
    + '<input type="button" class="center-block btn btn-sm btn-info" id="cancel-query-btn" value="Cancel"/>'
    + '<input type="hidden" id="query-id" value=""/>'
    + '</div>';
var dbConnId = null;
var catalogName = null;
var schemaName = null;

$(document).ready(function(){

	var urlParams = getUrlVars();
	dbConnId = urlParams["id"];
	catalogName = urlParams["catalog"];
	schemaName = urlParams["schema"];

    initEditorActions('div');

	$('.qbox').keydown(function (e) {

		  if (e.ctrlKey) {
			  if (e.keyCode == 13) {
		    // Ctrl-Enter pressed
				 $('.exec-sql-form').trigger('submit');
			  }
			  else if (e.keyCode == 69) {
				 $('#exec-sel-btn').trigger('click');
			  }
		  }
		});

	$( ".exec-btn" ).tooltip();
	$( "#exec-sel-btn" ).tooltip();

	$("#share-btn").click(function(){

		copyToClipboard(getUrlToShare());
	});
});

function initEditorActions(tabPanel) {

	$(tabPanel + ' .exec-sql-form').submit(function(e){
        e.preventDefault();
        console.log('submitting');
		executeQuery($(tabPanel + ' .qbox').val(), dbConnId, tabPanel);
	});

	$(tabPanel + ' .refresh-dbconn-btn').click(function(){
		loadSchemaView(tabPanel, true);
	});

//	$('.nav-sidebar li a').click(function(){
//        $('#table-name').html($(this).html());
//	});

}

function executeQuery(sql, dbConnectionId, tabPanel) {

	var queryId = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
	$(tabPanel + ' .dynamic-table').html(tableLoaderHtml);
	$('#loader-div #query-id').val(queryId);
	$('#cancel-query-btn').click(onCancelQuery);
	var params = {
		connId: dbConnectionId,
		catalog: catalogName,
		schema: schemaName,
		q: sql,
		qId: queryId
	};
	var startTime = new Date().getTime();
  	$("#exec-time-info").text("");
  	$.get("/table/query", params, function(result){
  		result = JSON.parse(result);
  		if (result.error) {
  			displayError(result, tabPanel);
  		} else if (!result.data) {
  			displayInfo(result, tabPanel);
  		}
  		else {
  		    var requestTime = new Date().getTime() - startTime;
  		    $("#exec-time-info").text("Query took " + requestTime/1000 + " seconds");
  	  		updateDynamicTable(result, tabPanel);
  		}
  	});
}

function onCancelQuery() {
    var queryId = $('#loader-div #query-id').val();
	var params = {
	    connId: dbConnId,
		qId: queryId
	};
    $.get("/table/cancelquery", params, function(result){

    });
}

function updateDynamicTable(result, tabPanel) {

	var tableHeaders = '';
	$.each(result.columns, function(i, val) {
	    tableHeaders = tableHeaders + '<th>' + val.title + '</th>';
	});

	$(tabPanel + ' .dynamic-table').html(
			'<table class="hover"><thead><tr>' + tableHeaders + '</tr></thead></table>');

	result.columns[0].render = function(data, type, row, meta) {
		return '<button class="btn btn-xs btn-primary id-btn">' + data + '</button>';
	};
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

	//enable display of Row details on double-clicking the row
//	$(tabPanel + ' .dynamic-table tbody').on('dblclick', 'tr', function(){
//
//		rowData = table.row( this ).data();
//		displayRowDataDialog(rowData);
//	});

	$(tabPanel + ' .dynamic-table tbody').on('click', '.id-btn', function(){

		rowData = table.row( $(this).parent() ).data();
		displayRowDataDialog(rowData);
	});
}

function displayError(result, tabPanel) {

	$(tabPanel + ' .dynamic-table').html(
			'<div class="col-lg-12 alert alert-danger">' + result.message + '</div>');
}

function displayInfo(result, tabPanel) {

	$(tabPanel + ' .dynamic-table').html(
			'<div class="col-lg-12 alert alert-info">' + result.message + '</div>');
}

function getColumns(tableName, tabPanel) {

	$(tabPanel + ' .dynamic-table').html(tableLoaderHtml);
	var params = {
		connId: dbConnId,
		table: tableName,
		catalog: catalogName
	};
  	$.get("/table/columns", params, function(result){
  		result = JSON.parse(result);
  		updateDynamicTable(result, tabPanel);
  	});
}

function displayRowDataDialog(rowData) {

	var rowCols = table.settings().init().columns;
	var div = $("<div>", {id: "somevalue", "class": "form-horizontal"});
	for (i = 0; i < rowData.length; i++) {

		var isMultiline = rowData[i] == null || typeof rowData[i] != "string" ?
				false : (rowData[i].indexOf("\n") > -1);
		var longText = rowData[i] == null || typeof rowData[i] != "string" ?
				false : rowData[i].length > 255;
		var dlLink = rowCols[i].blob || isMultiline || longText? "<a href='javascript:downloadData("
				+ i + ", " + rowCols[i].blob + ");'>Download data</a>" : "";

		var readonly = rowCols[i].primaryKey ? "readonly='readonly'" : "";
		var cellData = rowData[i] == null ? "" : rowData[i];
		div.append("<div class='form-group'><label class='control-label col-xs-3'>"
			+ rowCols[i].title + "</label><div class='col-xs-9'><input type='text' "
			+ " class='form-control' " + readonly + " value='"
			+ cellData + "' />" + dlLink + "</div></div>");
	}
	$('#myModal .modal-body').html(div);
	$('#myModal').modal('show');
	$('#myModal').on('shown.bs.modal', function () {
	    $('#myModal').find('input:text').first().focus();
	});
}