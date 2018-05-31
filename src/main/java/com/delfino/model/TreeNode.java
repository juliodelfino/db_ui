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
	List<TreeNode> nodes;
	TreeNodeType type;
	Map<String, Boolean> state;
	
	public TreeNode(String id, String text, TreeNodeType type) {
		setText(text);
		this.type = type;
		state = new HashMap();
		state.put("expanded", false);
		this.id = id;
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

	public void setState(String prop, boolean value) {
		state.put(prop, value);
	}
}
