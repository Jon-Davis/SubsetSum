import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.NetworkChannel;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * A collection of the different types of machines
 * in the network and their capabilities
 * @author Jonathon Davis
 *
 */
public class NetworkLedger implements Iterable<NetworkLedgerEntry>{
	private TreeMap<String,NetworkLedgerEntry> network;
	
	public NetworkLedger(){
		network = new TreeMap<>();
	}
	
	/**
	 * Adds a host to the ledger
	 */
	public void addHost(int numberOfProcessors, String id, NetworkChannel assosiatedSocket,ObjectInputStream in, ObjectOutputStream out){
		network.put(id, new NetworkLedgerEntry(numberOfProcessors,id,assosiatedSocket,in,out));
	}
	
	public int getNumberOfProcessors(String id){
		return network.get(id).numberOfProcessors;
	}
	
	public NetworkChannel getAssosiatedSocket(String id){
		return network.get(id).assosiatedSocket;
	}
	
	public boolean contains(String id){
		return network.containsKey(id);
	}
	
	public void remove(String id){
		network.remove(id);
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<NetworkLedgerEntry> iterator() {
		return network.values().iterator();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		for(NetworkLedgerEntry entry : network.values()){
			string.append(entry.toString() + System.lineSeparator());
		}
		return string.toString();
	}

	/**
	 * @param ipAddress
	 * @return
	 */
	public NetworkLedgerEntry getEntry(String ipAddress) {
		return network.get(ipAddress);
	}

}
