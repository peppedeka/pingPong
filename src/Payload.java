
public class Payload {
	Peer _peer;
	int _numberOfSharedFile;
	int _numberOfKbytesShared;

	public Payload( Peer peer , int numberOfSharedFile, int numberOfKbytesShared) {
		this._peer = peer;
		this._numberOfSharedFile = numberOfSharedFile;
		this._numberOfKbytesShared = numberOfKbytesShared;	 
	}

	public int port() { return this._peer.port; }
	public String ip() { return this._peer.ip; }
	public Peer peer() { return this._peer; }
	public int numberOfSharedFile() { return this._numberOfSharedFile;}
	public int numberOfKbytesShared() { return this._numberOfKbytesShared;}
}
