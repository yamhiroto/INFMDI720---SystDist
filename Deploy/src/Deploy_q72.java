import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Deploy_q72 {
		
	public static void main(String[] args) throws IOException, InterruptedException{
		
		List<String> ordis = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader("/home/yamhiroto/Desktop/ordinateurs.txt"));
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
				
				//création du dossier temporaire hyamakawa
				System.out.println("	création du dossier /tmp/hyamakawa au sein de " +ordi);
				ProcessBuilder createFolder = new ProcessBuilder("ssh","yamakawa@"+ordi,"mkdir","-p","/tmp/hyamakawa");
				Process Foldercreation = createFolder.start();
				int f1 = Foldercreation.waitFor();  
				
				//renvoie 0 si le dossier a été créé avec succès
				if (f1 == 0) { 
					System.out.println("	dossier /hyamakawa créé avec succès");
					
					//copie du fichier slave.jar
					System.out.println("		copie du fichier slave.jar dans /tmp/hyamakawa ");
					ProcessBuilder copyFile = new ProcessBuilder("scp","/home/yamhiroto/Desktop/slave.jar","yamakawa@"+ordi+":/tmp/hyamakawa/");
					Process Filecopy = copyFile.start();
					int f2 = Filecopy.waitFor();
					
					// renvoie 0 si le fichier a été copié avec succès
					if (f2 == 0) {
						System.out.println("		copie du fichier effectué avec succès ");
					}
				}
				
			//si l'ordinateur ne répond pas	
			} else {
				System.out.println("l'ordinateur "+ ordi + " n'a pas répondu au bout de 2 secondes");	
			}
		}
		
		br.close();
	}	
}

