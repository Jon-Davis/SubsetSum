import java.io.Serializable;

/**
 * The common message type that will be sent across the network
 * @author Jonathon Davis
 *
 */
public final class Message implements Serializable{
	private static final long serialVersionUID = -2723363051271966964L;
	public static final int RUN = 0,				// Begins a Subset sum calculation, argument Double[]
							NOTIFY = 2, 			// Notifies completeness, argument null
							CONNECT = 3, 			// Establishes a connection within the network, argument Integer
							NEW_CONNECTION = 5,		// Establishes a connection to a network, argument Integer
							REQUEST = 1, 			// Requests a TaskGroup, argument null
							REQUEST_RESPONSE = 4,	// Response to a request for a task, TaskGroup
							NETWORK_INFO = 6,		// Response to NEW_CONNECTION, argument INetAddress[]
							HOST_INFO = 7;			// Response to NEW_CONNECTION and CONNECT, argument Integer
	
	public final String src;			//The name of the sender
	public final String destination;	//The name of the destination
	public final int type;				//The type of message
	public final Object argument;		//An argument for the message
	
	public Message(String src, String destination, int type, Object argument){
		this.src = src;
		this.destination = destination;
		this.type = type;
		this.argument = argument;
	}
	
	public String toString(){
		return "src = " + src + " type = " + type + " argument = " + argument; 
	}

}
