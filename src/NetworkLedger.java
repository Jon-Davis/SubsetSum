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
	public void addHost(int numberOfProcessors, String id, NetworkChannel assosiatedSocket){
		network.put(id, new NetworkLedgerEntry(numberOfProcessors,id,assosiatedSocket));
	}
	
	public int getNumberOfProcessors(String id){
		return network.get(id).numberOfProcessors;
	}
	
	public NetworkChannel getAssosiatedSocket(String id){
		return network.get(id).assosiatedSocket;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<NetworkLedgerEntry> iterator() {
		return network.values().iterator();
	}

}
