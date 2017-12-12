package com.delfino.adaptor;

public interface Adaptor<I,O> {

	public O convert(I input) throws Exception;

    
    class Column {
    	String title;
    	
    	public Column(String title) {
    		this.title = title;
    	}
    }
}
