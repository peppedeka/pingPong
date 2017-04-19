
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

public class Report {
	static Timestamp  timestamp = new Timestamp(System.currentTimeMillis());
	static PrintWriter pingPong = null;
	static PrintWriter fastDeep = null;
	static PrintWriter pongCached = null;
	static PrintWriter  reject = null;
	static PrintWriter pingFlood = null;
	
	static int ping = 0;
	static int pong = 0;
	static int fastConn = 0;
	static int deepConn = 0;
	static int flood = 0;
	static int returnPongCached = 0;
	static int rejectPingTTL = 0;
	static int rejectPingGuiid = 0;
	static int rejectPongTTL = 0;
	static int rejectPongGuiid = 0;

	private static String del = ";";
	
	public Report() {
		
		try{
			pingPong = new PrintWriter("ping-pong.csv", "UTF-8");
			pingPong.println("\"Item name\";ping number;pong number");
			
			pingFlood = new PrintWriter("ping-flood.csv", "UTF-8");
			pingFlood.println("\"Item name\";ping number;flood number");
			
			pongCached = new PrintWriter("pong-cached.csv", "UTF-8");
			pongCached.println("\"Item name\";pong number;pong cached");
			
			fastDeep = new PrintWriter("fast-deep.csv", "UTF-8");
			fastDeep.println("\"Item name\";fast connection;deep connection");
			
			reject = new PrintWriter("reject-ping-pong.csv", "UTF-8");
			reject.println("\"Item name\";ping number;TTL = 0;same UUID");
		   
		   
		} catch (IOException e) {}
	}
	public static void step(int step) {
		// TODO Auto-generated method stub
		int peers = (Overlay.peers.size() + 3);
		pingPong.println("\""+step+"\""+del+ping+del+pong);
		pingFlood.println("\""+step+"\""+del+ping+del+flood);
		pongCached.println("\""+step+"\""+del+pong+del+returnPongCached);
		fastDeep.println("\""+step+"\""+del+fastConn+del+deepConn);
		reject.println("\""+step+"\""+del+ping+del+rejectPingTTL+del+rejectPingGuiid);
		reset();
		
	}

	private static void reset() {
		ping=0;
		pong=0;
		fastConn = 0;
		deepConn = 0;
		flood = 0;
		returnPongCached = 0;
		rejectPingTTL = 0;
		rejectPingGuiid = 0;
		rejectPongTTL = 0;
	}
	public static void close() {
		pingPong.close();
		pingFlood.close();
		pongCached.close();
		fastDeep.close();
		reject.close();
	}
	static void addPing() {
		ping++;
	}
	static void addPong() {
		pong++;
	}
	static void resetPong() {
		pong = 0;
	}
	static void resetPing() {
		ping = 0;
	}



	public static void fastConn() {
		fastConn++;
	}

	public static void deepConn() {
		deepConn++;
	}

	public static void flood() {
		flood++;
		
	}

	public static void returnPongCached() {
		returnPongCached++;
		
	}

	public static void rejectPingTTL() {
		rejectPingTTL++;
	}

	public static void rejectPingGuiid() {
		rejectPingGuiid++;
	}

	public static void rejectPongTTL() {
		rejectPongTTL++;
		
	}

	public static void rejectPongGuiid() {
		rejectPongGuiid++;
		
	}


}
