//TO DELETE
var databases = null;
var connLoaderHtml = '<div><img class="center-block" src="/assets/images/fluid-loader.gif"/></div>';
var tableLoaderHtml = '<div><img class="center-block" src="/assets/images/loader1.gif"/></div>';
//TO DELETE - END

var dbConnId = null;
var catalogName = null;
var rowData = null;

$(document).ready(function() {

	dbConnId = getUrlVars()["id"];
	catalogName = getUrlVars()["catalog"];
	initTableActions('div');
	$('#modal-title').html($('#db-table-name').text() + ' - Row Details');
	
	$('#dbtree').treeview({
		data: dbtree_data,
		enableLinks: true,
		selectedBackColor: '#5bc0de'
	});
	
	$('.q-alldata-btn').trigger('click');
	
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
	
	initQueryHistoryDialog();
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

	var tableName = $('#db-table-name').text();
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
		var sql = 'SELECT * FROM ' + tableName + ' LIMIT 30';
		$(tabPanel + ' .qbox').val(sql);
		$(tabPanel + ' .exec-sql-form').trigger('submit');
	});

	
	$('#exec-sel-btn').click(function(e){
		executeQuery(getSelectedText('qbox'), tabPanel);
	});
	
	$(tabPanel + ' .exec-sql-form').submit(function(e){
        e.preventDefault();
        console.log('submitting');
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
		catalog: catalogName,
		q: sql
	};
  	$.get("/table/query", params, function(result){
  		result = JSON.parse(result);
  		if (result.error) {
  			displayError(result, tabPanel);
  		} else if (!result.data) {
  			displayInfo(result, tabPanel);
  		}
  		else {
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
			'<table class="hover"><thead><tr>' + tableHeaders + '</tr></thead></table>');

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
	
	$(tabPanel + ' .dynamic-table tbody').on('dblclick', 'tr', function(){

		var urlVars = getUrlVars();
		rowData = table.row( this ).data();
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
		})  
	});
}

function initQueryHistoryDialog() {
	$("#query-history-btn").click(function(){
		$.get('/table/queryhistory', function(result) {
	    	var rowData = JSON.parse(result);

			$('#query-history-dialog .modal-body').html("");

			for (i = 0; i < rowData.length; i++) {
				
				$('#query-history-dialog .modal-body').append(
					"<a href='#' class='list-group-item'>"
					+ "<span class='timestamp' style='display: none'>"
					+ rowData[i].timestamp + "</span>&nbsp;<span class='log'>"
					+rowData[i].log + "</span><button type='button btn-xs' class='delete-log close'>x</button></a>");
			}
	    	
	    });
		$('#query-history-dialog').modal('show');
	});
	
	$("#query-history-dialog .modal-body").on('click', '.delete-log', function(e){
		e.preventDefault();
		
		var parent = $(this).closest('a');
		var timestamp = parent.find('.timestamp').text();
		$.ajax({
		    url: '/table/queryhistory?t=' + timestamp,
		    type: 'DELETE',
		    success: function(result) {
		    	if (JSON.parse(result)) {
			    	parent.remove();
		    	} else {
		    		alert("Error removing the item. Please refresh this page.");
		    	}
		    }
		});
	});	
	
	$("#query-history-dialog .modal-body").on('dblclick', '.list-group-item', function(){
		var log = $(this).find('.log').text();
		$('.qbox').val(function(i, text) {
		    return text + log + "; ";
		});
		$('#query-history-dialog').modal('hide');
	});
	
	$('#query-history-dialog #show-time-checkbox').change(function() {
		if (this.checked) {
			$('.timestamp').show();
		} else {
			$('.timestamp').hide();
		}
	});
	
	$('#query-history-dialog #select-btn').click(function(e) {
		e.preventDefault();
		var items = document.getElementsByClassName("list-group-item active");
		if (items.length > 0) {
			$(items[0]).trigger('dblclick');
		}
		$('#query-history-dialog').modal('hide');
	});
	
	$("#query-history-dialog .modal-body").on('click', 'a', function(e){
		e.preventDefault();
	   $("#query-history-dialog .list-group a").removeClass("active");
	   $(this).closest('a').addClass("active");
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

function downloadData(colId, isBinary) {
    var element = document.createElement('a');
    //var isBinary = isBase64(rowData[colId]);
    var charset = isBinary ? "application/octet-stream;base64" : "";
    var dlData = isBinary ? rowData[colId] : decodeEntities(rowData[colId]);
    element.setAttribute('href', 'data:' + charset + ',' + dlData);
    element.setAttribute('download', 'data.tmp');

    element.style.display = 'none';
    document.body.appendChild(element);

    element.click();

    document.body.removeChild(element);
}

/*
function isBase64(text) {
    try {
        window.atob(text);
    } catch(e) {
        return false;
    }
    return true;
}
*/

function decodeEntities(encodedString) {
    var textArea = document.createElement('textarea');
    textArea.innerHTML = encodedString;
    var decodedText = textArea.value;
    textArea.remove();
    return decodedText;
}

//source: https://stackoverflow.com/questions/717224/how-to-get-selected-text-in-textarea?noredirect=1&lq=1
function getSelectedText(textAreaId) // javascript
{
    // obtain the object reference for the <textarea>
    var txtarea = document.getElementById(textAreaId);
    // obtain the index of the first selected character
    var start = txtarea.selectionStart;
    // obtain the index of the last selected character
    var finish = txtarea.selectionEnd;
    // obtain the selected text
    return start == finish ? txtarea.value : txtarea.value.substring(start, finish);
    // do something with the selected content
}
