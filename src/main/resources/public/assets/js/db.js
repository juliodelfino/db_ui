
$(document).ready(function() {

	if (typeof dbtree_data !== 'undefined') {
		$('#dbtree').treeview({
			data: dbtree_data,
			enableLinks: true,
			selectedBackColor: '#5bc0de'
		});
	}
	
	$("#splitpanel").height('100vh').split({
		orientation: 'vertical',limit:50, position: '15%'
	});
});
