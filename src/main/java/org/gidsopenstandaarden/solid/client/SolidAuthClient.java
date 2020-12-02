package org.gidsopenstandaarden.solid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

/**
 *
 */
@Service
public class SolidAuthClient {
	private final RSAKey jwtSigingKey;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final HttpClientCreator httpClientCreator;

	public SolidAuthClient(RSAKey jwtSigingKey, HttpClientCreator httpClientCreator) {
		this.jwtSigingKey = jwtSigingKey;
		this.httpClientCreator = httpClientCreator;
	}

	public URL authorize(String idp, Map<String, Object> state, String redirectUrl) throws IOException {
		Map<String, Object> configuration;
		if (state.containsKey("conf")) {
			configuration = (Map<String, Object>) state.get("conf");
		} else {
			configuration = readConfiguration(idp);
			state.put("conf", configuration);

		}

		if (!state.containsKey("jwks")) {
			state.put("jwks", readJwksUri((String) configuration.get("jwks_uri")));
		}

		String stateId = UUID.randomUUID().toString();
		if (!state.containsKey("state")) {
			state.put("state", stateId);
		}

		if (!state.containsKey("validation_key")) {

			RSAKey publicJwk = jwtSigingKey.toPublicJWK();
			publicJwk = new RSAKey.Builder(publicJwk).keyOperations(Set.of(KeyOperation.VERIFY)).build();
			state.put("validation_key", publicJwk.toJSONObject());

		}

		String registrationEndpoint = (String) configuration.get("registration_endpoint");

		Map<String, Object> data = new HashMap<>();
		data.put("issuer", configuration.get("issuer"));
		data.put("grant_types", List.of("authorization_code", "refresh_token"));
		data.put("token_endpoint_auth_method", "client_secret_basic");
		data.put("application_type", "web");
		data.put("redirect_uris", List.of(redirectUrl));
		data.put("response_types", List.of("code"));
		data.put("scope", "openid profile");

		final Map<String, Object> registrationResponse = postJson(registrationEndpoint, data);
		state.put("registration_response", registrationResponse);

		Map<String, Object> authRequest = new HashMap<>();
		authRequest.put("redirect_uri", redirectUrl);
		authRequest.put("display", "page");
		authRequest.put("nonce", UUID.randomUUID().toString());
		authRequest.put("key", state.get("validation_key"));

		try {
			PlainJWT plainJWT = new PlainJWT(JWTClaimsSet.parse(authRequest));
			final String authRequestToken = plainJWT.serialize();
			URL authorizationUrl = getAuthizationUrl((String) configuration.get("authorization_endpoint"), authRequestToken, (String) registrationResponse.get("client_id"), stateId);
			return authorizationUrl;
		} catch (ParseException e) {
			throw new IOException(e);
		}

	}

	public Map<String, String> getAuthorizationHeaders(OAuth2Token oAuth2Token, String url, String method) throws IOException {
		HashMap<String, String> header = new HashMap<>();
		header.put("Authorization", String.format("DPoP %s", oAuth2Token.getAccessToken()));
		header.put("DPoP", getDpopHeader(url, method));
		return header;
	}

	public String getDpopHeader(String url, String method) throws IOException {
		try {
			SignedJWT signedJWT = new SignedJWT(
					new JWSHeader.Builder(JWSAlgorithm.RS256)
							.type(new JOSEObjectType("dpop+jwt"))
							.jwk(jwtSigingKey.toPublicJWK()).build(),
					new JWTClaimsSet.Builder()
							.claim("htm", method)
							.claim("htu", url)
							.issueTime(new Date())
							.jwtID(UUID.randomUUID().toString())
							.build());

			signedJWT.sign(new RSASSASigner(jwtSigingKey));
			return signedJWT.serialize();
		} catch (JOSEException e) {
			throw new IOException(e);
		}
	}

	public OAuth2Token token(String code, String redirectUri, Map<String, Object> state) throws IOException {
		Map<String, Object> configuration = (Map<String, Object>) state.get("conf");
		String tokenEndpoint = (String) configuration.get("token_endpoint");
		Map<String, String> data = new HashMap<>();
		data.put("grant_type", "authorization_code");
		data.put("code", code);
		data.put("redirect_uri", redirectUri);

		Map<String, Object> registrationResponse = (Map<String, Object>) state.get("registration_response");
		String clientId = (String) registrationResponse.get("client_id");
		String clientSecret = (String) registrationResponse.get("client_secret");

		String credentials = String.format("%s:%s", clientId, clientSecret);
		final String credentailsHeader = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.US_ASCII));
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", String.format("Basic %s", credentailsHeader));
		headers.put("DPoP", getDpopHeader(tokenEndpoint, "POST"));
		return postForm(tokenEndpoint, data, headers, OAuth2Token.class);
	}

	private URL getAuthizationUrl(String authorizationEndpoint, String request, String clientId, String state) throws IOException {
		try {
			return new URIBuilder(authorizationEndpoint)
					.setParameter("scope", "openid")
					.setParameter("client_id", clientId)
					.setParameter("response_type", "code")
					.setParameter("request", request)
					.setParameter("state", state)
					.setParameter("nonce", UUID.randomUUID().toString())
					.build()
					.toURL();
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	private <T> T postForm(String url, Map<String, String> data, Map<String, String> headers, Class<T> cls) throws IOException {
		CloseableHttpClient client = httpClientCreator.createHttpClient();
		final HttpPost httpPost = new HttpPost(url);
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpPost.setHeader(entry.getKey(), entry.getValue());
		}
		List<NameValuePair> params = new ArrayList<>();
		for (Map.Entry<String, String> entry : data.entrySet()) {
			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		HttpEntity requestEntity = new UrlEncodedFormEntity(params);
		httpPost.setEntity(requestEntity);
		try (CloseableHttpResponse response = client.execute(httpPost)) {
			final HttpEntity entity = response.getEntity();
			return objectMapper.readValue(entity.getContent(), cls);
		}
	}

	private Map<String, Object> postJson(String url, Map<String, Object> data) throws IOException {

		CloseableHttpClient client = httpClientCreator.createHttpClient();
		final HttpPost httpPost = new HttpPost(url);
		StringEntity requestEntity = new StringEntity(
				objectMapper.writeValueAsString(data),
				StandardCharsets.UTF_8);
		requestEntity.setContentType("application/json");
		httpPost.setEntity(requestEntity);
		try (CloseableHttpResponse response = client.execute(httpPost)) {
			final HttpEntity entity = response.getEntity();
			return objectMapper.readValue(entity.getContent(), Map.class);
		}


	}

	private Map<String, Object> readConfiguration(String idp) throws IOException {
		String url = String.format("%s/.well-known/openid-configuration", idp);
		return readJson(url);
	}

	private Map<String, Object> readJson(String url) throws IOException {
		return objectMapper.readValue(new URL(url), Map.class);
	}

	private Map<String, Object> readJwksUri(String url) throws IOException {
		return readJson(url);
	}

}
