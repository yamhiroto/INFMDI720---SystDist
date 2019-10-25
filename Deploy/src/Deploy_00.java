import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Deploy_00 {
	
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
			if (b == true) {
				InputStream is = p.getInputStream();
				System.out.println("l'ordinateur " + ordi + " est fonctionnel");
				
			} else {
				System.out.println("l'ordinateur "+ ordi + " n'a pas repondu au bout de 2 secondes");	
			}
		}
		
	}
}
