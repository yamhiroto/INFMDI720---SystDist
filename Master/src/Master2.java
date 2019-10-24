import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.crypto.interfaces.PBEKey;

public class Master {
    
	public static void main(String[] args) throws IOException, InterruptedException {
		
		//Process pb = new ProcessBuilder("java", "-jar", "/tmp/hyamakawa/slave.jar").start();
		
		Process pb = new ProcessBuilder("java", "-jar", "/home/yamhiroto/Desktop/slave.jar").start();
		
        BufferedReader bReader = new BufferedReader(new InputStreamReader(pb.getInputStream()));
        BufferedReader bReader_error = new BufferedReader(new InputStreamReader(pb.getErrorStream()));
        
        boolean running = true;
        
        while(running) {
        	
        	try{
        		boolean stillRunning = pb.waitFor(2, TimeUnit.SECONDS);
        
        		
        		String line = bReader.readLine();
        		System.out.println(line);
        		String line_error = bReader_error.readLine();
        	
        		if (line !=null) {
        			System.out.println("Standard output: ");
        			while (line != null) {
        				System.out.println(line);
        				line = bReader.readLine();
        				
        			}
        
        			System.out.println("Error output: ");
        			while (line_error != null ) {
        				System.out.println(line);
        				}
        			
        		} else {
        			
        			pb.destroy();
        			System.out.println("Too long ! Timeout");
        			break;
        		}
        		
        	}catch (InterruptedException e) {
        		e.printStackTrace();
        	}		
		 
        }
	}
}
