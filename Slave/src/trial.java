import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class trial {


	public static void main(String[] args) throws InterruptedException, IOException {
		BufferedReader objReader = null;
		String strCurrentLine;
		objReader = new BufferedReader(new FileReader("sante_publique.txt"));
		
		while ((strCurrentLine = objReader.readLine()) != null) {
			System.out.println(strCurrentLine);
		}
		objReader.close();
   
	}
 
}
  
 