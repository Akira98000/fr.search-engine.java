package search_engine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class SearchEngine {
	private Path indexation_directory;
	private List<IndexedPage> pages;
	
	public SearchEngine(Path indexation_directory) throws IOException {
		this.indexation_directory = indexation_directory;
		pages = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(indexation_directory, path -> Files.isRegularFile(path) && Files.isReadable(path))) {
			for (Path filePath : stream)
				pages.add(new IndexedPage(filePath));
		} catch (IOException e) {
			System.err.println("Probl�me lors de la lecture du r�pertoire " + indexation_directory.getFileName() + " : " + e.getMessage());
		}
	}
	
	public IndexedPage getPage(int i) throws IndexOutOfBoundsException {
	    if (pages == null || pages.size() == 0) {
	        throw new IndexOutOfBoundsException("The pages array is null or empty.");
	    }
	    
	    if (i < 0 || i >= pages.size()) {
	        throw new IndexOutOfBoundsException("Impossible d'acc�der au document � l'index " + i + ". Nombre de documents : " + this.getPagesNumber());
	    }
	    
	    return pages.get(i);
	}

	
	public int getPagesNumber() {
		return pages.size();
	}
	
	public SearchResult[] launchRequest(String requestString) {
		int i = 0;
		SearchResult[] results = new SearchResult[pages.size()];
		IndexedPage request = new IndexedPage(requestString);
		for (IndexedPage page : pages) {
			results[i] = new SearchResult(page.getUrl());
			results[i].setScore(request.proximity(page));
			i++;
		}
		return results;
	}
	
	public void printResults(String requestString){
		int i;
		final int printLimit = 15;
		SearchResult[] results = launchRequest(requestString);
		
		// Applique un filtre au tableau results afin d'enlever tous les r�sultats dont le score est nul
		results = Arrays.stream(results).filter(result -> result.getScore() > 0.).toArray(SearchResult[]::new);
		// Filtre les r�sultats selon leur pertinence
	    Arrays.sort(results, Comparator.comparingDouble(SearchResult::getScore).reversed());
	 
	    // Affichage des r�sultats les plus pertinents (limite fix�e par printLimit)
	    if (results.length == 0)
	    	System.out.println("Aucun r�sultat pour cette recherche !");
	    else
	    	System.out.println("Voici ce que nous avons trouv� :");
		for (i = 0; i < ((results.length < printLimit) ? (results.length) : (printLimit)); i++)
			System.out.println(i + " - " + results[i].toString());
	}
	
	public static void main(String[] args) throws URISyntaxException, IOException {
	    Scanner scanner = new Scanner(System.in);
	    String request;
	    URL location = SearchEngine.class.getProtectionDomain().getCodeSource().getLocation();
	    Path binFolder = Paths.get(location.toURI());
	    Path indexFolder = binFolder.resolve("INDEX_FILES");
	    SearchEngine engine = new SearchEngine(indexFolder);

	    if (args.length > 0) {
	        request = String.join(" ", args);
	        engine.printResults(request);
	    } else {
	        do {
	            System.out.println("\nLancez une recherche :");
	            request = scanner.nextLine();
	            if (request.isEmpty()) {
	                throw new IllegalArgumentException("La requ�te ne peut pas �tre vide !");
	            }
	            engine.printResults(request);
	        } while (!request.equals("exit"));
	    }
	    scanner.close();
	}

}
