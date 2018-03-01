package com.delfino.adaptor;

public interface Adaptor<I,O> {

	public O convert(I input) throws Exception;

}
