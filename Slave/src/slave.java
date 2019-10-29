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
			map(input);
		}
		
		
		if(mode.equals("1")) {
			String input = args[1]; //give location of filse UMx.txt in /tmp/hyamakawa/naps
			shuffle(input);								
		}
		
		
		if(mode.equals("2")) {
			String shufflesreceived_path = "/tmp/hyamakawa/shufflesreceived/";
			reduce(shufflesreceived_path);
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
		
		// f1 return 0 if the file has been copied successfully

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
		
		//return 0 if folder has been created successfully
		if (f0 == 0) { 
			System.out.println("	dossier " + path + " créé avec succès");
		}else {
			System.out.println("	Error durant creation dossier " + path);
		}
	}
	
	/* 
	This function takes as parameter a path where a folder need to be created + the name of one computer.
	it return a boolean to confirm whether the folder has been created
	*/
	public static Integer createFolder_elsewhere(String FolderToCreate ,String ordi) throws IOException, InterruptedException {
		
		//check if folder exists
		//ProcessBuilder checkFolder = new ProcessBuilder("ssh","yamakawa@"+ordi,"-d", FolderToCreate, "&&","echo","exists");
		//Process Folderchecking = checkFolder.start();
		//int f0 = Folderchecking.waitFor();
		//InputStream is = Folderchecking.getInputStream();
		//if (f0 == 0) {
		//	output(is);
		//}
		System.out.println("	création du dossier "+ FolderToCreate +" au sein de " +ordi);
		
		ProcessBuilder createFolder = new ProcessBuilder("ssh","yamakawa@"+ordi,"mkdir","-p", FolderToCreate);
		Process Foldercreation = createFolder.start();
		int f1 = Foldercreation.waitFor();
		
		// return 0 if folder has been created successfully
		/*
		if (f1 == 0) { 
			System.out.println("	dossier " + FolderToCreate + " créé avec succès");
		}else {
			System.out.println("	Error durant creation dossier " + FolderToCreate);
		}
		*/
		return f1;
	}
	

	/* 
	This function takes as parameter the filepath which will be used for the map and the output path.
	it doesn't return anything but will create the file needed afterwards for the shuffle
	*/
	public static void map(String input) throws IOException, InterruptedException {
		
		//create folder maps 
		String path = "/tmp/hyamakawa/maps";
		createFolder(path);
		
		//to retrieve number of file Sx.txt  from the path. the 5 is to remove ".txt" at the end
		char num = input.charAt(input.length()- 5);
		String output = path + "/UM" + num +".txt";
		
		//read the file Sx.txt and write in UMx.txt all the words with occurence as 1 
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
		
		
		System.out.println("		MESSAGE DU SLAVE : fichier UM" +num +".txt créé avec succès");
	}	
	
	/* 
	This function takes as parameter the filepath which will be used for the shuffle.
	it doesn't return anything but will create the file needed afterwards for the reduce
	*/
	public static void shuffle(String input) throws IOException, InterruptedException {

		
	//STEP 1 - CREATE FOLDERS shuffle AND shufflesreceived ON EACH COMPUTER
		System.out.println("Creation dossier shuffles ans shufflesreceived");
		//hashmap with the three computers which will be used for the shuffle
		HashMap<Integer,String> ordis_fonctionnel = new HashMap<>(); 
		
		//read the file "machines_used" sent by the MASTER to all the relevant computers 
		BufferedReader ordi_utilise = new BufferedReader(new FileReader("/tmp/hyamakawa/machines_used.txt"));
		String ordi = ordi_utilise.readLine();
		int i = 0;
		while (ordi !=null) {
			//SLAVE creates a folder shuffle and shufflesreceived on each computer
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
		
	
	//STEP 2 - CREATE ALL THE hascode_computername.txt FROM EACH UMx.txt
		
		// input = /tmp/hyamakawa/maps/UMx.txt
		BufferedReader br = new BufferedReader(new FileReader(input));
		//to retrieve computer's name
		String computer_name = java.net.InetAddress.getLocalHost().getHostName();
		
		String line = br.readLine();
		while (line != null) {
			//create a file hashcode_computer.txt for each word of the file UMx.txt
			String word = line.split(" ")[0];	
			//System.out.println("le hashcode de "+ word +" est: " + word.hashCode());
			String fileName = word.hashCode() +"_"+ computer_name + ".txt";
			String filePath = "/tmp/hyamakawa/shuffle/" + fileName;
			
			File file = new File(filePath);
			if (file.isFile()) {
				//if file already exists, i.e. same word several times, write in  the same file
				//System.out.println(fileName + " existe déjà, on écrira dedans");
				FileWriter writer = new FileWriter(file,true);				
				writer.write(word +" "+ 1 + "\n");				
				line = br.readLine();
				writer.close();
				
			}else {
				//if file doesn't exit, i.e. if it's a new word, create a new file 
				//System.out.println("création du fichier "+fileName);
				FileWriter writer = new FileWriter(filePath);
				writer.write(word +" "+ 1 + "\n");	
				line = br.readLine();
				writer.close();
			}
		}
		br.close();
	
	//STEP 3 - SEND FILES TO RELEVANT COMPUTERS 
		
		//list files within folder shuffle
		File folder = new File("/tmp/hyamakawa/shuffle/");
		File[] listOfFiles = folder.listFiles();
		
		//iterate over each file of folder shuffle
		for (File hashfile : listOfFiles) {
			if (hashfile.isFile()) {
				//remove path to only keep hash_computer.txt
				String hash_nopath= hashfile.toString().split("shuffle/")[1];
				//retrieve word's hash from the file's name
				String hash = hash_nopath.toString().split("_")[0];
					
				//calculate modulo to know which computer files need to be sent to
				int numeroMachine = Integer.parseInt(hash) % 3;
				//System.out.println(numeroMachine);
				
				//the modulo numeroMachine will match a key of the hashmap ordis_fonctionnel, send the file to the computer associated to that key 
				copy(hash_nopath.toString(), "/tmp/hyamakawa/shuffle/", "yamakawa@"+ordis_fonctionnel.get(numeroMachine)+":/tmp/hyamakawa/shufflesreceived/");	
			}
		}
		
		
		System.out.println("		MESSAGE DU SLAVE : Shuffle terminé avec succès");
	}
	
	/* 
	This function takes as parameter the filepath to the folder shufflesreceived which will be used for the reduce.
	it doesn't return anything but will create the file needed 
	*/
	
	public static void reduce(String shufflesreceived_path) throws IOException, InterruptedException {
		
		//create folder reduces
		createFolder("/tmp/hyamakawa/reduces");
		
		
	//STEP 1 - CREATE THE WORDCOUNT
		
		//hashmap used to do the wordcount in the folder shufflereceived
		HashMap<String,Integer> wordcount = new HashMap<String,Integer>();
		
		//iterate over files within folder shufflesreceived
		File folder = new File(shufflesreceived_path);
		File[] listOfFiles = folder.listFiles();
		
		//read each file's content to build the wordcount
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
		
		
		
	//STEP 2 - CREATE FILES FOR WORD's OCCURENCE WITHIN FOLDER reduces
		
		// for each pair (word, occurrence) within the wordcount, create a word's hashcode.txt within the reduces folder
		FileWriter writer;
		
		for (Entry<String,Integer> pair : wordcount.entrySet()) {
			String newfileName = "/tmp/hyamakawa/reduces/" + pair.getKey().hashCode() +".txt";
			writer = new FileWriter(newfileName);
			writer.write(pair.getKey() + " " + pair.getValue());
			writer.close();
			//System.out.println("fichier "+ pair.getKey().hashCode() + ".txt créé avec succès");
		}
	
		System.out.println("		MESSAGE DU SLAVE : Reduce terminé avec succès");
	}
	
	
}
	
