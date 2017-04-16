import java.util.UUID;

public class Message {
	Peer fromPeer;
	private UUID _guid;
	private String _function;
	int _TTL;
	int _hops;
	Payload _payload;
	
	public UUID guid(){ return this._guid;}
	public String function(){ return this._function;}
	public int TTL(){return this._TTL;}
	public int hops(){return this._hops;}
	public Payload payload(){return this._payload;}
	public void incrementHops() {this._hops++;}
	public void decrementTTL() {this._TTL--;}
	
	public Message(Peer fromPeer, String function, Payload payload){
		this.fromPeer = fromPeer;
		_guid = UUID.randomUUID();
		_function = function;
		_TTL = (function == "handshake")?1:7;
		_hops = 0;
		_payload =  payload;
	}	
}
