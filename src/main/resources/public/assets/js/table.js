//TO DELETE
var databases = null;
var connLoaderHtml = '<div><img class="center-block" src="/assets/images/fluid-loader.gif"/></div>';
//TO DELETE - END


var rowData = null;

$(document).ready(function() {

    $.getScript('/assets/js/sql_editor.js');

	var urlParams = getUrlVars();
	var queryText = urlParams["q"];
	initTableActions('div');
	$('#modal-title').html($('#db-table-name').text() + ' - Row Details');

	dbtree_data = recomputeTreeModel(dbtree_data);
	$('#dbtree').treeview({
		data: dbtree_data,
		enableLinks: true,
		selectedBackColor: '#5bc0de'
	});

	if (typeof queryText === 'undefined') {
		$('.q-alldata-btn').trigger('click');
	} else {
		$('.qbox').val(decodeURIComponent(queryText));
		$('.exec-sql-form').trigger('submit');
	}

	initQueryHistoryDialog();

	$("#splitpanel").height(930).split({
		orientation: 'vertical',limit:50, position: '15%'
	});
});

function getUrlToShare()
{
	var url = window.location.href.slice(0, window.location.href.indexOf('?') + 1);
	
    var vars = [], hash;   
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        if (hash[0] !== 'q') {
        	vars.push(hashes[i]);
        }
    }
   	vars.push('q=' + $('.qbox').val());
    return url + vars.join('&');
}

function initTableActions(tabPanel) {

	var tableName = $('#db-table-name').text();
	$(tabPanel + ' .q-columns-btn').click(function(){
		$(tabPanel + ' .qbox').val('');
		getColumns(tableName, tabPanel);
	});
	
	$(tabPanel + ' .q-rowcount-btn').click(function(){
		var sql = 'SELECT COUNT(*) AS ROWCOUNT FROM ' + tableName;
		$(tabPanel + ' .qbox').val(sql);
		$(tabPanel + ' .exec-sql-form').trigger('submit');
	});
	
	$(tabPanel + ' .q-alldata-btn').click(function(){
		var sql = 'SELECT * FROM ' + tableName + ' LIMIT 30';
		$(tabPanel + ' .qbox').val(sql);
	//	$(tabPanel + ' .exec-sql-form').trigger('submit');
	});

	
	$('#exec-sel-btn').click(function(e){
		executeQuery(getSelectedText('qbox'), dbConnId, tabPanel);
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
		    return text + log + " ";
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
