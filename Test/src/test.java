import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class test {
	
	public static void main(String[] args) {
		String input = "4\n"
				+"bella\n "
				+ "de\n "
				+ "la\n "
				+ "muerte\n"; 
	    
		Scanner scan = new Scanner(System.in);
		int var = scan.nextInt();
		scan.nextLine();
		String myString;
	
		List<String> list = new ArrayList<String>();
		
		for (int i = 0 ; i<var;i++) {
			myString = scan.nextLine();
			list.add(myString);
		}
		
		System.out.println((list));
		
				
		for (int j = 0; j < list.size(); j++ ) {
			String str= list.get(j);
			String evenChar = "";
			String oddChar = "";
			
			for (int i = 0 ; i < str.length() ; i++) {
				
				if (i % 2 == 0) {
					evenChar += str.charAt(i);
					}	else	{
						oddChar += str.charAt(i);
					}
			}
		
			System.out.println(evenChar + " "+ oddChar);
			
			
		}
		scan.close();


	}
		
}

