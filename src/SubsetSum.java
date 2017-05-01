import java.util.LinkedHashSet;
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
	private static int numberOfProcessors = 0;
	public ProgramArguments args = new ProgramArguments();
	
	
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
			System.out.println("sockets           - returns the current number of sockets");
		} else if (input[0].toLowerCase().equals("network")){
			System.out.println(networkHandler.getNetworkLedgerAsString());
		} else if (input[0].toLowerCase().equals("sockets")){
			System.out.println(networkHandler.numberOfSockets());
		}else {
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
		args.target = Double.parseDouble(input[1]);
		String[] argsAsStrings = input[2].split("\\[")[1].split("\\]")[0].split(",");
		LinkedHashSet<Double> doubleSet = new LinkedHashSet<>();
		for(String string : argsAsStrings)
			doubleSet.add(Double.parseDouble(string));
		int i = 0;
		args.set = new double[doubleSet.size()];
		for(Double d : doubleSet)
			args.set[i++] = d;
		networkHandler.setBeginRun(true);
		networkHandler.getSelector().wakeup();
		begin();
	}
	
	public void begin(){
		System.out.print("Begining new SubsetSum calculation, target = " + args.target + " array = ");
		for(double d : args.set)
			System.out.print(d +" ");
		System.out.println();
		networkHandler.calculateTaskSet();
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
