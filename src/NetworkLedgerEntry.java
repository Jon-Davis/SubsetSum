import java.nio.channels.NetworkChannel;

/**
 * An entry in the network ledger
 * @author Jonathon Davis
 *
 */
public class NetworkLedgerEntry {
	public final int numberOfProcessors;
	public final String id;
	public final NetworkChannel assosiatedSocket;
	public boolean knownCompleted;
	
	/**
	 * @param numberOfProcessors
	 * @param id
	 * @param assosiatedSocket2
	 */
	public NetworkLedgerEntry(int numberOfProcessors, String id, NetworkChannel assosiatedSocket2) {
		this.numberOfProcessors = numberOfProcessors;
		this.id = id;
		this.assosiatedSocket = assosiatedSocket2;
		knownCompleted = false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Host: " + id + " - Number of processors = " + numberOfProcessors;
	}
}
