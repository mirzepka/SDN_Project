package pl.edu.agh.kt;

public class LinkContainer {

	private Long srcId;
	private Long dstId;
	private Long id;
	private Long cost;
	
	public LinkContainer(Long src,Long dst,Long Id, Long linkCost){
		srcId=src;
		dstId=dst;
		id=Id;
		cost=linkCost;
	}
	public String toString(){
		return "LinkID: "+id+" src: "+srcId+" dst: " + dstId + " cost: " + cost;
	}
	public Long getSrc(){
		return srcId;
	}
	public Long getDst(){
		return dstId;
	}
	public Long getId(){
		return id;
	}
	public Long getCost(){
		return cost;
	}
}
