import java.util.Scanner;

/**
 * InputHandler reads in information from System.in and updates the program
 * accordingly
 * @author Jonathon Davis
 *
 */
public class InputHandler extends Thread {
	private final Scanner systemIn;
	
	public InputHandler(Scanner systemIn2) {
		systemIn = systemIn2;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while(systemIn.hasNextLine())
			SubsetSum.getInstance().addInput(systemIn.nextLine().split(" "));
	}

}
