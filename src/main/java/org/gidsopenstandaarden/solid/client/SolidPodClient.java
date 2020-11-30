package org.gidsopenstandaarden.solid.client;

import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Service
public class SolidPodClient {
	private final SolidAuthClient solidAuthClient;
	private final HttpClientCreator httpClientCreator;

	public SolidPodClient(SolidAuthClient solidAuthClient, HttpClientCreator httpClientCreator) {
		this.solidAuthClient = solidAuthClient;
		this.httpClientCreator = httpClientCreator;
	}

	public String getBaseUrl(String token, String path) throws IOException {
		try {
			final String subject = SignedJWT.parse(token).getJWTClaimsSet().getSubject();
			return UrlUtils.getBaseUrl(subject, path);
		} catch (ParseException e) {
			throw new IOException(e);
		}
	}

	public Map<String, Object> listFiles(OAuth2Token token, String path) throws IOException {
		Map<String, Object> rv = new HashMap<>();
		String url = getBaseUrl(token.getIdToken(), path);
//		Model model = ModelFactory.createDefaultModel();
		Model model = getRdfRequest(token, url, "GET");
		Property property = ResourceFactory.createProperty("http://www.w3.org/ns/ldp#contains");
		final StmtIterator statements = model.listStatements(null, property, (String) null);
		while (statements.hasNext()) {
			final Statement next = statements.next();
			final RDFNode object = next.getObject();
			final Resource container = ResourceFactory.createResource("http://www.w3.org/ns/ldp#Container");
			final Resource resource = ResourceFactory.createResource("http://www.w3.org/ns/ldp#Resource");
			final Resource item = object.asResource();
			final String name = getLocalName(item);
			if (model.contains(item, RDF.type, container)) {
				String childPath;
				if (StringUtils.endsWith(path, "/")) {
					childPath = String.format("%s%s/", path, name);
				} else {
					childPath = String.format("%s/%s/", path, name);
				}

				rv.put(name, listFiles(token, childPath));
			} else if (model.contains(item, RDF.type, resource)) {
				rv.put(name, name);
			}

		}
		return rv;
	}

	public boolean putFile(OAuth2Token token, String path, String content, String type, String encoding) throws IOException {
		String url = getBaseUrl(token.getIdToken(), path);
		Map<String, String> headers = new HashMap<>();
		headers.put("link", "<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"");
		headers.putAll(solidAuthClient.getAuthorizationHeaders(token, url, "PUT"));
		if (StringUtils.isNotEmpty(type)) {
			headers.put("Content-Type", type);
		}

		try (CloseableHttpClient client = httpClientCreator.createHttpClient()) {
			HttpPut httpPut = new HttpPut(url);
			setHeaders(headers, httpPut);
			httpPut.setEntity(new StringEntity(content, encoding));

			try (final CloseableHttpResponse response = client.execute(httpPut)) {
				return response.getStatusLine().getStatusCode() == 201;
			}
		}

	}

	private String getLocalName(Resource item) {
		String uri = item.getURI();
		if (StringUtils.endsWith(uri, "/")) {
			uri = StringUtils.removeEnd(uri, "/");
		}
		return StringUtils.substringAfterLast(uri, "/");
	}

	private Model getRdfRequest(OAuth2Token token, String url, String method) throws IOException {
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/rdf+xml");

		try (CloseableHttpClient client = httpClientCreator.createHttpClient()) {
			HttpGet httpGet = new HttpGet(url);
			headers.putAll(solidAuthClient.getAuthorizationHeaders(token, url, method));
			setHeaders(headers, httpGet);

			try (CloseableHttpResponse response = client.execute(httpGet)) {
				Model model = ModelFactory.createDefaultModel();
				final HttpEntity entity = response.getEntity();
//			final String content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
				return model.read(entity.getContent(), url, "RDF/XML");
			}
		}
	}

	private void setHeaders(Map<String, String> headers, HttpRequest request) {
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			request.setHeader(entry.getKey(), entry.getValue());
		}
	}
}
