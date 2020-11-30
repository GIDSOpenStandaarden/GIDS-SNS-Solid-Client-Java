package org.gidsopenstandaarden.solid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.gidsopenstandaarden.solid.client.SolidAuthClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.util.Assert.hasText;

/**
 *
 */
public class SolidAuthClientTest {
	SolidAuthClient solidAuthClient;
	CloseableHttpClient httpClient;

	@BeforeEach
	public void init() throws JOSEException {
		final RSAKey jwtSigingKey = new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).generate();
		httpClient = mock(CloseableHttpClient.class);
		solidAuthClient = new SolidAuthClient(jwtSigingKey, () -> httpClient);
	}

	@Test
	public void testNormal() throws IOException, JOSEException {
		HashMap<String, Object> state = new HashMap<>();
		CloseableHttpResponse response = mock(CloseableHttpResponse.class);
		HttpEntity httpEntity = mock(HttpEntity.class);
		when(httpEntity.getContent()).thenReturn(new StringBufferInputStream("{\"redirect_uris\":[\"http://localhost/solid_auth\"],\"client_id\":\"fa21b0bfe55afd8c179220c38df196e2\",\"client_secret\":\"14d504fd000738f2961bb454bc595bb4\",\"response_types\":[\"code\"],\"grant_types\":[\"authorization_code\",\"refresh_token\"],\"application_type\":\"web\",\"id_token_signed_response_alg\":\"RS256\",\"token_endpoint_auth_method\":\"client_secret_basic\",\"registration_access_token\":\"eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwczovL3NvbGlkY29tbXVuaXR5Lm5ldCIsImF1ZCI6ImZhMjFiMGJmZTU1YWZkOGMxNzkyMjBjMzhkZjE5NmUyIiwic3ViIjoiZmEyMWIwYmZlNTVhZmQ4YzE3OTIyMGMzOGRmMTk2ZTIifQ.WPRt06Yy9xRoncA_Aoc5raZBc1xi3SpRtvyDWXfruNqAX_24N2yVUiPC8gx-PBaMIdXbHyZmTLUDqPMfpJFzomKSZUl6UOhEDh9C2gB8ch7iLqx4AvzHXhx-fTLItPwBe3iOMyLJfgdRkfwj0EV9BUQsTkzIV9r5BjAz8hKcRO7UZrEYXu5Q-eGi2kfXg6w0KiD4OE0P-TYwW-5FnUhUz6G-gDaTVXKsjKQ83ZuJNpdoCpKJvOeE8jnbiQ2sIY7WWM6Xq3_cb6peOyOf3c-9buV9lP9WcKUv6LEZDeaJffZvBqM3G5HJ3VSBX1VnBQjhoQAsNl8_sG58wRbJg7iU1w\",\"registration_client_uri\":\"https://solidcommunity.net/register/fa21b0bfe55afd8c179220c38df196e2\",\"client_id_issued_at\":1606492470,\"client_secret_expires_at\":0}"));
		when(response.getEntity()).thenReturn(httpEntity);
		when(httpClient.execute(any())).thenReturn(response);
		final URL authorizeUrl = solidAuthClient.authorize("https://solidcommunity.net", state, "http://localhost/solid_auth");
		hasText("solidcommunity", authorizeUrl.toExternalForm());

		OAuth2Token tok = new OAuth2Token();
		when(httpEntity.getContent()).thenReturn(new StringBufferInputStream(new ObjectMapper().writeValueAsString(tok)));
		OAuth2Token token = solidAuthClient.token("1234", "http://localhost/solid_auth", state);
		Assertions.assertEquals(tok, token);


	}


}
