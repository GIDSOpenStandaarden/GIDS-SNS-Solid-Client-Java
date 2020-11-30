package org.gidsopenstandaarden.solid.client;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 *
 */
public interface HttpClientCreator {
	CloseableHttpClient createHttpClient();
}
