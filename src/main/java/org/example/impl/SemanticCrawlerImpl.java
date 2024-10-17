package org.example.impl;

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
	
			// Busca URIs com OWL.sameAs
			processSameAsStatements(graph, resourceURI, true);
			processSameAsStatements(graph, resourceURI, false);
	
		} catch (Exception e) {
			System.err.println("Failed to process the URI: " + e.getMessage());
		}
		System.out.println("Total successfully processed URIs: " + successfulURICounter);
	}
	
	// Processa URIs relacionadas ao recurso utilizando a propriedade OWL.sameAs
	private void processSameAsStatements(Model graph, String resourceURI, boolean isSubject) {
		StmtIterator statements = isSubject ?
			modelFactory.listStatements(null, OWL.sameAs, modelFactory.createResource(resourceURI)) :
			modelFactory.listStatements(modelFactory.createResource(resourceURI), OWL.sameAs, (RDFNode) null);
	
		while (statements.hasNext()) {
			Statement statement = statements.nextStatement();
			Resource resource = isSubject ? (Resource) statement.getSubject() : (Resource) statement.getObject();
			String uri = resource.getURI();
	
			// Verifica se a URI já foi visitada
			boolean isVisited = visitedURIs.contains(uri);
	
			System.out.println("\nDiscovered new URI: " + uri);
			if (isVisited) {
				System.out.println("This URI has been visited before\n");
			}
	
			// Verifica se a URI pode ser codificada e se não foi visitada
			if (encoder.canEncode(uri) && !isVisited && !resource.isAnon()) {
				search(graph, uri);
				successfulURICounter++;
			} else if (uri == null || resource.isAnon()) {
				System.out.println("\nEncountered a blank node with ID: " + resource.getId().toString() + "\n");
				blankNodeTreatment(graph, resource);
			}
		}
	}

	// Trata nós anônimos no modelo RDF de forma recursiva
	private void blankNodeTreatment(Model model, Resource object) {
		
		StmtIterator triples = model.listStatements(object, (Property) null, (RDFNode) null);
	
		while (triples.hasNext()) {
			Statement currentTriple = triples.nextStatement();
			
			model.add(currentTriple);
	
			RDFNode objectNode = currentTriple.getObject();
			if (objectNode.isResource() && ((Resource) objectNode).isAnon()) {
				blankNodeTreatment(model, (Resource) objectNode);
			}
		}
	}
}
