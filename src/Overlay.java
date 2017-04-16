import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.Timer;

public class Overlay {

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	static Timer timer = new Timer();
	static int seconds = 4;
	static int step = 0;

	static Random randomGenerator = new Random();

	static int numberOfNodes = 3;
	static List<Peer> peers = new ArrayList<Peer>();
	static List<Peer> bootstrapPeers = new ArrayList<Peer>();

	public Overlay(){
		generateBootstrapNodes();
	}
	public static List<Peer> nodes() {
		return peers;
	}

	public static void peers(List<Peer> peers) {
		Overlay.peers = peers;
	}
	public static List<Peer> getBootstrapList() {
		return bootstrapPeers;
	}

	public static void MyTimer(Overlay myNet) {

		TimerTask task;

		task = new TimerTask() {
			@Override
			public void run() { 
				int numberOfPeers = peers.size() - 1;
				System.out.format(ANSI_PURPLE+"\n\n\nSTEP: %d\n"+ANSI_RESET, step);
				switch(step){
				case 0: case 1: case 3:default: 
					System.out.format(ANSI_PURPLE+"ADD NEW NODE ON OVERLAY NETWORK\n"+ANSI_RESET);
					myNet.addPeer();
					step++;
					break;
				case 2: case 4: 
					int randomPeerIndex = randomGenerator.nextInt(numberOfPeers);
					System.out.format(ANSI_PURPLE+"NODE %s LEAVE OVERLAY NETWORK\n"+ANSI_RESET, peers.get(randomPeerIndex).ip);
					myNet.removePeer(randomPeerIndex);
					step++;
					break;
				}
			}
		};
		timer.schedule(task, 0, 40000);

	}

	protected void removePeer(int randomPeerIndex) {
		peers.get(randomPeerIndex).interrupt();
		peers.remove(randomPeerIndex);

	}
	public void addPeer() {
		Peer newPeer = new Peer();
		System.out.format("\nOverlay: new peer has joined %s\n", newPeer.ip);

		peers.add(newPeer);
		newPeer.start();
	}
	public void generateBootstrapNodes() {
		for(int i=0; i < numberOfNodes; i++){
			Peer newPeer = new Peer();
			bootstrapPeers.add(newPeer);
		}
		for(int i=0; i < numberOfNodes; i++){
			bootstrapPeers.get(i).run();
		}	 
	}

	public static void main(String[] args) {


		Overlay myNet = new Overlay();
		System.out.format(ANSI_PURPLE +"CREATE OVERLAY NETWORK...DONE WITH %d NODE\n"+ANSI_RESET, + myNet.getBootstrapList().size());
		MyTimer(myNet);


	}

} 