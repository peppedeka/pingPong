import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Network {

	static Random randomGenerator = new Random();

	static List<Integer> portList = new ArrayList<Integer>();
	static List<String> ipList = new ArrayList<String>();

	
	public static int generatePort() {
		
		int randomInt = 0;
		boolean check = false;
		
		if(!check){
			randomInt = randomGenerator.nextInt(65535);
			if(!Arrays.asList(portList).contains(randomInt)){
				portList.add(randomInt);
				check= true;
			};	
		}
		
		return randomInt;
	}
	
	public static String generateIPAddress() {

	    String sb = "";
	    boolean check = false;
	    
	    while(!check){
		    int p1 = randomGenerator.nextInt(256);
		    int p2 = randomGenerator.nextInt(256);
		    int p3 = randomGenerator.nextInt(256);
		    int p4 = randomGenerator.nextInt(256);
	
	
		    String ip1 = Integer.toString(p1);
		    String ip2 = Integer.toString(p2);
		    String ip3 = Integer.toString(p3);
		    String ip4 = Integer.toString(p4);
		    
		    sb = ""+ip1+"."+ip2+"."+ip3+"."+ip4;
		    if(!Arrays.asList(ipList).contains(sb)){
				ipList.add(sb);
				check= true;
			};	
	    } 

	    return sb;

	}
	
}
