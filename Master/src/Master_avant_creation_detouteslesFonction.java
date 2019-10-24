import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;


public class Master {
	
	public static void output(InputStream inputStream) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String line = br.readLine();
		while (line  != null) {
			System.out.println(line);
			line = br.readLine();
			int i = 2;
		}
		br.close();
	}
	
	
	public static Integer copy(String file,String file_path, String destination_path) throws IOException, InterruptedException {
		System.out.println("		copie du fichier "+ file+" depuis "+ file_path+ " vers "+ destination_path);
		
		ProcessBuilder copyFile = new ProcessBuilder("scp", file_path+file ,destination_path);
		Process Filecopy = copyFile.start();
		int f1 = Filecopy.waitFor();
		
		// renvoie 0 si le fichier a été copié avec succès
		return f1;
	}
	
	
	
	public static void main(String[] args) throws IOException, InterruptedException{
		
		//crée une liste à partir du fichier "machines.txt"  Pour la question 9, il y a un PC
		List<String> ordis = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader("/home/yamhiroto/Desktop/machines.txt"));
		String line = br.readLine();
		while (line !=null) {
			ordis.add(line);
			line = br.readLine();
		}
		
		
		int i = 0;
			
		//to write the file showing which machine has been used 
		String path = "/home/yamhiroto/Desktop/machines_used.txt";
		FileWriter writer = new FileWriter(path);
		
		//liste qui va ajouter les trois ordinateurs utilisés pour la suite
		HashMap<String, Integer> ordis_fonctionnel = new HashMap<>(); 
		
		for (String ordi : ordis) {
			
			//sortir de la boucle si les trois fichiers ont été copiés
			if (i==3) {
				System.out.println("Les trois fichiers ont été copiés sur trois pc différents, plus besoin de tenter d'autres connexion SSH");
				break;
			}
			
			//Teste la connexion SSH avec chaque ordinateur de la liste 
			ProcessBuilder tryConnection = new ProcessBuilder("ssh", "yamakawa@"+ordi, "hostname");
			Process Connection = tryConnection.start();
			boolean c = Connection.waitFor(2,TimeUnit.SECONDS);
		
			//renvoie vrai si l'ordinateur répond
			if (c == true) {
				System.out.println("l'ordinateur " + ordi + " est fonctionnel");
				
				//création du dossier temporaire hyamakawa
				System.out.println("	création du dossier /tmp/hyamakawa/ au sein de " +ordi);
				ProcessBuilder createFolder = new ProcessBuilder("ssh","yamakawa@"+ordi,"mkdir","-p","/tmp/hyamakawa");
				Process Foldercreation = createFolder.start();
				int f0 = Foldercreation.waitFor();  
				
				//renvoie 0 si le dossier a été créé avec succès
				if (f0 == 0) { 
					System.out.println("	dossier /hyamakawa créé avec succès");
					
					//copie du fichier slave.jar
					System.out.println("		copie du fichier slave.jar dans /tmp/hyamakawa ");
					ProcessBuilder copyFile = new ProcessBuilder("scp","/home/yamhiroto/Desktop/slave.jar","yamakawa@"+ordi+":/tmp/hyamakawa/");
					Process Filecopy = copyFile.start();
					int f1 = Filecopy.waitFor();
					
					// renvoie 0 si le fichier a été copié avec succès
					if (f1 == 0) {
						System.out.println("		copie du fichier effectué avec succès ");
					}
					
					System.out.println(" ");
					
					//creation du dossier temporaire splits
					System.out.println("		création du dossier /tmp/hyamakawa/splits au sein de " +ordi);
					ProcessBuilder createFolder2 = new ProcessBuilder("ssh","yamakawa@"+ordi,"mkdir","-p","/tmp/hyamakawa/splits");
					Process Foldercreation2 = createFolder2.start();
					int f2 = Foldercreation2.waitFor();  
					
					//renvoie 0 si le dossier a été créé avec succès
					if (f2 ==0) {
						System.out.println("		dossier /splits créé avec succès");
						
						//copie du fichier slave.jar
						System.out.println("			copie du fichier S"+i+" dans /tmp/hyamakawa/splits ");
						ProcessBuilder copyFile2 = new ProcessBuilder("scp","/home/yamhiroto/Desktop/S"+i+".txt","yamakawa@"+ordi+":/tmp/hyamakawa/splits/");
						Process Filecopy2 = copyFile2.start();
						int f3 = Filecopy2.waitFor();
						
						
					
						// renvoie 0 si le fichier a été copié avec succès
						if (f3 == 0) {
							System.out.println("			copie du fichier effectué avec succès ");
							//incrémente i de 1
							
							//dico key = nom ordi , value = numero fichier
							ordis_fonctionnel.put(ordi, i);
							writer.write(ordi + "\n");
							i = i+1;
						}
					}
				}
			} else {
				System.out.println("l'ordinateur "+ ordi + " n'a pas répondu au bout de 2 secondes");	
			}
			br.close();
		}	
		writer.close();
		
		
		
		System.out.println(" ======= Starting MAP ======");
		int numSlave = 0;	
		
		for (Map.Entry<String, Integer> pair : ordis_fonctionnel.entrySet()) {
			
			System.out.println("Ordinateur: " + pair.getKey()+" - Fichier: S"+pair.getValue() + ".txt");
			ProcessBuilder launchSlave = new ProcessBuilder("ssh","yamakawa@"+pair.getKey(),"java","-jar","/tmp/hyamakawa/slave.jar","0","/tmp/hyamakawa/splits/S"+pair.getValue()+".txt");
			Process slaveStart = launchSlave.start();
			int f0 = slaveStart.waitFor();  
			InputStream is = slaveStart.getInputStream(); 		

			if (f0 == 0) {
				output(is);
				int copied3 = copy("machines_used.txt","/home/yamhiroto/Desktop/","yamakawa@"+pair.getKey()+":/tmp/hyamakawa/");
				if (copied3 == 0) {
					System.out.println("fichier copie avec succes");
				}
				numSlave +=1;	
			}
		}
		if (numSlave == 3) {
			System.out.println(" ====== MAP Finished ======");
		
		} else {
			System.out.println("Got an issue");
		}
		
			/*
			System.out.println("==== Lancement du fichier slave.jar sur le pc "+ordi+" ====");
			ProcessBuilder pb = new ProcessBuilder("ssh", "yamakawa@"+ordi, "java", "-jar", "/tmp/hyamakawa/slave.jar");
			Process p = pb.start();
			boolean b = p.waitFor(15,TimeUnit.SECONDS);
			if (b == true) {
				InputStream is = p.getInputStream(); 		
				InputStream is_err = p.getErrorStream();	
			
				System.out.println("Standard output / sortie standard : ");
				output(is);
				System.out.println("Error output  / sortie d'erreur : ");
				output(is_err);
			
			} else {
				System.out.println("Too Long, Timeout");
				p.destroy();
			}
			*/
		
		
	}
}
	
		//Process pb = new ProcessBuilder("java", "-jar", "/tmp/hyamakawa/slave.jar").start();
		/*
		String command[] = {"java", "-jar", "/home/yamhiroto/Desktop/slave.jar"};
		ProcessBuilder Builder = new ProcessBuilder(command).inheritIO();
		Process pb = Builder.start();
		
        BufferedReader bReader = new BufferedReader(new InputStreamReader(pb.getInputStream()));
        BufferedReader bReader_error = new BufferedReader(new InputStreamReader(pb.getErrorStream()));
        
        
        	
        	try{
        		boolean stillRunning = pb.waitFor(5, TimeUnit.SECONDS);
        
        		if (stillRunning == false) {
        			pb.destroy();
        			System.out.println("Too long ! Timeout");
        			
        		} else {
        			    		
        			String line = bReader.readLine();
        			String line_error = bReader_error.readLine();
        	
    
        			System.out.println("Standard output: ");
        			while (line != null) {
        				System.out.println(line);	
        			}
        
        			System.out.println("Error output: ");
        			while (line_error != null ) {
        				System.out.println(line);
        			}
        		
        		}
        		
        	}catch (InterruptedException e) {
        		e.printStackTrace();
        	}		
		*/ 
        
	
