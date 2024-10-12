package org.example.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.example.impl.SemanticCrawlerImpl;

public class main {
	public static void main(String[] args) {
		Model graph = ModelFactory.createDefaultModel();
		String uri = "http://dbpedia.org/resource/Zico";
		SemanticCrawlerImpl crawler = new SemanticCrawlerImpl();
		crawler.search(graph, uri);		
		graph.write(System.out, "TTL");	
	}
}