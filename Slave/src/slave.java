import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;


public class slave {
		
	public static void main(String[] args) throws InterruptedException, IOException {
		
		String mode = args[0];
		// 0 = Map
		// 1 = Shuffle
		// 2 = Reduce 
		
		if (mode.equals("0")) {
			
			String input = args[1]; //give location of file Sx.txt  in /tmp/hyamakawa/split/
			
			//création du dossier temporaire hyamakawa
			String path = "/tmp/hyamakawa/maps";
			createFolder(path);
			
			//to retrieve number of file Sx.txt  from the path
			char num = input.charAt(input.length()- 5);
			String output = path + "/UM" + num +".txt";
			map(input,output);
			
			System.out.println("		MESSAGE DU SLAVE : fichier UM" +num +".txt créé avec succès");
			}
		
		
		if(mode.equals("1")) {
			String input = args[1]; //give location of filse UMx.txt in /tmp/hyamakawa/naps
			
			shuffle(input);
			System.out.println("		MESSAGE DU SLAVE : Shuffle terminé avec succès");
					
		}
		
		if(mode.equals("2")) {
			String shufflesreceived_path = "/tmp/hyamakawa/shufflesreceived/";
			
			reduce(shufflesreceived_path);
			System.out.println("		MESSAGE DU SLAVE : Reduce terminé avec succès");
		}
		
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

		InputStream is = Filecopy.getInputStream();
		InputStream es = Filecopy.getErrorStream();

		if (f1 == 0) {
			output(is);	
		} else {
			System.out.println("error :");
			output(es);
		}
		return f1;
	}
	
	/* 
	This function takes as parameter a path where a folder need to be created.
	it return a boolean to confirm whether the folder has been created
	*/
	public static void createFolder(String path) throws IOException, InterruptedException {
		
		System.out.println("	création du dossier " +path);
		ProcessBuilder createFolder = new ProcessBuilder("mkdir","-p",path);
		Process Foldercreation = createFolder.start();
		int f0 = Foldercreation.waitFor();  
		
		//renvoie 0 si le dossier a été créé avec succès
		if (f0 == 0) { 
			System.out.println("	dossier " + path + " créé avec succès");
		}
	}
	
	/* 
	This function takes as parameter a path where a folder need to be created + the name of one computer.
	it return a boolean to confirm whether the folder has been created
	*/
	public static Integer createFolder_elsewhere(String FolderToCreate ,String ordi) throws IOException, InterruptedException {
		System.out.println("	création du dossier "+ FolderToCreate +" au sein de " +ordi);
		
		ProcessBuilder createFolder = new ProcessBuilder("ssh","yamakawa@"+ordi,"mkdir","-p", FolderToCreate);
		Process Foldercreation = createFolder.start();
		int f0 = Foldercreation.waitFor();
		
		// renvoie 0 si le dossier a été créé avec succès
		return f0;
	}
	
	/* 
	This function takes as parameter the filepath which will be used for the map and the output path.
	it doesn't return anything but will create the file needed afterwards for the shuffle
	*/
	public static void map(String input, String output) throws IOException {
			
		BufferedReader br = new BufferedReader(new FileReader(input));
		FileWriter writer = new FileWriter(output);
		
		
		String line = br.readLine();
		while (line != null) {
			for (String word : line.split(" ")) {	
				writer.write(word +" "+ 1 + "\n");	
				line = br.readLine();
				}	
			
		}
		br.close();
		writer.close();
	}	
	
