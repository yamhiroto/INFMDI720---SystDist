import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;


public class Master00 {
	
	public static void output(InputStream inputStream) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String line = br.readLine();
		while (line  != null) {
			System.out.println(line);
			line = br.readLine();	
		}
	}
    
	
	public static void main(String[] args) throws IOException, InterruptedException{
		
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/hyamakawa/slave.jar");
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
        
	
