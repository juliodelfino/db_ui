$(document).ready(function() {
	$('#exec-btn').click(function(){

		executeQuery($('#qbox').val());
	});
	
});

function executeQuery(sql) {
	
	var dbtable_url = "/db/query?q=" + sql;
  	$.get(dbtable_url, function(result){
  		
  		result = JSON.parse(result);
  		table = $('#admin_tbl').DataTable({
  			
  			destroy: true,
			columns: result.columns,
			data: result.data
		});	
  	});
}