	/* 
	This function takes as parameter the filepath which will be used for the shuffle.
	it doesn't return anything but will create the file needed afterwards for the reduce
	*/
	public static void shuffle(String input) throws IOException, InterruptedException {

		//envoie du fichier sur les differents ordi
		//dico qui va associer un numero aux trois ordinateurs utilisés
		HashMap<Integer,String> ordis_fonctionnel = new HashMap<>(); 
		
		//Creation de dossier shuffle et shuffle received
		BufferedReader ordi_utilise = new BufferedReader(new FileReader("/tmp/hyamakawa/machines_used.txt"));
		String ordi = ordi_utilise.readLine();
		int i = 0;
		while (ordi !=null) {
			String path_to_shuffle = "/tmp/hyamakawa/shuffle/";
			createFolder_elsewhere(path_to_shuffle, ordi);
			
			String path_to_shufflesreceived = "/tmp/hyamakawa/shufflesreceived/";
			createFolder_elsewhere(path_to_shufflesreceived, ordi);
			
			//fll the dictionarry
			ordis_fonctionnel.put(i, ordi);
			ordi = ordi_utilise.readLine();
			i = i+1;
		}
		
		ordi_utilise.close();
	
		// input = /tmp/hyamakawa/maps/UM1.txt
		
		
		BufferedReader br = new BufferedReader(new FileReader(input));
		
		
		//pour recuperer nom du pc
		String addr = java.net.InetAddress.getLocalHost().getHostName();
		
		String line = br.readLine();
		while (line != null) {
			//choisir le premier mot de chaque ligne et afficher son hash
			String word = line.split(" ")[0];	
			//System.out.println("le hashcode de "+ word +" est: " + word.hashCode());
			
			//nom du fichier et du chemin
			String fileName = word.hashCode() +"_"+ addr + ".txt";
			String filePath = "/tmp/hyamakawa/shuffle/" + fileName;
				
			File file = new File(filePath);
			
			if (file.isFile() ) {
				//System.out.println(fileName + " existe déjà, on écrira dedans");
				//si le fichier existe, écrire dedans
				FileWriter writer = new FileWriter(file,true);				
				writer.write(word +" "+ 1 + "\n");				
				line = br.readLine();
				writer.close();
				
			}else {
				//System.out.println("création du fichier "+fileName);
				//si le fichier n'existe pas, le créer
				FileWriter writer = new FileWriter(filePath);
				writer.write(word +" "+ 1 + "\n");	
				line = br.readLine();
				writer.close();
				
		
			}
			//calcul du modulo pour savoir sur quel machine envoyer le fichier
			int numeroMachine = word.hashCode() % 3;
			//System.out.println(numeroMachine);
			for (Map.Entry<Integer, String> pair : ordis_fonctionnel.entrySet()) {
				if (pair.getKey() == numeroMachine) {
					copy(fileName, "/tmp/hyamakawa/shuffle/", "yamakawa@"+pair.getValue()+":/tmp/hyamakawa/shufflesreceived/");
				}
				
			}
			
		}
		
		br.close();
		
		
	}
	
	/* 
	This function takes as parameter the filepath to the folder shufflesreceived which will be used for the reduce.
	it doesn't return anything but will create the file needed 
	*/
	
	public static void reduce(String shufflesreceived_path) throws IOException, InterruptedException {
		
		//creation du dossier
		createFolder("/tmp/hyamakawa/reduces");
		
		HashMap<String,Integer> wordcount = new HashMap<String,Integer>();
		
		// Reading SM*.txt file and counting number of times key appear in the doc
		File folder = new File(shufflesreceived_path);
		//liste tous les files dans le dossier
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile()) {
		        //System.out.println(file.getName());
		        //System.out.println(file.toString());
		    	BufferedReader br = new BufferedReader(new FileReader(file.toString()));
				String line = br.readLine();
				while (line != null) {
					String word =line.split(" ")[0];
					if(!wordcount.containsKey(word)) {
						wordcount.put(word, 1) ;
						line =br.readLine();
					} else { 
						wordcount.put(word, wordcount.get(word)+1);
						line =br.readLine();
					}
					
				}
				br.close();
			}
		    
		}
		System.out.println(wordcount.entrySet());
		
		
		// Pour chaque couple (mot,occurence) du dico, creer un fichier mot.hashcode().txt dans le dossier reduces et ecrire dedans
		FileWriter writer;
		for (Entry<String,Integer> pair : wordcount.entrySet()) {
			String newfileName = "/tmp/hyamakawa/reduces/" + pair.getKey().hashCode() +".txt";
			writer = new FileWriter(newfileName);
			writer.write(pair.getKey() + " " + pair.getValue());
			writer.close();
			
			//System.out.println("fichier "+ pair.getKey().hashCode() + ".txt créé avec succès");
		}
		
		
		
	}
	
	
}
	
