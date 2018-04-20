package com.delfino.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public TreeNode(String id, String text, TreeNode parent) {
		setText(text);
		this.id = id;
		state = new HashMap();
		state.put("expanded", false);
		if (parent == null) {
			setHref("/db/dbinfo?id=" + id);
			setIcon("glyphicon glyphicon-briefcase");
		} else {
			setHref("/table?id=" + parent.id + "&table=" + text);
			setIcon("glyphicon glyphicon-th-list");
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
