import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;


public class Master {

	public static void main(String[] args) throws IOException, InterruptedException{
		
		//crée un split du fichier d'entrée 
		splitFile("/home/yamhiroto/Desktop/input.txt");
		
		//crée une liste à partir du fichier "machines.txt", modifier le chemin si nécessaire 
		List<String> listeOrdinateurs = listMachine("/home/yamhiroto/Desktop/machines.txt");
		
		//liste qui va ajouter les trois ordinateurs utilisés pour la suite
		HashMap<String, Integer> ordis_fonctionnels = new HashMap<>(); 
		
		//fichier dans lequel les ordinateurs utilisés seront indiqués
		String path = "/home/yamhiroto/Desktop/machines_used.txt";
		FileWriter writer = new FileWriter(path);
		
		
	// 0 - Phase de préparation: envoi des Slave.jar et des fichiers sur les trois ordis,
		
		int i = 0;
		for (String ordi : listeOrdinateurs) {
			//sortir de la boucle si les trois fichiers ont été copiés, ce qui ne sera pas le cas au début!
			if (i==3) {
				System.out.println("");
				System.out.println("INFO : Les trois fichiers ont été copiés sur trois pc différents, plus besoin de tenter d'autres connexion SSH");
				break;
			}
			
			//Teste la connexion SSH avec chaque ordinateur de la liste 
			boolean ordinateur_actif = trySSHConnection(ordi);
			
			if (ordinateur_actif == true) { 
				//création du dossier temporaire hyamakawa
				int creation_Dossier_temp = createFolder("/tmp/hyamakawa", ordi);
				if (creation_Dossier_temp == 0) { 
					//copie du fichier slave.jar
					copy("slave.jar", "/home/yamhiroto/Desktop/","yamakawa@"+ ordi +":/tmp/hyamakawa/");
					
					//création du dossier temporaire splits
					int creation_Dossier_splits = createFolder("/tmp/hyamakawa/splits",ordi);
					if (creation_Dossier_splits ==0) {
						
						//copie du fichier Si.txt
						int copie_Fichier_Si = copy("S"+i+".txt","/home/yamhiroto/Desktop/","yamakawa@"+ordi+":/tmp/hyamakawa/splits/");
						if (copie_Fichier_Si == 0) {
							
							//dico key = nom ordi , value = numero fichier
							ordis_fonctionnels.put(ordi, i);
							writer.write(ordi + "\n");
							
							//incrémente i de 1 lorsqu'un fichier Sx.txt est copié.
							i = i+1;
							
							//on recommence jusque i = 3. La première condition sera alors vraie et on sortira de la boucle 
						}
					}
				}
			}
		}
		
		writer.close();
		
		
	// 1 - 	MAP
		
		System.out.println(" ======= Starting MAP ======");
		//timer
		long startMap_timer = System.currentTimeMillis();
		
		
		int numMap = 0;	
		for (Map.Entry<String, Integer> pair : ordis_fonctionnels.entrySet()) {
			//lancer le slave sur chaque ordi
			System.out.println("Ordinateur: " + pair.getKey()+" - Fichier: S"+pair.getValue() + ".txt");
			
			ProcessBuilder launchSlave = new ProcessBuilder("ssh","yamakawa@"+pair.getKey(),"java","-jar","/tmp/hyamakawa/slave.jar","0","/tmp/hyamakawa/splits/S"+pair.getValue()+".txt");
			Process slaveStart = launchSlave.start();
			int f0 = slaveStart.waitFor();  
			InputStream is = slaveStart.getInputStream();
			InputStream es = slaveStart.getErrorStream();

			if (f0 == 0) {
				output(is);
				copy("machines_used.txt","/home/yamhiroto/Desktop/","yamakawa@"+pair.getKey()+":/tmp/hyamakawa/");
				
				numMap +=1;	
			} else {
				output(es);
				
			}
		}
		
		if (numMap == 3) {
			System.out.println(" ====== MAP Finished ======");
		} else {
			System.out.println("Got an issue with the MAP");
		}
		//display hashmap ordis_fonctionnels
		System.out.println(ordis_fonctionnels.entrySet());
		
		
		//end timer
		long endMap_timer = System.currentTimeMillis();
		long totalMap_timer = endMap_timer - startMap_timer;
		
		System.out.println("");
		
		
		
		
		System.out.println(" ======= Starting SHUFFLE ======");
		long startShuffle_timer = System.currentTimeMillis();
		
		int numShuffle = 0;
		
		for (Map.Entry<String, Integer> pair : ordis_fonctionnels.entrySet()) {
			
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
		
		//end timer
		long endShuffle_timer = System.currentTimeMillis();
		long totalShuffle_timer = endShuffle_timer - startShuffle_timer;
		
		
		
		
		System.out.println(" ======= Starting REDUCE ======");
		long startReduce_timer = System.currentTimeMillis();
		
		//map contenant les trois ordinateurs utilisés pour la suite
		for (Map.Entry<String, Integer> pair : ordis_fonctionnels.entrySet()) {
			
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
		if (numMap == 3) {
			System.out.println(" ====== Reduce Finished ======");
		
		} else {
			System.out.println("Got an issue with the Reduce");
		}
		
		long endReduce_timer = System.currentTimeMillis();
		
		long totalReduce_timer = endReduce_timer - startReduce_timer;
		
		
		
		System.out.println(" ====== AFFICHAGE DES TIMERS ======");
		System.out.println("MAP: "+ totalMap_timer +"ms");
		System.out.println("SHUFFLE: "+ totalShuffle_timer+"ms");
		System.out.println("REDUCE: "+ totalReduce_timer+"ms");
		
		
		
		
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
	
	
	/* 
	This function takes as parameter the filepath for the file where computers are listed.
	it returns a list which will be used afterwards.
	*/
	public static List<String> listMachine(String path) throws IOException {
	
		List<String> ordis = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = br.readLine();
		while (line !=null) {
			ordis.add(line);
			line = br.readLine();
		}
		br.close();
		return ordis;
	}
	
	
	/* 
	This function takes as parameter the filepath of the main text file.
	it returns 3 separates file S0,S1,S2.txt which will be used afterwards.
	*/
		
	public static void splitFile(String filepath) throws IOException {
		
		File input = new File(filepath);
		
		//compter le nombre de ligne
		int linecount =0;
		BufferedReader br1 =new BufferedReader(new FileReader(input));
		String line1 = br1.readLine();
		
		while (line1 != null) {
			linecount += 1;
			line1 = br1.readLine();
		}
		
		br1.close();
		
		int line_per_file = linecount / 3;
		System.out.println("Le nombre de lignes par fichier S_.txt est : " + Integer.toString(line_per_file));
			
		//diviser le fichier en trois fichiers S0,S1,S2
		BufferedReader br = new BufferedReader(new FileReader(input));
		String line = br.readLine();
		
		int Num = 0;
		FileWriter writer;
		
		while (line != null) {
			
			//creer un fichier S_.txt (au max de trois)
			String newfileName = "/home/yamhiroto/Desktop/S" + Num  +".txt";
			int linecounter = 1;
			System.out.println("création du fichier S"+ Num + ".txt");
			
			writer = new FileWriter(newfileName);
			while (linecounter <= line_per_file) {
				writer.write(line + "\n");
				linecounter +=1;
				line=br.readLine();
			} 
			writer.close();			
			Num += 1;
	
		}
		br.close();	
	}
	
	/* 
	This function takes as parameter the name of one computer.
	it return a boolean to confirm whether the computer has replied back and can be used for the MapReduce
	*/
	public static boolean trySSHConnection(String ordi) throws IOException, InterruptedException {
		
		ProcessBuilder tryConnection = new ProcessBuilder("ssh", "yamakawa@"+ordi, "hostname");
		Process Connection = tryConnection.start();
		boolean c = Connection.waitFor(4,TimeUnit.SECONDS);
		
		if (c == true) {
			System.out.println("l'ordinateur " + ordi + " est fonctionnel");
			
			ProcessBuilder noCheck = new ProcessBuilder("ssh", "-o", "\"StrictHostKeyChecking=no\"", "yamakawa@"+ordi);
			Process copykey = noCheck.start();
			boolean d = copykey.waitFor(4,TimeUnit.SECONDS);
			if (d == true) {
				System.out.println("no check should be required anymore");
			}
			
		}else {
			System.out.println("l'ordinateur "+ ordi + " n'a pas répondu au bout de 2 secondes");	
		} 	
		return c;
	}
	
	/* 
	This function takes as parameter a path where a folder need to be created + the name of one computer.
	it return a boolean to confirm whether the folder has been created
	*/
	public static Integer createFolder(String FolderToCreate ,String ordi) throws IOException, InterruptedException {
		System.out.println("	création du dossier "+ FolderToCreate +" au sein de " +ordi);
		
		ProcessBuilder createFolder = new ProcessBuilder("ssh","yamakawa@"+ordi,"mkdir","-p", FolderToCreate);
		Process Foldercreation = createFolder.start();
		int f0 = Foldercreation.waitFor();
		
		// renvoie 0 si le dossier a été créé avec succès
		if(f0==1) {
			System.out.println("	ERREUR: la création du dossier "+ FolderToCreate+" a posé problème");
		}
		return f0;
	}
	
	/* 
	This function takes as parameter the file that need to be copied, its original path and destination path.
	it return a boolean to confirm whether the file has been copied
	*/
	public static Integer copy(String file,String file_path, String destination_path) throws IOException, InterruptedException {
		System.out.println("		copie du fichier "+ file+" depuis "+ file_path+ " vers "+ destination_path);
		
		ProcessBuilder copyFile = new ProcessBuilder("scp", file_path+file ,destination_path);
		Process Filecopy = copyFile.start();
		int f1 = Filecopy.waitFor();
		
		// renvoie 0 si le fichier a été copié avec succès
		if (f1 == 1) {
			System.out.println("		ERREUR la copie du fichier "+file+ " a posé problème");
		}
		return f1;
	}

	/* 
	This function takes as parameter an inputstream (standard or error)
	useful if you need to check whether a processbuilder has worked or not.
	it returns the message "written by the computer" line by line
	*/
	public static void output(InputStream inputStream) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String line = br.readLine();
		while (line  != null) {
			System.out.println(line);
			line = br.readLine();
		}
		br.close();
	}
	
	
	//FONCTION NON TERMINE
	public static void launchSlave(Object[] args ) throws IOException, InterruptedException {
		//launchSlave(MAP, ordis_fonctionnels, S, 1)
		
		//lancer le slave sur chaque ordi
		String mode = (String) args[0];
		@SuppressWarnings("unchecked")
		HashMap<String, Integer> ordis_fonctionnels= (HashMap<String, Integer>) args[1];
		
		String prefix_file = (String) args[2];
		String option = (String) args[3];
		
		
		System.out.println(" ======= Starting "+ mode +" ======");
		int counter = 0;
		
		for (Map.Entry<String, Integer> pair : ordis_fonctionnels.entrySet()) {
			
			System.out.println("Ordinateur: " + pair.getKey()+" - Fichier: " +prefix_file+pair.getValue() + ".txt");
			ProcessBuilder launchSlave = new ProcessBuilder("ssh","yamakawa@"+pair.getKey(),"java","-jar","/tmp/hyamakawa/slave.jar",option,"/tmp/hyamakawa/maps/"+prefix_file+pair.getValue()+".txt");
			Process slaveStart = launchSlave.start();
			int f0 = slaveStart.waitFor();  
			InputStream is = slaveStart.getInputStream();
			InputStream es = slaveStart.getErrorStream();

			if (f0 == 0) {
				output(is);
				counter +=1;	
			} else {
				output(es);
			}
		}
		if (counter == 3) {
			System.out.println(" ====== "+ mode +" Finished ======");		
		
		} else {
			System.out.println("Got an issue with the "+ mode);
		}

	}
	
}

