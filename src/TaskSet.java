import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class TaskSet {
	private String owner; // who is responsible for this TaskGroup
	private Long begin;	  // the first TaskGroup this TaskGroup is responsible for
	private final long firstStart;
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
		this.firstStart = begin;
	}
	
	/**
	 * Requests a small task
	 */
	public synchronized long[] requestSmallTask(){
		if(begin < end){
			if(end-begin >= 1024){
				begin += 1024;
				printProgress();
				return new long[] {begin-1024,begin};
			} else {
				long temp = begin;
				begin = end;
				printProgress();
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
	
	public void printProgress(){
		final int width = 50; // progress bar width in chars
		final double progressPercentage = (((double) begin - firstStart)/((double) end - firstStart));
		StringBuilder progress = new StringBuilder();
		progress.append("[");
	    int i = 0;
	    for (; i <= (int)(progressPercentage*width); i++) {
	    	progress.append(".");
	    }
	    for (; i < width; i++) {
	    	progress.append(" ");
	    }
	    progress.append("]");
	    if(begin >= end){
	    	DecimalFormat formatter = new DecimalFormat("#000");
	    	progress.append(" ");
	    	progress.append(formatter.format(progressPercentage*100));
	    	progress.append("% ");
	    	progress.append(begin-firstStart);
	    	progress.append("/");
	    	progress.append(end-firstStart);
	    	System.out.println('\r'+progress.toString());
	    }else{
	    	DecimalFormat formatter = new DecimalFormat("#00");
	    	progress.append(" ");
	    	progress.append(formatter.format(progressPercentage*100));
	    	progress.append("% ");
	    	progress.append(begin-firstStart);
	    	progress.append("/");
	    	progress.append(end-firstStart);
	    	System.out.print('\r'+progress.toString());
	    }
	}
}
