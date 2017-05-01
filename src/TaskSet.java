import java.math.BigInteger;

public class TaskSet {
	private String owner; // who is responsible for this TaskGroup
	private Long begin;	  // the first TaskGroup this TaskGroup is responsible for
	private Long end;	  // the last TaskGroup this TaskGroup is responsible for
	
	/**
	 * @param owner
	 * @param begin
	 * @param end
	 */
	public TaskSet(String owner, long begin, long end) {
		super();
		this.owner = owner;
		this.begin = begin;
		this.end = end;
	}
	
	/**
	 * Requests a small task
	 */
	public synchronized long[] requestSmallTask(){
		if(begin < end){
			if(end-begin >= 1024){
				begin += 1024;
				return new long[] {begin-1024,begin};
			} else {
				long temp = begin;
				begin = end;
				return new long[] {temp, end};
			}
		}
		return null;
	}
	
	/**
	 * Requests a small task
	 */
	public synchronized long[] requestLargeTask(){
		if(begin != end){
			if(end-begin > 1048576 * 2){
				begin += 1048576;
				return new long[] {begin-1048576,begin};
			} 
		}
		return null;
	}
}
