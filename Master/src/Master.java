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
		}
		br.close();
	}
	
	public static boolean trySSHConnection(String ordi) throws IOException, InterruptedException {
		
		ProcessBuilder tryConnection = new ProcessBuilder("ssh", "yamakawa@"+ordi, "hostname");
		Process Connection = tryConnection.start();
		boolean c = Connection.waitFor(4,TimeUnit.SECONDS);
		
		if (c) {
			ProcessBuilder noCheck = new ProcessBuilder("ssh", "-o", "\"StrictHostKeyChecking=no\"", "yamakawa@"+ordi);
			Process copykey = noCheck.start();
			boolean d = copykey.waitFor(4,TimeUnit.SECONDS);
			if (d == true) {
				System.out.println("no check should be required anymore");
			}

		}
		// renvoie 0 si la connection a pu etre etabli
		return c;
	}
		
	public static Integer copy(String file,String file_path, String destination_path) throws IOException, InterruptedException {
		System.out.println("		copie du fichier "+ file+" depuis "+ file_path+ " vers "+ destination_path);
		
		ProcessBuilder copyFile = new ProcessBuilder("scp", file_path+file ,destination_path);
		Process Filecopy = copyFile.start();
		int f1 = Filecopy.waitFor();
		
		// renvoie 0 si le fichier a été copié avec succès

		
		return f1;
	}

	public static Integer createFolder(String FolderToCreate ,String ordi) throws IOException, InterruptedException {
		System.out.println("	création du dossier "+ FolderToCreate +" au sein de " +ordi);
		
		ProcessBuilder createFolder = new ProcessBuilder("ssh","yamakawa@"+ordi,"mkdir","-p", FolderToCreate);
		Process Foldercreation = createFolder.start();
		int f0 = Foldercreation.waitFor();
		
		// renvoie 0 si le dossier a été créé avec succès
		return f0;
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
		
		//liste qui va ajouter les trois ordinateurs utilisés pour la suite
		HashMap<String, Integer> ordis_fonctionnel = new HashMap<>(); 
		
		//fichier dans lequel les ordinateurs utilisés seront indiqués
		String path = "/home/yamhiroto/Desktop/machines_used.txt";
		FileWriter writer = new FileWriter(path);
		
		//incrémente de 1 lorsqu'un fichier Sx.txt est copié
		int i = 0;
		
		for (String ordi : ordis) {
			//sortir de la boucle si les trois fichiers ont été copiés
			if (i==3) {
				System.out.println("");
				System.out.println("INFO : Les trois fichiers ont été copiés sur trois pc différents, plus besoin de tenter d'autres connexion SSH");
				break;
			}
			
			//Teste la connexion SSH avec chaque ordinateur de la liste 
			boolean c = trySSHConnection(ordi);
			
			if (c == false) { 
				System.out.println("l'ordinateur "+ ordi + " n'a pas répondu au bout de 2 secondes");	
			} else {
				System.out.println("l'ordinateur " + ordi + " est fonctionnel");
				
				//création du dossier temporaire hyamakawa
				int creation_Dossier_temp = createFolder("/tmp/hyamakawa", ordi);
				if (creation_Dossier_temp == 0) { 
					System.out.println("	dossier /hyamakawa créé avec succès");
					
					//copie du fichier slave.jar
					int copie_Fichier_slave = copy("slave.jar", "/home/yamhiroto/Desktop/","yamakawa@"+ ordi +":/tmp/hyamakawa/");
					if (copie_Fichier_slave == 0) {
						System.out.println("		copie du fichier effectué avec succès ");
					}
					
					//creation du dossier temporaire splits
					int creation_Dossier_splits = createFolder("/tmp/hyamakawa/splits",ordi);
					if (creation_Dossier_splits ==0) {
						System.out.println("		dossier /splits créé avec succès");
						
						//copie du fichier Si.txt
						int copie_Fichier_Si = copy("S"+i+".txt","/home/yamhiroto/Desktop/","yamakawa@"+ordi+":/tmp/hyamakawa/splits/");
						if (copie_Fichier_Si == 0) {
							System.out.println("			copie du fichier effectué avec succès ");
						
							//dico key = nom ordi , value = numero fichier
							ordis_fonctionnel.put(ordi, i);
							writer.write(ordi + "\n");
							//incrémente i de 1
							i = i+1;
						}
					}
				}
			}	
		}
		br.close();
		writer.close();
		
		
		
		
		System.out.println(" ======= Starting MAP ======");
		int numSlave = 0;	
		
		for (Map.Entry<String, Integer> pair : ordis_fonctionnel.entrySet()) {
			
			System.out.println("Ordinateur: " + pair.getKey()+" - Fichier: S"+pair.getValue() + ".txt");
			ProcessBuilder launchSlave = new ProcessBuilder("ssh","yamakawa@"+pair.getKey(),"java","-jar","/tmp/hyamakawa/slave.jar","0","/tmp/hyamakawa/splits/S"+pair.getValue()+".txt");
			Process slaveStart = launchSlave.start();
			int f0 = slaveStart.waitFor();  
			InputStream is = slaveStart.getInputStream();
			InputStream es = slaveStart.getErrorStream();

			if (f0 == 0) {
				output(is);
				int copied3 = copy("machines_used.txt","/home/yamhiroto/Desktop/","yamakawa@"+pair.getKey()+":/tmp/hyamakawa/");
				if (copied3 == 0) {
					System.out.println("fichier copie avec succes");
				}
				numSlave +=1;	
			} else {
				output(es);
			}
		}
		if (numSlave == 3) {
			System.out.println(" ====== MAP Finished ======");
		
		} else {
			System.out.println("Got an issue with the MAP");
		}
		
		System.out.println("");
		
		
		
		System.out.println(" ======= Starting SHUFFLE ======");
		int numShuffle = 0;
		
		for (Map.Entry<String, Integer> pair : ordis_fonctionnel.entrySet()) {
			
			System.out.println("Ordinateur: " + pair.getKey()+" - Fichier: UM"+pair.getValue() + ".txt");
			ProcessBuilder launchSlave = new ProcessBuilder("ssh","yamakawa@"+pair.getKey(),"java","-jar","/tmp/hyamakawa/slave.jar","1","/tmp/hyamakawa/maps/UM"+pair.getValue()+".txt");
			Process slaveStart = launchSlave.start();
			int f0 = slaveStart.waitFor();  
			InputStream is = slaveStart.getInputStream();
			InputStream es = slaveStart.getErrorStream();

			if (f0 == 0) {
				output(is);
				numShuffle +=1;	
			} else {
				output(es);
			}
		}
		if (numShuffle == 3) {
			System.out.println(" ====== Shuffle Finished ======");
		
		} else {
			System.out.println("Got an issue with the Shuffle");
		}
		
		System.out.println(" ======= Starting REDUCE ======");
		
		//map contenant les trois ordinateurs utilisés pour la suite
		for (Map.Entry<String, Integer> pair : ordis_fonctionnel.entrySet()) {
			
			System.out.println("Ordinateur: " + pair.getKey());
			ProcessBuilder launchReduce = new ProcessBuilder("ssh","yamakawa@"+pair.getKey(),"java","-jar","/tmp/hyamakawa/slave.jar","2");
			Process reduce = launchReduce.start();
			int f0 = reduce.waitFor();  
			InputStream is = reduce.getInputStream();
			InputStream es = reduce.getErrorStream();

			if (f0 == 0) {
				output(is);
				numShuffle +=1;	
			} else {
				output(es);
			}
		}
		if (numSlave == 3) {
			System.out.println(" ====== Reduce Finished ======");
		
		} else {
			System.out.println("Got an issue with the Reduce");
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

