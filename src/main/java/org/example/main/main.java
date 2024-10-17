package org.example.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.example.impl.SemanticCrawlerImpl;

public class main {
    public static void main(String[] args) {
        Model graph = ModelFactory.createDefaultModel();
	String uri = "http://dbpedia.org/resource/Roberto_Ribeiro";

        //Outros recursos para teste
        //String uri = "https://raw.githubusercontent.com/CarlosEduardo-IFF/assets/refs/heads/main/CarlosEduardo1.rdf";
        //String uri = "http://dbpedia.org/resource/Zico";

	SemanticCrawlerImpl crawler = new SemanticCrawlerImpl();
	crawler.search(graph, uri);		
	graph.write(System.out, "TTL");	
        System.err.println("Triplas:"+ graph.size());


        FileManager fileManager = FileManager.get();
        Model modelFromFile = ModelFactory.createDefaultModel();
        fileManager.readModel(modelFromFile, "C:/Users/Carlos/Downloads/RobertoRibeiro_UTF-8.rdf");

        System.err.println("Triplas2:"+ modelFromFile.size());

        // Comparar grafos
        if (graph.isIsomorphicWith(modelFromFile)) {
            System.out.println("Os grafos são isomórficos (iguais).");
        } else {
            System.out.println("Os grafos não são isomórficos (diferentes).");
            compareModels(graph, modelFromFile);
        }

    }

    public static void compareModels(Model model1, Model model2) {
        // Encontrar triplas que estão apenas no model1
        System.out.println("\nTriplas presentes apenas no grafo gerado:");
        model1.listStatements().forEachRemaining(statement -> {
            if (!model2.contains(statement)) {
                System.out.println(statement);
            }
        });

        // Encontrar triplas que estão apenas no model2
        System.out.println("\nTriplas presentes apenas no grafo do arquivo:");
        model2.listStatements().forEachRemaining(statement -> {
            if (!model1.contains(statement)) {
                System.out.println(statement);
            }
        });
    }
}
