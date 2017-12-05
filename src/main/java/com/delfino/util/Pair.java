package com.delfino.util;

import java.util.Map.Entry;

public class Pair<K, V> implements Entry<K, V> {

	private K k;
	private V v;
	
	public Pair(K k, V v) {
		this.k = k;
		this.v = v;
	}
	
	@Override
	public K getKey() {
		return k;
	}

	@Override
	public V getValue() {
		return v;
	}

	@Override
	public V setValue(V value) {
		this.v = value;
		return value;
	}

}
