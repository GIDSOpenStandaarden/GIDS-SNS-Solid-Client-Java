package org.gidsopenstandaarden.solid.client;

import org.gidsopenstandaarden.solid.client.UrlUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

/**
 *
 */
public class UrlUtilsTest {

	@Test
	public void testNormal() throws MalformedURLException {
		Assertions.assertEquals("https://localhost:8098/test", UrlUtils.getBaseUrl("https://localhost:8098/test/flap/flop", "/test"));
		Assertions.assertEquals("https://localhost:8098/test", UrlUtils.getBaseUrl("https://localhost:8098", "/test"));
		Assertions.assertEquals("http://localhost:8098/test", UrlUtils.getBaseUrl("http://localhost:8098", "/test"));
		Assertions.assertEquals("http://localhost/test", UrlUtils.getBaseUrl("http://localhost", "/test"));
		Assertions.assertEquals("http://localhost/test", UrlUtils.getBaseUrl("http://localhost:80", "/test"));
		Assertions.assertEquals("https://localhost/test", UrlUtils.getBaseUrl("https://localhost:443", "/test"));
		Assertions.assertEquals("http://localhost:443/test", UrlUtils.getBaseUrl("http://localhost:443", "/test"));
	}

	@Test()
	public void testError(){
		Assertions.assertThrows(MalformedURLException.class, () -> {
			String baseUrl = UrlUtils.getBaseUrl("adsadsas", "/test");
		});

	}
}
