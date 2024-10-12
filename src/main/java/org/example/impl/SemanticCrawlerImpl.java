package org.example.impl;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.example.crawler.SemanticCrawler;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

public class SemanticCrawlerImpl implements SemanticCrawler {
	private ArrayList<String> visitedURIs = new ArrayList<>();
	private CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();
	private Model modelFactory = ModelFactory.createDefaultModel();
	private int successfulURICounter = 0;

	public void search(Model graph, String resourceURI) {
		try {
			modelFactory.read(resourceURI);
			StmtIterator triples = modelFactory.listStatements(modelFactory.createResource(resourceURI), (Property) null, (RDFNode) null);
			graph.add(triples);
			visitedURIs.add(resourceURI);

			StmtIterator statements = modelFactory.listStatements(modelFactory.createResource(resourceURI), OWL.sameAs, (RDFNode) null);
			while (statements.hasNext()) {
				Statement statement = statements.nextStatement();
				Resource object = (Resource) statement.getObject();

				Set<String> set = new HashSet<>(visitedURIs);
				boolean isVisited = set.contains(object.getURI());

				System.out.println("\nNew URI obtained: " + object.getURI());
				if (isVisited) System.out.println("URI already visited\n");

				if (encoder.canEncode(object.getURI()) && !isVisited && !object.isAnon()) {
					search(graph, object.getURI());
					successfulURICounter++;
				} else if (object.getURI() == null || object.isAnon()) {
					System.out.println("\nBlank node: " + object.getId().toString() + "\n");
					blankNodeTreatment(graph, object);
				}
			}
		} catch (Exception e) {
			System.err.println("Error opening URI: " + e.getMessage());
		}
		System.out.println("Successfully visited URIs: " + successfulURICounter);
	}

	private void blankNodeTreatment(Model model, Resource object) {
		StmtIterator blankNodeTriples = model.listStatements(object, (Property) null, (RDFNode) null);
		while (blankNodeTriples.hasNext()) {
			Statement currentTriple = blankNodeTriples.nextStatement();
			model.add(currentTriple);
		}
		blankNodeTriples = model.listStatements(object, (Property) null, (RDFNode) null);
		while (blankNodeTriples.hasNext()) {
			Statement currentTriple = blankNodeTriples.nextStatement();
			Resource objectNode = (Resource) currentTriple.getObject();
			if (objectNode.isAnon()) {
				blankNodeTreatment(model, objectNode);
			}
		}
	}
}
