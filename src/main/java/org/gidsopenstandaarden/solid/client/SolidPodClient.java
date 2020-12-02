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
import org.apache.http.util.EntityUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Service
public class SolidPodClient {
	public static final Resource TYPE_RESOURCE = ResourceFactory.createResource("http://www.w3.org/ns/ldp#Resource");
	public static final Resource TYPE_CONTAINER = ResourceFactory.createResource("http://www.w3.org/ns/ldp#Container");
	public static final Property PROPERTY_TYPE = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

	protected final SolidAuthClient solidAuthClient;
	protected final HttpClientCreator httpClientCreator;

	private final static Map<String, String> CONTENT_TYPE_MAP_RDF_ = new HashMap<>();
	static {
		CONTENT_TYPE_MAP_RDF_.put("application/rdf+xml", "RDF/XML");
		CONTENT_TYPE_MAP_RDF_.put("text/turtle", "TURTLE");
	}

	protected final static Map<String, String> CONTENT_TYPE_MAP_RDF = Collections.unmodifiableMap(CONTENT_TYPE_MAP_RDF_);

	public SolidPodClient(SolidAuthClient solidAuthClient, HttpClientCreator httpClientCreator) {
		this.solidAuthClient = solidAuthClient;
		this.httpClientCreator = httpClientCreator;
	}

	public String getBaseUrl(String token, String path) throws IOException {
		try {
			String subject = getSubject(token);
			return UrlUtils.getBaseUrl(subject, path);
		} catch (ParseException e) {
			throw new IOException(e);
		}
	}

	private String getSubject(String token) throws ParseException {
		return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
	}

	public String getWebId(OAuth2Token token) throws ParseException {
		return getSubject(token.getIdToken());
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
			final Resource item = object.asResource();
			final String name = getLocalName(item);
			if (model.contains(item, RDF.type, TYPE_CONTAINER)) {
				String childPath;
				if (StringUtils.endsWith(path, "/")) {
					childPath = String.format("%s%s/", path, name);
				} else {
					childPath = String.format("%s/%s/", path, name);
				}

				rv.put(name, listFiles(token, childPath));
			} else if (model.contains(item, RDF.type, TYPE_RESOURCE)) {
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

			try (CloseableHttpResponse response = client.execute(httpPut)) {
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

	protected Model getRdfRequest(OAuth2Token token, String url, String method) throws IOException {
		return getRdfRequest(token, url, method, "application/rdf+xml");
	}

	protected Model getRdfRequest(OAuth2Token token, String url, String method, String contentType) throws IOException {
		Map<String, String> headers = new HashMap<>();

		headers.put("Accept", contentType);

		try (CloseableHttpClient client = httpClientCreator.createHttpClient()) {
			HttpGet httpGet = new HttpGet(url);
			headers.putAll(solidAuthClient.getAuthorizationHeaders(token, url, method));
			setHeaders(headers, httpGet);

			try (CloseableHttpResponse response = client.execute(httpGet)) {
				Model model = ModelFactory.createDefaultModel();
				final HttpEntity entity = response.getEntity();
				final String content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
				return model.read(new StringReader(content), url, CONTENT_TYPE_MAP_RDF.get(contentType));
			}
		}
	}

	private void setHeaders(Map<String, String> headers, HttpRequest request) {
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			request.setHeader(entry.getKey(), entry.getValue());
		}
	}
}
