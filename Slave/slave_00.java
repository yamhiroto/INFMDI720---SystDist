
public class slave {
	public static void main(String[] Args) throws InterruptedException {
		
		int a = 3+5;
		long startTime = System.currentTimeMillis();
		Thread.sleep(10000);
		long endTime = System.currentTimeMillis();
		long totalTime = (endTime - startTime)/1000;
		
		System.out.println("Temps d'exécution : " + totalTime + "s");
		System.out.println("3 + 5 = " + a);
	}
}
