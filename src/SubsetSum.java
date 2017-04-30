import java.util.Scanner;

/**
 * Singleton program
 * @author Jonathon Davis
 *
 */
public class SubsetSum {
	private static final SubsetSum instance = new SubsetSum();
	private static NetworkHandler networkHandler;
	private static InputHandler inputHandler;
	private static boolean running;
	private String[] input;
	private static int mode = 0;
	private static int numberOfProcessors = 0;
	
	
	public static void main(String[] args){
		Scanner systemIn = new Scanner(System.in);
		System.out.println("How many processors:");
		numberOfProcessors = Integer.parseInt(systemIn.nextLine());
		System.out.println("Using " + numberOfProcessors + " processors. Starting program SubsetSum:");
		running = true;
		networkHandler = new NetworkHandler(numberOfProcessors);
		inputHandler = new InputHandler(systemIn);
		inputHandler.start();
		networkHandler.start();
		while(running){
			instance.run();
		}
		
	}
	


	/**
	 * This method blocks the main thread until input from
	 * either the InputHandler or SocketHandler has been given 
	 */
	public synchronized void run(){
		//wait for input
		try {
			this.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//process input
		if(input[0].toLowerCase().equals("compute")){
			start();
		} else if (input[0].toLowerCase().equals("connect")){
			networkHandler.connect(input[1]);
		} else if (input[0].toLowerCase().equals("help")){
			System.out.println("Commands for SubsetSum:" + System.lineSeparator());
			System.out.println("connect ipaddress - connects this computer with an exsisting network given an ip address in that network");
			System.out.println("compute # [#,#,#] - computes the given array, checking to see if any subset of the array add to the given digit");
			System.out.println("network           - returns the current network ledger");
		} else if (input[0].toLowerCase().equals("network")){
			System.out.println(networkHandler.getNetworkLedgerAsString());
		} else {
			System.out.println(input[0] + " is not a valid command, type help for a list of valid commands.");
		}
	}
	
	/**
	 * Wakes up the main method and passes it the input
	 * @param string
	 */
	public synchronized void addInput(String[] input){
		this.input = input;
		instance.notify();
	}
	
	/**
	 * Begins a computation
	 */
	public synchronized void start(){
		
	}

	/**
	 * @return the instance
	 */
	public static SubsetSum getInstance() {
		return instance;
	}



	/**
	 * @return the running
	 */
	public static boolean isRunning() {
		return running;
	}
	
	

}
