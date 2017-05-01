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
	private boolean beginRun = false;

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
			ledger.addHost(numberOfProcessors, address, listener, null, null, true);
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

			for (SocketChannel socket : sockets) {
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

			if (num == 0 && isBeginRun() == false)
				continue;

			Set keys = selector.selectedKeys();
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();
				if (key.interestOps() == SelectionKey.OP_ACCEPT && key.isAcceptable()) {
					System.out.println(key.isAcceptable());
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
			if(isBeginRun() == true){
				for(NetworkLedgerEntry entry : ledger){
					if(entry.isSelf() == false){
						Message run = new Message(address,entry.getId(),Message.RUN,SubsetSum.getInstance().args);
						ObjectOutputStream oos = entry.getOut();
						try {
							oos.writeObject(run);
							oos.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		

	}

	public void read(SocketChannel selectableChannel) {
		try {
			String id = selectableChannel.socket().getRemoteSocketAddress().toString().split("/")[1].split(":")[0];
			System.out.println("Recieved message from " + id);
			if (!ledger.contains(id)) {
				ledger.addHost(0, id, selectableChannel,
						new ObjectInputStream(selectableChannel.socket().getInputStream()),
						new ObjectOutputStream(selectableChannel.socket().getOutputStream()), false);
			}
			ObjectInputStream ois = ledger.getEntry(id).getIn();
			Message message = (Message) ois.readObject();
			ObjectOutputStream oos = ledger.getEntry(id).getOut();
			// determine the type of message
			if (message.type == Message.CONNECT) {
				System.out.println("Established new connection with " + message.src);
				ledger.getEntry(id).setNumberOfProcessors((int) message.argument);
				Message hostInfo = new Message(this.address, id, Message.HOST_INFO, numberOfProcessors);
				System.out.println("Sending host information to " + id);
				oos.writeObject(hostInfo);
			} else if (message.type == Message.NEW_CONNECTION) {
				System.out.println("Established new connection with " + id);
				ledger.getEntry(id).setNumberOfProcessors((int) message.argument);
				Message hostInfo = new Message(this.address, id, Message.HOST_INFO, numberOfProcessors);
				System.out.println("Sending host information to " + id);
				oos.writeObject(hostInfo);
				oos.flush();
				LinkedList<String> networkIDs = new LinkedList<>();
				for (NetworkLedgerEntry entry : ledger)
					if (!entry.id.equals(id))
						networkIDs.add(entry.id);
				Message networkInfo = new Message(this.address, id, Message.NETWORK_INFO, networkIDs);
				System.out.println("Sending network information to " + id);
				oos.writeObject(networkInfo);
			} else if (message.type == Message.NETWORK_INFO) {
				System.out.println("Recieved network info.");
				LinkedList<String> networkIDs = (LinkedList<String>) message.argument;
				for (String ids : networkIDs)
					if (!ledger.contains(ids))
						connect(ids);
			} else if (message.type == Message.HOST_INFO) {
				ledger.getEntry(id).setNumberOfProcessors((int) message.argument);
				System.out.println("Established new connection with " + message.src);
			} else if (message.type == Message.RUN) {
				SubsetSum.getInstance().args = (ProgramArguments) message.argument;
				SubsetSum.getInstance().begin();
			} else if (message.type == Message.NOTIFY) {
				// TODO: The src of this message is notifying this host that it
				// has completed all of its assigned work
			} else if (message.type == Message.REQUEST) {

			} else if (message.type == Message.REQUEST_RESPONSE) {

			}
			oos.flush();
		} catch (IOException | ClassNotFoundException e) {
			String id = selectableChannel.socket().getRemoteSocketAddress().toString().split("/")[1].split(":")[0];
			System.out.println("Closing connection with " + id);
			sockets.remove(selectableChannel);
			try {
				ledger.getEntry(id).getIn().close();
				ledger.getEntry(id).getOut().close();
				ledger.remove(id);
				selectableChannel.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public String getNetworkLedgerAsString() {
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
			System.out.println("Socket to " + ipAddress + " created");
			sockets.add(socketChannel);
			ObjectOutputStream oos = new ObjectOutputStream(socketChannel.socket().getOutputStream());
			Message newConnect = new Message(this.address, ipAddress, Message.NEW_CONNECTION, numberOfProcessors);
			oos.writeObject(newConnect);
			oos.flush();
			ObjectInputStream ois = new ObjectInputStream(socketChannel.socket().getInputStream());
			ledger.addHost(0, ipAddress, socketChannel, ois, oos, false);
			selector.wakeup();
		} catch (IOException e) {
			System.out.println("Failed to connect to " + ipAddress);
			return;
		}
	}

	/**
	 * @return
	 */
	public int numberOfSockets() {
		// TODO Auto-generated method stub
		return sockets.size() + 1;
	}

	/**
	 * @return the beginRun
	 */
	public boolean isBeginRun() {
		return beginRun;
	}

	/**
	 * @param beginRun
	 *            the beginRun to set
	 */
	public void setBeginRun(boolean beginRun) {
		this.beginRun = beginRun;
	}

}
