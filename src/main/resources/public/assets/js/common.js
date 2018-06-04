var treeIconMap = {
		"DBCONN" : "glyphicon glyphicon-briefcase",
		"CATALOG" : "glyphicon glyphicon-book",
		"SCHEMA" : "glyphicon glyphicon-user",
		"TABLE" : "glyphicon glyphicon-th-list"
};

var treeHrefMap = {
		"DBCONN" : "/db/dbconninfo?",
		"CATALOG" : "/db/dbinfo?",
		"SCHEMA" : "/db/dbinfo?",
		"TABLE" : "/table?"
};

$(document).ready(function() {

    $.ajaxSetup({
        error: function (x, status, error) {
            if (x.status == 403 || x.status == 401) {
                window.location.replace("/user/login");
            } else if (x.status == 404 || x.status == 500) {
            	alert("Error " + x.status + ": " + x.responseText);
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

function recomputeTreeModel(treeModel, parentNode) {
	
	for (var idx in treeModel) {
		
		var parent = treeModel[idx];
		parent.icon = treeIconMap[parent.type];
		//temporarily link the parentNode to its child for http link generation
		parent.parent = parentNode;
		parent.href = createLink(parent);
		if (typeof parent.state !== 'undefined' && parent.state.selected) {
			parent.state.expanded = true;
			var tmpParent = parent.parent;
			while (tmpParent != null) {
				tmpParent.state.selected = true;
				tmpParent.state.expanded = true;
				tmpParent = tmpParent.parent;
			}
		}
		if (typeof parent.nodes !== 'undefined') {
			recomputeTreeModel(parent.nodes, parent);
		}
		//now remove the parentNode link to avoid stackoverflow error caused by 
		//treeview javascript library
		delete parent.parent;
	}
	return treeModel;
}

function createLink(node) {
	
	var url = "";
	var tmpNode = node;
	while (tmpNode != null) {
		
		if (typeof tmpNode.id === 'undefined') {
			tmpNode.id = "";
		}
		if (tmpNode.type == "TABLE") {
			url = "&table=" + tmpNode.id + url;	
		}	
		else if (tmpNode.type == "CATALOG") {
			url = "&catalog=" + tmpNode.id + url;
		}	
		else if (tmpNode.type == "SCHEMA") {
			url = "&schema=" + tmpNode.id + url;
		}	
		else if (tmpNode.type == "DBCONN") {
			url = "id=" + tmpNode.id + url;
		}
		tmpNode = tmpNode.parent;
	}
	if (treeHrefMap[node.type] != null) {
		url = treeHrefMap[node.type] + url;
	}
	console.log(url);
	return url;	
}
