var newUserMode = true;

$(document).ready(function() {

	$('#dbs-table').DataTable({
        "bPaginate": false,
        "bFilter": false,
		dom: 'Bfrtip',
        buttons: [
                  {
                      text: 'New DB connection',
                      action: function ( e, dt, node, config ) {
                          alert( 'Button activated' );
                      }
                  }
              ] });
	
	$('#users-table').DataTable({
        "bPaginate": false,
        "bFilter": false,
        ajax: '/user/list',
        rowId: 'username',
        initComplete: initTableActions,
        "autoWidth": false,
		dom: 'Bfrtip',
		"columns":[{"data":"username"},
		           {"data":"fullName"},
		           {"data":"admin"},
		           {"data":null, "defaultContent":'<span ' +
					'class="btn btn-xs btn-default edit-user-btn pull-right"> <i ' +
					'class="glyphicon glyphicon-pencil"></i> ' +
				'</span>' +
				'<span ' +
					'class="btn btn-xs btn-default delete-user-btn pull-right"> <i ' +
					'class="glyphicon glyphicon-remove"></i> ' +
				'</span>'}],
        buttons: [
                  {
                      text: 'New user',
                      action: function ( e, dt, node, config ) {
                    	  $('#user-info-form').trigger('reset');
                  		  $('#user-info-dialog .modal-title').html('Create new user');
                          $('#user-info-dialog').modal('show');
                          newUserMode = true;
                      }
                  },
                  {
                      text: 'Reload',
                      action: function ( e, dt, node, config ) {
                    	  $('#users-table').DataTable().ajax.reload(initTableActions);
                      }
                  }
                ]});
	
	$('#user-submit-btn').click(function(e){
		e.preventDefault();
		var userInfoParams = $('#user-info-form').serialize();
		var dbOpts = [];
		$('#db-options input:checked').each(function(){
			dbOpts.push($(this).val());
		});
		var params = userInfoParams + "&dbaccess=" + dbOpts.join("+");
		var userUrl = newUserMode ? "/user/new" : "/user/info";
		$.post(userUrl, params, function(result){
			$('#user-info-dialog').modal('hide');
			$('#users-table').DataTable().ajax.reload(initTableActions);
		});
	});
});

function initTableActions() {

	$('.edit-user-btn').click(function(){
		userId = $(this).closest('tr').prop('id');
		
	  	$.get("/user/info", { username: userId}, function(result){
	  		
	  		result = JSON.parse(result);
	  		user = result.user;
	  		$('#user-info-dialog input[name=username]').val(user.username);
	  		$('#user-info-dialog input[name=fullName]').val(user.fullName);
	  		$('#user-info-dialog input[name=admin]').prop('checked', user.admin);
  			$('#db-options input').prop('checked', false);
  			$.each(result.dbList, function(i, val){
  				$('#db-options #db-' + val).prop('checked', true);
  			});
			
			$('#user-info-dialog .modal-title').html('Update user');
			$('#user-info-dialog').modal('show');
			newUserMode = false;
	  	});
	});
	
	$('.delete-user-btn').click(function(){
		userId = $(this).closest('tr').prop('id');
		var confirmDelete = confirm("Deleting this user " + userId + " cannot be undone. Click OK to continue.");
		if (confirmDelete) {
			$.ajax({
			    url: '/user/info?username=' + userId,
			    type: 'DELETE',
			    success: function(result) {
			    	if (JSON.parse(result)) {
			    		$('#users-table').DataTable().ajax.reload(initTableActions);
			    	}
			    }
			});
		}
	});
}