
$(document).ready(function() {

	if (typeof dbtree_data !== 'undefined') {
		$('#dbtree').treeview({
			data: dbtree_data,
			enableLinks: true,
			selectedBackColor: '#5bc0de'
		});
	}
});
