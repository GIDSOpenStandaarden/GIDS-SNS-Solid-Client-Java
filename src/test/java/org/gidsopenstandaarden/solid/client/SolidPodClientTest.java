package org.gidsopenstandaarden.solid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.util.Assert.hasText;

/**
 *
 */
public class SolidPodClientTest {

	private SolidPodClient solidPodClient;
	private SolidAuthClient solidAuthClient;
	private CloseableHttpClient httpClient;

	@BeforeEach
	public void init() throws JOSEException {
		solidAuthClient = mock(SolidAuthClient.class);
		httpClient = mock(CloseableHttpClient.class);
		solidPodClient = new SolidPodClient(solidAuthClient, () -> httpClient);
	}

	@Test
	public void testListFiles() throws IOException {
		OAuth2Token token = new ObjectMapper().readValue("{\"access_token\":\"eyJhbGciOiJSUzI1NiIsImtpZCI6IkpxS29zX2J0SHBnIn0.eyJpc3MiOiJodHRwczovL3NvbGlkY29tbXVuaXR5Lm5ldCIsImF1ZCI6WyI4OWM0N2M0OWU5NTk0NDk0YjY1ODliYzgyNGM5NmNiYiJdLCJzdWIiOiJodHRwczovL3JvbGFuZGdyb2VuLnNvbGlkY29tbXVuaXR5Lm5ldC9wcm9maWxlL2NhcmQjbWUiLCJleHAiOjE2MDc3MDQxMTAsImlhdCI6MTYwNjQ5NDUxMCwianRpIjoiZGVhYjE1ZDVlOTViZTMyYiIsImNuZiI6eyJqa3QiOiJaMzNOc2Z2Zlh3eV9BV2g3WEp0QVpfcnB3R3lYTG1Xd25YUktHUm5EcmpnIn19.P2JgdmBCAl5amLBKaci5UbmwlU3ivBhxvfv6exy0UDiGadpTnqBsNNKg91ebRhs5i29JOEX7oEhej5SDkqTUvr4gg8YSg4hbZklKw4c-l_rNKlJ7oq6vC9ZJZ13RKFuGMr7NfndrJ96bhPcCLLQdGdKUVw7gvE2-aYHJ_6_zIXlrkEzN7RBaC-wdT3kgfcYl9scO58NlMir57lpirHPgJMl79UA3ir_BuRoztSS_6UMjnl_l6ov1yZKECxj5PZRyg04uEQs4t1deyjoogsBybypMv1p1w8YxBqjVgdUQmMS3BSmg7JCMMCR5ORvhpN0CHFlhUBDgcq95daUU7hSf7Q\",\"refresh_token\":\"5953b830ab28f49b020599a41652f2fe\",\"id_token\":\"eyJhbGciOiJSUzI1NiIsImtpZCI6InhoSFJkSFRqQmZRIn0.eyJpc3MiOiJodHRwczovL3NvbGlkY29tbXVuaXR5Lm5ldCIsImF1ZCI6Ijg5YzQ3YzQ5ZTk1OTQ0OTRiNjU4OWJjODI0Yzk2Y2JiIiwiYXpwIjoiODljNDdjNDllOTU5NDQ5NGI2NTg5YmM4MjRjOTZjYmIiLCJzdWIiOiJodHRwczovL3JvbGFuZGdyb2VuLnNvbGlkY29tbXVuaXR5Lm5ldC9wcm9maWxlL2NhcmQjbWUiLCJleHAiOjE2MDc3MDQxMTAsImlhdCI6MTYwNjQ5NDUxMCwianRpIjoiNjI5N2QwYWYzZGE0NTM4YiIsImF0X2hhc2giOiI3YnlDSGEzVmp6TUR3TTQyVUlqTUJ3In0.D43_JwzN9IyQ3_jg5o0VhqukoRmJCkFzTj7OeqYq4e5aCanOR-0Uja69mI_cP5IhdXko_PXzFqMbAVmTU0fgTWelTRnz1dmh1mkYYbW77OmajasBpAVZ5lpJ7Vz87UAa2K7IXxyB7wUispABzHTNnO9unpWFJd_sRDjKTcPRyAhPofHqrmDhsDxIfJNeXObL6tcpveRwOgyc4qbFV8aDBgmZfeZfEY5ldEhzozqjb-XKnijj9K1hq9wu6mDdlGD6PI_D8Npj_g7BArYvjKBXHzDGokOU4rJYwRSxVad1jgcLX4RsTjMdMjthe5pitQeBN1hj-qNvknjX25iUWNpHQQ\",\"token_type\":\"Bearer\",\"expires_in\":1209600}", OAuth2Token.class);
		CloseableHttpResponse response = mock(CloseableHttpResponse.class);
		HttpEntity httpEntity = mock(HttpEntity.class);
		when(httpEntity.getContent()).thenReturn(new StringBufferInputStream("<rdf:RDF\n" +
				" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
				" xmlns:terms=\"http://purl.org/dc/terms/\"\n" +
				" xmlns:ldp=\"http://www.w3.org/ns/ldp#\"\n" +
				" xmlns:st=\"http://www.w3.org/ns/posix/stat#\"\n" +
				" xmlns:ter=\"http://www.w3.org/ns/solid/terms#\">\n" +
				"    <rdf:Description rdf:about=\"\">\n" +
				"        <terms:modified rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2020-11-17T22:08:04Z</terms:modified>\n" +
				"        <rdf:type rdf:resource=\"http://www.w3.org/ns/ldp#BasicContainer\"/>\n" +
				"        <rdf:type rdf:resource=\"http://www.w3.org/ns/ldp#Container\"/>\n" +
				"        <ldp:contains rdf:resource=\"/robots.txt\"/>\n" +
				"        <st:mtime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#decimal\">1605650884.543</st:mtime>\n" +
				"        <st:size rdf:datatype=\"http://www.w3.org/2001/XMLSchema#integer\">4096</st:size>\n" +
				"    </rdf:Description>\n" +
				"    <rdf:Description rdf:about=\"/robots.txt\">\n" +
				"        <terms:modified rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2020-10-21T12:33:11Z</terms:modified>\n" +
				"        <rdf:type rdf:resource=\"http://www.w3.org/ns/iana/media-types/text/plain#Resource\"/>\n" +
				"        <rdf:type rdf:resource=\"http://www.w3.org/ns/ldp#Resource\"/>\n" +
				"        <st:mtime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#decimal\">1603283591.103</st:mtime>\n" +
				"        <st:size rdf:datatype=\"http://www.w3.org/2001/XMLSchema#integer\">83</st:size>\n" +
				"    </rdf:Description>\n" +
				"</rdf:RDF>"));
		when(response.getEntity()).thenReturn(httpEntity);
		when(httpClient.execute(any())).thenReturn(response);
		Map<String, Object> map = solidPodClient.listFiles(token, "/");
		System.out.println(map);

	}

