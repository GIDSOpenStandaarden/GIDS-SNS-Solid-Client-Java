package org.gidsopenstandaarden.solid.client;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public class UrlUtils {
	public static String getBaseUrl(String url, String path) throws MalformedURLException {
		URL host = new URL(url);
		final int port = host.getPort();
		final String protocol = host.getProtocol();
		if (isDefault(port, protocol)) {
			return String.format("%s://%s%s", protocol, host.getHost(), path);
		} else {
			return String.format("%s://%s:%d%s", protocol, host.getHost(), port, path);
		}
	}

	private static boolean isDefault(int port, String protocol) {
		if (port == -1) {
			return true;
		}
		if (port == 80 && StringUtils.equals("http", protocol)) {
			return true;
		}
		return port == 443 && StringUtils.equals("https", protocol);
	}
}
