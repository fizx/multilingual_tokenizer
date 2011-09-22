package com.websolr.misc;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import junit.framework.TestCase;

public class MultiLingualTokenizerTest extends TestCase {
	private StringReader reader;
	private String text;
	private MultiLingualTokenizer tokenizer;

	public void setUp() {
		text = "巴士阿叔 hello world look arabic: لوحة المفاتيح";
		reader = new StringReader(text);
		tokenizer = new MultiLingualTokenizer(reader);
	}

	public void testTokenization() throws IOException {
		String[] values = new String[] { "巴", "士", "阿", "叔", "hello", "world",
				"look", "arabic", "لوحة", "المفاتيح" };
		for (int i = 0; i < values.length; i++) {
			tokenizer.incrementToken();
			assertEquals(values[i], ((TermAttribute) tokenizer
					.getAttribute(TermAttribute.class)).term());
		}
	}
}