	@Test
	public void testPutFile() throws IOException {
		OAuth2Token token = new ObjectMapper().readValue("{\"access_token\":\"eyJhbGciOiJSUzI1NiIsImtpZCI6IkpxS29zX2J0SHBnIn0.eyJpc3MiOiJodHRwczovL3NvbGlkY29tbXVuaXR5Lm5ldCIsImF1ZCI6WyI4OWM0N2M0OWU5NTk0NDk0YjY1ODliYzgyNGM5NmNiYiJdLCJzdWIiOiJodHRwczovL3JvbGFuZGdyb2VuLnNvbGlkY29tbXVuaXR5Lm5ldC9wcm9maWxlL2NhcmQjbWUiLCJleHAiOjE2MDc3MDQxMTAsImlhdCI6MTYwNjQ5NDUxMCwianRpIjoiZGVhYjE1ZDVlOTViZTMyYiIsImNuZiI6eyJqa3QiOiJaMzNOc2Z2Zlh3eV9BV2g3WEp0QVpfcnB3R3lYTG1Xd25YUktHUm5EcmpnIn19.P2JgdmBCAl5amLBKaci5UbmwlU3ivBhxvfv6exy0UDiGadpTnqBsNNKg91ebRhs5i29JOEX7oEhej5SDkqTUvr4gg8YSg4hbZklKw4c-l_rNKlJ7oq6vC9ZJZ13RKFuGMr7NfndrJ96bhPcCLLQdGdKUVw7gvE2-aYHJ_6_zIXlrkEzN7RBaC-wdT3kgfcYl9scO58NlMir57lpirHPgJMl79UA3ir_BuRoztSS_6UMjnl_l6ov1yZKECxj5PZRyg04uEQs4t1deyjoogsBybypMv1p1w8YxBqjVgdUQmMS3BSmg7JCMMCR5ORvhpN0CHFlhUBDgcq95daUU7hSf7Q\",\"refresh_token\":\"5953b830ab28f49b020599a41652f2fe\",\"id_token\":\"eyJhbGciOiJSUzI1NiIsImtpZCI6InhoSFJkSFRqQmZRIn0.eyJpc3MiOiJodHRwczovL3NvbGlkY29tbXVuaXR5Lm5ldCIsImF1ZCI6Ijg5YzQ3YzQ5ZTk1OTQ0OTRiNjU4OWJjODI0Yzk2Y2JiIiwiYXpwIjoiODljNDdjNDllOTU5NDQ5NGI2NTg5YmM4MjRjOTZjYmIiLCJzdWIiOiJodHRwczovL3JvbGFuZGdyb2VuLnNvbGlkY29tbXVuaXR5Lm5ldC9wcm9maWxlL2NhcmQjbWUiLCJleHAiOjE2MDc3MDQxMTAsImlhdCI6MTYwNjQ5NDUxMCwianRpIjoiNjI5N2QwYWYzZGE0NTM4YiIsImF0X2hhc2giOiI3YnlDSGEzVmp6TUR3TTQyVUlqTUJ3In0.D43_JwzN9IyQ3_jg5o0VhqukoRmJCkFzTj7OeqYq4e5aCanOR-0Uja69mI_cP5IhdXko_PXzFqMbAVmTU0fgTWelTRnz1dmh1mkYYbW77OmajasBpAVZ5lpJ7Vz87UAa2K7IXxyB7wUispABzHTNnO9unpWFJd_sRDjKTcPRyAhPofHqrmDhsDxIfJNeXObL6tcpveRwOgyc4qbFV8aDBgmZfeZfEY5ldEhzozqjb-XKnijj9K1hq9wu6mDdlGD6PI_D8Npj_g7BArYvjKBXHzDGokOU4rJYwRSxVad1jgcLX4RsTjMdMjthe5pitQeBN1hj-qNvknjX25iUWNpHQQ\",\"token_type\":\"Bearer\",\"expires_in\":1209600}", OAuth2Token.class);
		CloseableHttpResponse response = mock(CloseableHttpResponse.class);
		StatusLine statusLine = mock(StatusLine.class);
		when(statusLine.getStatusCode()).thenReturn(201);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(httpClient.execute(any())).thenReturn(response);

		solidPodClient.putFile(token, "/test.txt", "This is a test", "text/html", "UTF-8");
	}
}
