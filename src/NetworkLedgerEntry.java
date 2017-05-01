import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.NetworkChannel;

/**
 * An entry in the network ledger
 * 
 * @author Jonathon Davis
 *
 */
public class NetworkLedgerEntry {
	public int numberOfProcessors;
	public final String id;
	public final NetworkChannel assosiatedSocket;
	public boolean knownCompleted;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean isSelf;

	/**
	 * @param numberOfProcessors
	 * @param id
	 * @param assosiatedSocket2
	 */
	public NetworkLedgerEntry(int numberOfProcessors, String id, NetworkChannel assosiatedSocket, ObjectInputStream in, ObjectOutputStream out, boolean isSelf) {
		this.numberOfProcessors = numberOfProcessors;
		this.id = id;
		this.assosiatedSocket = assosiatedSocket;
		this.in = in;
		this.out = out;
		knownCompleted = false;
		this.isSelf = isSelf;
	}

	/**
	 * @return the isSelf
	 */
	public boolean isSelf() {
		return isSelf;
	}

	/**
	 * @param isSelf the isSelf to set
	 */
	public void setSelf(boolean isSelf) {
		this.isSelf = isSelf;
	}

	/**
	 * @return the knownCompleted
	 */
	public boolean isKnownCompleted() {
		return knownCompleted;
	}

	/**
	 * @param knownCompleted
	 *            the knownCompleted to set
	 */
	public void setKnownCompleted(boolean knownCompleted) {
		this.knownCompleted = knownCompleted;
	}

	/**
	 * @return the in
	 */
	public ObjectInputStream getIn() {
		return in;
	}

	/**
	 * @param in
	 *            the in to set
	 */
	public void setIn(ObjectInputStream in) {
		this.in = in;
	}

	/**
	 * @return the out
	 */
	public ObjectOutputStream getOut() {
		return out;
	}

	/**
	 * @param out
	 *            the out to set
	 */
	public void setOut(ObjectOutputStream out) {
		this.out = out;
	}

	/**
	 * @return the numberOfProcessors
	 */
	public int getNumberOfProcessors() {
		return numberOfProcessors;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the assosiatedSocket
	 */
	public NetworkChannel getAssosiatedSocket() {
		return assosiatedSocket;
	}

	/**
	 * @param numberOfProcessors
	 *            the numberOfProcessors to set
	 */
	public void setNumberOfProcessors(int numberOfProcessors) {
		this.numberOfProcessors = numberOfProcessors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Host: " + id + " - Number of processors = " + numberOfProcessors;
	}
}
