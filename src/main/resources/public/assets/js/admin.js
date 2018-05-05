var newUserMode = true;
var newDbMode = true;
var dbsTable = null;

$(document).ready(function() {

	dbsTable = $('#dbs-table').DataTable({
        "bPaginate": false,
        "bFilter": false,
        ajax: '/admin/dblist',
        rowId: 'connId',
        initComplete: initDbTableActions,
        "autoWidth": false,
		dom: 'Bfrtip',
		"columns": [{"data":"connectionName"},
		           {"data":"url"},
		           {"data":"users"},
		           {"data":null, "defaultContent":'<span ' +
					'class="btn btn-xs btn-default edit-db-btn pull-right"> <i ' +
					'class="glyphicon glyphicon-pencil"></i> ' +
				'</span>' +
				'<span ' +
					'class="btn btn-xs btn-default delete-db-btn pull-right"> <i ' +
					'class="glyphicon glyphicon-remove"></i> ' +
				'</span>'}],
        buttons: [
                  {
                      text: 'New DB connection',
                      action: function ( e, dt, node, config ) {
                    	  $('#db-info-form').trigger('reset');
                  		  $('#db-info-dialog .modal-title').html('Create new database connection');
                          $('#db-info-dialog').modal('show');
                          newDbMode = true;
                      }
                  },
                  {
                      text: 'Reload',
                      action: function ( e, dt, node, config ) {
                    	  $('#dbs-table').DataTable().ajax.reload(initDbTableActions);
                      }
                  }
              ] });
	
	$('#users-table').DataTable({
        "bPaginate": false,
        "bFilter": false,
        ajax: '/admin/userlist',
        rowId: 'username',
        initComplete: initUserTableActions,
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
                    	  $('#users-table').DataTable().ajax.reload(initUserTableActions);
                      }
                  }
                ]});
	
	$('#dbs-table tbody').on('dblclick', 'tr', function(){

		var dbInfo = dbsTable.row( this ).data();
		showDbInfoDialog(dbInfo);
	});
	$('#users-table tbody').on('dblclick', 'tr', showUserInfoDialog);
	
	$('#db-submit-btn').click(onDbSubmit);
	$('#user-submit-btn').click(onUserSubmit);
	
	$("#user-options input[type=checkbox]").each(function () {
	    $(this).change(function(){
	    	$('#user-count').text($("#user-options input[type=checkbox]:checked").size());
	    });
	});
	
	$("#db-options input[type=checkbox]").each(function () {
	    $(this).change(function(){
	    	$('#db-count').text($("#db-options input[type=checkbox]:checked").size());
	    });
	});
});

function initDbTableActions() {	
	
	$('.edit-db-btn').click(function(){

		var dbInfo = dbsTable.row( $(this).closest('tr') ).data();
		showDbInfoDialog(dbInfo);
	});
	
	$('.delete-db-btn').click(function(){
		connId = $(this).closest('tr').prop('id');
		var confirmDelete = confirm("Deleting this database " + connId + " cannot be undone. Click OK to continue.");
		if (confirmDelete) {
			$.ajax({
			    url: '/db/info?connId=' + connId,
			    type: 'DELETE',
			    success: function(result) {
			    	if (JSON.parse(result)) {
			    		dbsTable.ajax.reload(initDbTableActions);
			    	}
			    }
			});
		}
	});
}

function initUserTableActions() {	
	
	$('.edit-user-btn').click(showUserInfoDialog);
	
	$('.delete-user-btn').click(function(){
		userId = $(this).closest('tr').prop('id');
		var confirmDelete = confirm("Deleting this user " + userId + " cannot be undone. Click OK to continue.");
		if (confirmDelete) {
			$.ajax({
			    url: '/user/info?username=' + userId,
			    type: 'DELETE',
			    success: function(result) {
			    	if (JSON.parse(result)) {
			    		$('#users-table').DataTable().ajax.reload(initUserTableActions);
			    	}
			    }
			});
		}
	});
}

function showDbInfoDialog(dbInfo) {

    $.each(dbInfo, function(prop, value){
        $("#db-info-dialog input[name='" + prop + "']").val(value);
     });
    
	$('#user-options input').prop('checked', false);
	$('#user-count').text(dbInfo.users.length);
	$.each(dbInfo.users, function(i, val){
		$('#user-options #user-' + val).prop('checked', true);
	});
	
	$('#db-info-dialog .modal-title').html('Update database connection');
	$('#db-info-dialog').modal('show');
	
	newDbMode = false;
}

function showUserInfoDialog() {

	var userId = $(this).closest('tr').prop('id');	
  	$.get("/user/info", { username: userId}, function(result){
  		
  		result = JSON.parse(result);
  		var user = result.user;
  		$('#user-info-dialog input[name=username]').val(user.username);
  		$('#user-info-dialog input[name=password]').val("");
  		$('#user-info-dialog input[name=fullName]').val(user.fullName);
  		$('#user-info-dialog input[name=admin]').prop('checked', user.admin);
  		$('#db-options input').prop('checked', false);
  		$('#db-count').text(result.dbList.length);
  		$.each(result.dbList, function(i, val){
  			$('#db-options #db-' + val).prop('checked', true);
  		});
  		
  		$('#user-info-dialog .modal-title').html('Update user');
  		$('#user-info-dialog').modal('show');
  		
		newUserMode = false;
  	});
}

function onDbSubmit(e){
	e.preventDefault();
	if (!$('#db-info-form')[0].checkValidity()) {
		$('#db-info-form')[0].reportValidity();
		return false;
	}
	var dbInfoParams = $('#db-info-form').serialize();
	var userOpts = [];
	$('#user-options input:checked').each(function(){
		userOpts.push($(this).val());
	});
	var params = dbInfoParams + "&users=" + userOpts.join("+");
	var dbUrl = newDbMode ? "/db/connectdb" : "/db/infoupdate";
	$.post(dbUrl, params, function(result){
		result = JSON.parse(result);
		if (result.error) {
  			$("#db-info-dialog .alert-message").html(result.message);
  			$("#db-info-dialog .alert-danger").show();
		} else {
			$('#db-info-dialog').modal('hide');
			dbsTable.ajax.reload(initDbTableActions);
		}
	});
}

function onUserSubmit(e){
	e.preventDefault();
	if (!$('#user-info-form')[0].checkValidity()) {
		$('#user-info-form')[0].reportValidity();
		return false;
	}
	var userInfoParams = $('#user-info-form').serialize();
	var dbOpts = [];
	$('#db-options input:checked').each(function(){
		dbOpts.push($(this).val());
	});
	var params = userInfoParams + "&dbaccess=" + dbOpts.join("+");
	var userUrl = newUserMode ? "/user/new" : "/user/info";
	$.post(userUrl, params, function(result){
		$('#user-info-dialog').modal('hide');
		$('#users-table').DataTable().ajax.reload(initUserTableActions);
	});
}