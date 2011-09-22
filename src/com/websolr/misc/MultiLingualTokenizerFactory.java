package com.websolr.misc;

import java.io.Reader;

import org.apache.solr.analysis.BaseTokenizerFactory;

public class MultiLingualTokenizerFactory extends BaseTokenizerFactory {
	public MultiLingualTokenizer create(Reader in) {
		return new MultiLingualTokenizer(in);
	}
}