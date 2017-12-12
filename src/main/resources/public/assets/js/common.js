$(document).ready(function() {

    $.ajaxSetup({
        error: function (x, status, error) {
            if (x.status == 403 || x.status == 401) {
                window.location.replace("/user/login");
            }
        }
    });
    
	$('#logout-btn').click(function() {
		window.location.replace('/user/logout');
	});
});

function hasOverflow(element) {
	return (element.offsetHeight < element.scrollHeight 
			|| element.offsetWidth < element.scrollWidth);
}

function objectifyForm(formArray) {// serialize data function

	var returnArray = {};
	for (var i = 0; i < formArray.length; i++) {
		returnArray[formArray[i]['name']] = formArray[i]['value'];
	}
	return returnArray;
}