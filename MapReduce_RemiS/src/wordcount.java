
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class wordcount {

	public static void main(String[] args) {
		// method that takes as a filename as an input and produces a HashMaP<Key,Value> as an output
		//public static HashMap<String, Integer> countOccurences(String filename) throws IOException {
			
		//STEP 1: COUNT OCCURENCES FOR EACH WORD
			// start timer for this step
			long startTime_count = System.currentTimeMillis();
			
			//declare a new ArrayList called Text and a HashMap called Dico. We need to use an ArrayList since we  will add element
			List<String> Text = new ArrayList<>();
			HashMap<String, Integer> dico = new HashMap<String, Integer>();
			
			
			//verify that the file can be read or send a message if the file is incorrect
			try {
				// read the file
				Text = Files.readAllLines(Paths.get("CC-MAIN-20170322212949-00140-ip-10-233-31-227.ec2.internal.warc.wet"));
				
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}

			//loop on each item (which are lines of the file) of the ArrayList Text
			
			for (String line : Text ) {	
				
				//for each line, create an array called words and split at white space
				String[] words = line.split(" ");
					
					//loop on each Array words, 
					for (int i=0;i<words.length;i++) {
						
						//words[i] is the word in the position no. i   within the Array words.
						//declare word_lowChar and assign it the lower cased version of words[i]
						String word_lowChar = words[i].toLowerCase();
						
						// if word_lowChar isn't a key of dico, create a key/value pair with key = word_lowChar and value = 1 
						if(!dico.containsKey(word_lowChar)) {
							dico.put(word_lowChar, 1);	
							
						//if it's a key of dico, increment value by 1 
						} else { dico.put(word_lowChar, dico.get(word_lowChar)+1);
					
						}
					}
			}
			/*
			for (String key : dico.keySet()) {
				System.out.println(key +" " + dico.get(key));
			}
			*/	
			
			// End the timer for STEP 1
			long endTime_count = System.currentTimeMillis();	
			
			
		//STEP 2 ; SORTING WORDS
			//start timer for step 2
			long startTime_sort = System.currentTimeMillis();
			
			//create a list based on the key,value pair from the HashMap dico above
			List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(dico.entrySet());
			
			//Sort the list based on values
			Collections.sort(list, new Comparator<Entry<String,Integer>>() {
				public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
					// We sort by value
					int result = o2.getValue().compareTo(o1.getValue());
					if (result != 0) {
						return result;
					}
					//if values are equal ,i.e. result == 0, we will sort alphabetically by keys.
						else {
							return o1.getKey().compareTo(o2.getKey());	
					}
					
				}	
				
			});
			
			//display results key and value
			for (int i = 0 ; i< 50 ; i++) {
				System.out.println(list.get(i).getKey() + " " + list.get(i).getValue());
			}
			
			//stop timer for step 2
			long endTime_sort = System.currentTimeMillis();
		
			//total time for step 1 : count
			long totalTime_count = endTime_count - startTime_count;
			//total time for step 2 : sort
			long totalTime_sort = endTime_sort - startTime_sort;
			
			System.out.println("Time to count occurence of words :" + totalTime_count + "ms");
			System.out.println("Time to sort words :" + totalTime_sort + "ms");
			
	
	}	
}
