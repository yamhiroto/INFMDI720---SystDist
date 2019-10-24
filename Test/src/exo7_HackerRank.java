import java.util.Arrays;
import java.util.Scanner;

public class exo7_HackerRank {
	private static final Scanner scanner = new Scanner(System.in);
	
	public static void main (String[] args) {
		int n = scanner.nextInt();
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        int[] arr = new int[n];

        String[] arrItems = scanner.nextLine().split(" ");
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        String myString;

        for (int i = 0; i < n; i++) {
            int arrItem = Integer.parseInt(arrItems[i]);
            int a = n - 1 - i;
            arr[a] = arrItem;
            
        }   
        String arrayConverted = Arrays.toString(arr);
        myString += arrayConverted + " "; 
        System.out.println(Arrays.toString(arr));
        scanner.close();
  	
	}
}
