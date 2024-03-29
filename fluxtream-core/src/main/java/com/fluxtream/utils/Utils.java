package com.fluxtream.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;

public class Utils {

	// private static Policy policy;
	//
	// @SuppressWarnings("deprecation")
	// public static String clean(String userContent) {
	// try {
	// if (policy==null)
	// policy =
	// Policy.getInstance(Utils.class.getResourceAsStream("/antisamy-ebay-1.4.4.xml"));
	//
	// AntiSamy as = new AntiSamy();
	// CleanResults cr;
	// cr = as.scan(userContent, policy);
	//
	// return cr.getCleanHTML();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return "YOUR CONTENT COULD NOT BE VERIFIED FOR CODE INJECTION";
	// }

	public static final String replaceLinks(String s) {
		StringBuffer result = new StringBuffer();
		String[] parts = s.split("\\s");

		// Attempt to convert each item into an URL.
		for (String item : parts)
			try {
				URL url = new URL(item);
				// If possible then replace with anchor...
				result.append("<a href=\"").append(url).append("\" target=\"_new\">").append(url).append("</a> ");
			} catch (MalformedURLException e) {
				// If there was an URL that was not it!...
				result.append(item).append(" ");
			}

		return result.toString();
	}

	public static final Map<String, String> parseParameters(String s) {
		StringTokenizer st = new StringTokenizer(s, "=&");
		Map<String, String> result = new HashMap<String, String>();
		while (st.hasMoreTokens()) {
			String key = st.nextToken();
			String val = st.nextToken();
			result.put(key, val);
		}
		return result;
	}

	public static String hash(String toHash) {
		byte[] uniqueKey = toHash.getBytes();
		byte[] hash = null;
		try {
			hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		StringBuilder hashString = new StringBuilder();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(hash[i]);
			if (hex.length() == 1) {
				hashString.append('0');
				hashString.append(hex.charAt(hex.length() - 1));
			} else
				hashString.append(hex.substring(hex.length() - 2));
		}
		return hashString.toString();
	}

	public static String sha1Hash(String toHash) throws NoSuchAlgorithmException {
		byte[] uniqueKey = toHash.getBytes();
		byte[] hash = null;
		hash = MessageDigest.getInstance("SHA1").digest(uniqueKey);
		StringBuilder hashString = new StringBuilder();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(hash[i]);
			if (hex.length() == 1) {
				hashString.append('0');
				hashString.append(hex.charAt(hex.length() - 1));
			} else
				hashString.append(hex.substring(hex.length() - 2));
		}
		return hashString.toString();
	}

	public static String shortStackTrace(Throwable t) {
		StringWriter writer = new StringWriter();
		t.printStackTrace(new PrintWriter(writer));
		String trace = writer.toString();
		return trace.length() < 64 ? trace : trace.substring(0, 64);
	}

	public static String mediumStackTrace(Throwable t) {
		StringWriter writer = new StringWriter();
		t.printStackTrace(new PrintWriter(writer));
		String trace = writer.toString();
		return trace.length() < 64 ? trace : trace.substring(0, 512);
	}

	public static String stackTrace(Throwable t) {
		StringWriter writer = new StringWriter();
		t.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	public static Integer safelyParseToInteger(Object source) {
		if (source == null) {
			return null;
		}
		if (source instanceof Integer) {
			return (Integer) source;
		}
		return Integer.parseInt(source + "");
	}

	public static Float safelyParseToFloat(Object source) {
		if (source == null) {
			return null;
		}
		if (source instanceof Float) {
			return (Float) source;
		}
		return Float.parseFloat(source + "");
	}

	public static Long safelyParseToLong(Object source) {
		if (source == null) {
			return null;
		}
		if (source instanceof Long) {
			return (Long) source;
		}
		return Long.parseLong(source + "");
	}

	public static ImmutableSortedMap<Integer, String> toSortedMap(Map<Integer, String> variables) {
		Ordering<Integer> valueComparator = Ordering.natural().onResultOf(Functions.forMap(variables));
		return ImmutableSortedMap.copyOf(variables, valueComparator);
	}

}
