package com.delfino.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.delfino.util.Constants.TreeNodeType;

/**
 * Model class for jonmiles/bootstrap-treeview nodes.
 * @author julio
 *
 */
public class TreeNode {

	String id;
	String text;
	String href;
	String icon;
	List<TreeNode> nodes;
	Map<String, Boolean> state;
	
	public TreeNode(String connId, String catalogName, String tableName, String text, TreeNodeType type) {
		setText(text);
		state = new HashMap();
		state.put("expanded", false);
		if (type == TreeNodeType.DBCONN) {
			this.id = connId;
			setHref("/db/dbconninfo?id=" + id);
			setIcon("glyphicon glyphicon-briefcase");
		} else if (type == TreeNodeType.CATALOG) {
			this.id = connId + catalogName;
			setHref("/db/dbinfo?id=" + connId + "&catalog=" + catalogName);
			setIcon("glyphicon glyphicon-book");
		}
		else if (type == TreeNodeType.TABLE) {
			this.id = connId + catalogName + tableName;
			setHref("/table?id=" + connId + "&catalog=" + catalogName + "&table=" + tableName);
			setIcon("glyphicon glyphicon-th-list");
		}
		else {
			throw new IllegalStateException("Invalid TreeNodeType: " + type);
		}
	}
	
	private void setIcon(String iconCss) {
		this.icon = iconCss;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public List<TreeNode> getNodes() {
		return nodes;
	}
	public void setNodes(List<TreeNode> nodes) {
		this.nodes = nodes;
	}
	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
	
	public static enum NodeType {
		DB, TABLE
	}

	public void setState(String prop, boolean value) {
		state.put(prop, value);
	}
}
