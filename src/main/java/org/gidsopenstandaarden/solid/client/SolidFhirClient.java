package org.gidsopenstandaarden.solid.client;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Task;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class SolidFhirClient extends SolidPodClient {


	public SolidFhirClient(SolidAuthClient solidAuthClient, HttpClientCreator httpClientCreator) {
		super(solidAuthClient, httpClientCreator);
	}

	public Task getTask(OAuth2Token token, String userReference, String id) throws IOException {
		String subjectUrl = getBaseUrl(token.getIdToken(), "/fhir/Task/" + id);
		Model model = getRdfRequest(token, subjectUrl, "GET", "text/turtle");
		Resource subject = model.getResource(subjectUrl);
		return buildTask(subject, model, userReference);
	}

	public List<Task> listTasks(OAuth2Token token, String userReference) throws IOException {
		List<Task> rv = new ArrayList<>();
		String url = getBaseUrl(token.getIdToken(), "/fhir/Task/");
		Model model = getRdfRequest(token, url, "GET", "text/turtle");

		for (Resource subject : model.listSubjectsWithProperty(PROPERTY_TYPE, TYPE_RESOURCE).toList()) {
			Model subjectModel = getRdfRequest(token, subject.getURI(), "GET", "text/turtle");
			rv.add(buildTask(subject, subjectModel, userReference));
		}

		return rv;
	}

	private Task buildTask(Resource subject, Model model, String userReference) {
		Statement identifier = model.getRequiredProperty(subject, model.createProperty("https://www.hl7.org/fhir/stu3/task-definitions.html", "#Task.identifier"));
		Statement status = model.getRequiredProperty(subject, model.createProperty("https://www.hl7.org/fhir/stu3/task-definitions.html", "#Task.status"));
		Statement definitionReference = model.getRequiredProperty(subject, model.createProperty("https://www.hl7.org/fhir/stu3/task-definitions.html", "#Task.definitionReference"));
		Task task = new Task();
		task.setId(identifier.getString());
		task.setFor(new Reference(userReference));
		task.setDefinition(new Reference("ActivityDefinition/" + definitionReference.getString()));
		task.setStatus(getTaskStatus(status.getString()));
		return task;
	}

	private Task.TaskStatus getTaskStatus(String status) {
		switch (status) {
			case "submitted":
				status = Task.TaskStatus.COMPLETED.toCode();
				break;
			case "opened":
				status = Task.TaskStatus.INPROGRESS.toCode();
				break;
			default:
				break;
		}
		return Task.TaskStatus.fromCode(status);
	}

}
