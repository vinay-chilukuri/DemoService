
package com.ps.DemoService;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Random;

import org.apache.http.HttpEntity;

import com.ps.DemoService.TestUtilRuntimeException;

/**
 * @author SAP AG
 */
public class StringHelper {

	public static String inputStreamToString(final InputStream in, final boolean preserveLineBreaks)
			throws IOException {
		final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
		final StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		int i=1;
		while ((line = bufferedReader.readLine()) != null) {

			if (line.contentEquals(new StringBuilder("--batch_36522ad7-fc75-4b56-8c71-56071383e77b")) && (i!=1)) {
				stringBuilder.append("\n");
				stringBuilder.append("\n");
			}
			stringBuilder.append(line);
			if (preserveLineBreaks) {
				stringBuilder.append("\n");
			}
			i++;
		}

		bufferedReader.close();

		final String result = stringBuilder.toString();

		return result;
	}

	public static String inputStreamToString(final InputStream in) throws IOException {
		return inputStreamToString(in, false);
	}

	public static String httpEntityToString(final HttpEntity entity) throws IOException, IllegalStateException {
		return inputStreamToString(entity.getContent());
	}

	/**
	 * Encapsulate given content in an {@link InputStream} with charset
	 * <code>UTF-8</code>.
	 * 
	 * @param content to encapsulate content
	 * @return content as stream
	 */
	public static InputStream encapsulate(final String content) {
		try {
			return encapsulate(content, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// we know that UTF-8 is supported
			throw new TestUtilRuntimeException("UTF-8 MUST be supported.", e);
		}
	}

	/**
	 * Encapsulate given content in an {@link InputStream} with given charset.
	 * 
	 * @param content to encapsulate content
	 * @param charset to be used charset
	 * @return content as stream
	 * @throws UnsupportedEncodingException if charset is not supported
	 */
	public static InputStream encapsulate(final String content, final String charset)
			throws UnsupportedEncodingException {
		return new ByteArrayInputStream(content.getBytes(charset));
	}

	/**
	 * Generate a string with given length containing random upper case characters
	 * ([A-Z]).
	 * 
	 * @param len length of to generated string
	 * @return random upper case characters ([A-Z]).
	 */
	public static InputStream generateDataStream(final int len) {
		return encapsulate(generateData(len));
	}

	/**
	 * Generates a string with given length containing random upper case characters
	 * ([A-Z]).
	 * 
	 * @param len length of the generated string
	 * @return random upper case characters ([A-Z])
	 */
	public static String generateData(final int len) {
		Random random = new Random();
		StringBuilder b = new StringBuilder(len);
		for (int j = 0; j < len; j++) {
			final char c = (char) ('A' + random.nextInt('Z' - 'A' + 1));
			b.append(c);
		}
		return b.toString();
	}

}