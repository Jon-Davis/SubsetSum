import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * SocketHandler reads in input from the network and updates the program
 * accordingly
 * 
 * @author Jonathon Davis
 */
public class NetworkHandler extends Thread {
	public static final int port = 10003;
	private Selector selector;
	private NetworkLedger ledger;
	private String address;
	private LinkedList<SocketChannel> pendingConnections;

	public NetworkHandler(int numberOfProcessors) {
		ledger = new NetworkLedger();
		pendingConnections = new LinkedList<>();
		try {
			selector = Selector.open();
			ServerSocketChannel listener = ServerSocketChannel.open();
			listener.socket().bind(new InetSocketAddress(NetworkHandler.port));
			listener.configureBlocking(false);
			listener.register(this.getSelector(), SelectionKey.OP_ACCEPT);
			address = InetAddress.getLocalHost().getHostAddress();
			ledger.addHost(numberOfProcessors, address, listener);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(SubsetSum.isRunning()){
			int num = 0;
			
			while(pendingConnections.size() > 0){
				try {
					pendingConnections.removeFirst().register(selector, SelectionKey.OP_READ);
				} catch (ClosedChannelException e1) {
					e1.printStackTrace();
				}
			}
			
			try {
				num = selector.select();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(num==0)
				continue;
			
			Set keys = selector.selectedKeys();
			Iterator it = keys.iterator();
			while (it.hasNext()){
				SelectionKey key = (SelectionKey)it.next();
				if(!key.isAcceptable())
					continue;
				if(key.interestOps()==SelectionKey.OP_ACCEPT){
					//register the new connection with the Selector
					try {
						((ServerSocketChannel) key.channel()).accept().register(this.getSelector(), SelectionKey.OP_READ);
						System.out.println("new connection made");
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (key.interestOps()==SelectionKey.OP_READ){
					//read the message from the socket
					read((SocketChannel) key.channel());
				}
			}
			
			keys.clear();
		}
		
	}
	
	public void read(SocketChannel selectableChannel){
		 try {
			ObjectInputStream ois = new ObjectInputStream(selectableChannel.socket().getInputStream());
			ObjectOutputStream  oos = new ObjectOutputStream(selectableChannel.socket().getOutputStream());
			Message message = (Message) ois.readObject();
			//determine the type of message 
			if(message.type == Message.CONNECT){
				ledger.addHost((Integer) message.argument, selectableChannel.socket().getRemoteSocketAddress().toString(), selectableChannel);
				//TODO: Send a Message of Type HOST_INFO, with this computers info
			} else if (message.type == Message.NEW_CONNECTION) {
				ledger.addHost((Integer) message.argument, selectableChannel.socket().getRemoteSocketAddress().toString(), selectableChannel);
				// TODO: Compile list of other machines in the network, send this in a message of type NETWORK_INFO
				System.out.println("Recieved new connection from " + selectableChannel.socket().getRemoteSocketAddress().toString());
			} else if (message.type == Message.NETWORK_INFO) {
				// TODO: Given a list of NETWORK_INFO connect to, and collect information from all other hosts in the network
			} else if (message.type == Message.RUN){
				// TODO: Given a list of numbers to compute, 
				// calculate the permutations this host is responsible for given the NetworkLedger
				// Then run the calculations
			} else if (message.type == Message.NOTIFY) {
				// TODO: The src of this message is notifying this host that it has completed all of its assigned work
			} else if (message.type == Message.REQUEST) {
				
			} else if (message.type == Message.REQUEST_RESPONSE){
				
			}
			ois.close();
			oos.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * @return the selector
	 */
	public Selector getSelector() {
		return selector;
	}
	
	public void connect(String ipAddress, int numberOfProcessors){
		System.out.println("opening connections with " + ipAddress);
		SocketChannel socketChannel;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(true);
			socketChannel.connect(new InetSocketAddress(ipAddress, port));
			socketChannel.configureBlocking(false);
			pendingConnections.add(socketChannel);
			selector.wakeup();
			ObjectOutputStream  oos = new ObjectOutputStream(socketChannel.socket().getOutputStream());
			Message newConnect = new Message(this.address, ipAddress, Message.NEW_CONNECTION, numberOfProcessors);
			oos.writeObject(newConnect);
			oos.close();
			System.out.println("Established connection with " + ipAddress);
		} catch (IOException e) {
			System.out.println("Failed to connect to " +ipAddress);
			return;
		}
		
	}
	
}
