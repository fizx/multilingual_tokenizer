package com.websolr.misc;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;

public final class MultiLingualTokenizer extends Tokenizer {

	private static final Set<Character.UnicodeBlock> CJK = new HashSet<Character.UnicodeBlock>(
			Arrays.asList(new Character.UnicodeBlock[] {
					Character.UnicodeBlock.CJK_COMPATIBILITY,
					Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS,
					Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
					Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT,
					Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION,
					Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
					Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
					Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO,
					Character.UnicodeBlock.HANGUL_JAMO,
					Character.UnicodeBlock.HANGUL_SYLLABLES,
					Character.UnicodeBlock.HIRAGANA }));

	public MultiLingualTokenizer(Reader in) {
		super(in);
		init();
	}

	public MultiLingualTokenizer(AttributeSource source, Reader in) {
		super(source, in);
		init();
	}

	public MultiLingualTokenizer(AttributeFactory factory, Reader in) {
		super(factory, in);
		init();
	}

	private void init() {
		termAtt = (TermAttribute) addAttribute(TermAttribute.class);
		offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
	}

	private int offset = 0, bufferIndex = 0, dataLen = 0;
	private final static int MAX_WORD_LEN = 255;
	private final static int IO_BUFFER_SIZE = 1024;
	private final char[] buffer = new char[MAX_WORD_LEN];
	private final char[] ioBuffer = new char[IO_BUFFER_SIZE];

	private int length;
	private int start;

	private TermAttribute termAtt;
	private OffsetAttribute offsetAtt;

	private final void push(char c) {

		if (length == 0)
			start = offset - 1; // start of token
		buffer[length++] = Character.toLowerCase(c); // buffer it

	}

	private final boolean flush() {

		if (length > 0) {
			// System.out.println(new String(buffer, 0,
			// length));
			termAtt.setTermBuffer(buffer, 0, length);
			offsetAtt.setOffset(correctOffset(start), correctOffset(start
					+ length));
			return true;
		} else
			return false;
	}

	public boolean incrementToken() throws IOException {
		clearAttributes();

		length = 0;
		start = offset;

		while (true) {

			final char c;
			offset++;

			if (bufferIndex >= dataLen) {
				dataLen = input.read(ioBuffer);
				bufferIndex = 0;
			}

			if (dataLen == -1)
				return flush();
			else
				c = ioBuffer[bufferIndex++];

			switch (Character.getType(c)) {

			case Character.DECIMAL_DIGIT_NUMBER:
			case Character.LOWERCASE_LETTER:
			case Character.UPPERCASE_LETTER:
				push(c);
				if (length == MAX_WORD_LEN)
					return flush();
				break;

			case Character.OTHER_LETTER:
				if (CJK.contains(Character.UnicodeBlock.of(c))) {
					if (length > 0) {
						bufferIndex--;
						offset--;
						return flush();
					}
					push(c);
					return flush();
				} else {
					push(c);
					if (length == MAX_WORD_LEN)
						return flush();
				}
				break;
			default:
				if (length > 0)
					return flush();
				break;
			}
		}
	}

	public final void end() {
		// set final offset
		final int finalOffset = offset;
		this.offsetAtt.setOffset(finalOffset, finalOffset);
	}

	public void reset() throws IOException {
		super.reset();
		offset = bufferIndex = dataLen = 0;
	}

	public void reset(Reader input) throws IOException {
		super.reset(input);
		reset();
	}
}
