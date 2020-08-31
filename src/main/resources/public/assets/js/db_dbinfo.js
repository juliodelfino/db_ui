
$(document).ready(function() {

    $.getScript('/assets/js/sql_editor.js');
    $( "#tabs" ).tabs();
//	initTableActions('div');
});


function initTableActions(tabPanel) {

	$(tabPanel + ' #sql-query-btn').click(function(){
        alert("sql query");
	});
}
