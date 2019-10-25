import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Clean {
	
	public static void main(String[] args) throws IOException, InterruptedException{
		
		List<String> ordis = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader("/home/yamhiroto/Desktop/machines_used.txt"));
		
		String line = br.readLine();
		while (line !=null) {
			ordis.add(line);
			line = br.readLine();
		}
		
		for (String ordi : ordis) {
			ProcessBuilder pb = new ProcessBuilder("ssh", "yamakawa@"+ordi, "hostname");
			Process p = pb.start();
			boolean b = p.waitFor(2,TimeUnit.SECONDS);
			//renvoie vrai si l'ordinateur répond
			if (b == true) { 
				System.out.println("l'ordinateur " + ordi + " est fonctionnel");
				
				//Suppression du dossier temporaire hyamakawa
				System.out.println("	suppression du dossier /tmp/hyamakawa au sein de " +ordi);
				ProcessBuilder deleteFolder = new ProcessBuilder("ssh","yamakawa@"+ordi,"rm","-rf","/tmp/hyamakawa");
				Process Folderdeletion = deleteFolder.start();
				int f1 = Folderdeletion.waitFor();  
				
				//renvoie 0 si le dossier a été effacé avec succès
				if (f1 == 0) { 
					System.out.println("	dossier /hyamakawa effacé avec succès");
				}
				
			//si l'ordinateur ne répond pas	
			} else {
				System.out.println("l'ordinateur "+ ordi + " n'a pas répondu au bout de 2 secondes");	
			}
		}
		
		br.close();
	}	
}
