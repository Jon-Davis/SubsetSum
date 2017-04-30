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
	private LinkedList<SocketChannel> sockets;
	private ServerSocketChannel listener;
	private int numberOfProcessors;

	public NetworkHandler(int numberOfProcessors) {
		ledger = new NetworkLedger();
		sockets = new LinkedList<>();
		this.numberOfProcessors = numberOfProcessors;
		try {
			selector = Selector.open();
			listener = ServerSocketChannel.open();
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
		while (SubsetSum.isRunning()) {
			int num = 0;

			try {
				selector.close();
				selector = Selector.open();
				listener.register(selector, SelectionKey.OP_ACCEPT);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			for(SocketChannel socket : sockets) {
				try {
					socket.configureBlocking(false);
					socket.register(selector, SelectionKey.OP_READ);
				} catch (ClosedChannelException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				num = selector.select();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (num == 0)
				continue;

			Set keys = selector.selectedKeys();
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();
				if (key.interestOps() == SelectionKey.OP_ACCEPT && key.isAcceptable()) {
					// register the new connection with the Selector
					try {
						sockets.add(((ServerSocketChannel) key.channel()).accept());
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (key.interestOps() == SelectionKey.OP_READ) {
					// read the message from the socket
					SocketChannel readChannel = (SocketChannel) key.channel();
					key.cancel();
					try {
						readChannel.configureBlocking(true);
						read(readChannel);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			keys.clear();
		}

	}

	public void read(SocketChannel selectableChannel) {
		try {
			ObjectInputStream ois = new ObjectInputStream(selectableChannel.socket().getInputStream());
			Message message = (Message) ois.readObject();
			ObjectOutputStream oos = new ObjectOutputStream(selectableChannel.socket().getOutputStream());
			// determine the type of message
			if (message.type == Message.CONNECT) {
				ledger.addHost((Integer) message.argument, message.src, selectableChannel);
				Message hostInfo = new Message(this.address, message.src, Message.HOST_INFO, numberOfProcessors);
				oos.writeObject(hostInfo);
				System.out.println("Recieved new connection from " + message.src);
			} else if (message.type == Message.NEW_CONNECTION) {
				ledger.addHost((Integer) message.argument, message.src, selectableChannel);
				Message hostInfo = new Message(this.address, message.src, Message.HOST_INFO, numberOfProcessors);
				oos.writeObject(hostInfo);
				LinkedList<String> networkIDs = new LinkedList<>();
				for(NetworkLedgerEntry entry : ledger)
					networkIDs.add(entry.id);
				Message networkInfo = new Message(this.address, message.src, Message.NETWORK_INFO, networkIDs);
				oos.writeObject(networkInfo);
				System.out.println("Recieved new connection from " + message.src);
			} else if (message.type == Message.NETWORK_INFO) {
				System.out.println("Recieved network info.");
				LinkedList<String> networkIDs = (LinkedList<String>) message.argument;
				for(String id : networkIDs)
					if(!ledger.contains(id))
						connect(id);
			} else if (message.type == Message.HOST_INFO) {
				ledger.addHost((Integer) message.argument, message.src, selectableChannel);
				System.out.println("Recieved new connection from " + message.src);
			} else if (message.type == Message.RUN) {
				// TODO: Given a list of numbers to compute,
				// calculate the permutations this host is responsible for given
				// the NetworkLedger
				// Then run the calculations
			} else if (message.type == Message.NOTIFY) {
				// TODO: The src of this message is notifying this host that it
				// has completed all of its assigned work
			} else if (message.type == Message.REQUEST) {

			} else if (message.type == Message.REQUEST_RESPONSE) {

			}
		} catch (IOException | ClassNotFoundException e) {
			String id = selectableChannel.socket().getRemoteSocketAddress().toString().split("/")[1].split(":")[0];
			ledger.remove(id);
			System.out.println("Closing connection with " + id);
			sockets.remove(selectableChannel);
			try {
				selectableChannel.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
	public String getNetworkLedgerAsString(){
		return ledger.toString();
	}

	/**
	 * @return the selector
	 */
	public Selector getSelector() {
		return selector;
	}

	public void connect(String ipAddress) {
		System.out.println("opening connections with " + ipAddress);
		SocketChannel socketChannel;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(true);
			socketChannel.connect(new InetSocketAddress(ipAddress, port));
			sockets.add(socketChannel);
			ObjectOutputStream oos = new ObjectOutputStream(socketChannel.socket().getOutputStream());
			Message newConnect = new Message(this.address, ipAddress, Message.NEW_CONNECTION, numberOfProcessors);
			oos.writeObject(newConnect);
			selector.wakeup();
		} catch (IOException e) {
			System.out.println("Failed to connect to " + ipAddress);
			return;
		}
	}

}
