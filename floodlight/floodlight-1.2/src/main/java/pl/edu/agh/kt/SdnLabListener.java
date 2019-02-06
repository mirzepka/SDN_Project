package pl.edu.agh.kt;

import static org.easymock.EasyMock.createMock;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.List;
import java.util.HashMap;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.python.google.common.collect.ImmutableList;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.internal.Device;
import net.floodlightcontroller.devicemanager.internal.DeviceManagerImpl;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.linkdiscovery.web.LinksResource;
import net.floodlightcontroller.routing.BroadcastTree;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.routing.PathId;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.topology.NodePortTuple;
import net.floodlightcontroller.topology.TopologyInstance;
import net.floodlightcontroller.topology.TopologyManager;
import net.floodlightcontroller.routing.Path;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdnLabListener implements IFloodlightModule, IOFMessageListener {


    public static final int MAX_LINK_WEIGHT = 10000;
    public static final int MAX_PATH_WEIGHT = Integer.MAX_VALUE - MAX_LINK_WEIGHT - 1;
    public static final int PATH_CACHE_SIZE = 1000;

	protected IFloodlightProviderService floodlightProvider;
	protected static Logger logger;
	protected TopologyManager tm;
	protected DeviceManagerImpl deviceManager;
	protected int pingCounter = 4;
	protected Map<Integer, DatapathId> addrMap = new HashMap<Integer, DatapathId>(); 
	
	private Map<Long,DatapathId> nodes = new HashMap<>(); 
	private Map<DatapathId,Set<Link>> nodesWithLinks= new HashMap<>();
    private Map<Link,Integer> linksContainer = new HashMap<>();
	
	@Override
	public String getName() {
		return SdnLabListener.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected boolean isCorrectCall() {
		if (addrMap.size() > 0){
			return true;
		}
		if ( pingCounter > deviceManager.deviceMap.size())
		{
			return false;
		}
		for (Device dev : deviceManager.deviceMap.values()){
			addrMap.put(dev.getEntities()[0].getIpv4Address().getInt(), dev.getEntities()[0].getSwitchDPID());
		}
		return true;
	}
	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
			FloodlightContext cntx) {
		logger.info("************* NEW PACKET IN *************");
		if(!isCorrectCall()){
			return  Command.STOP;
		}
		PacketExtractor extractor = new PacketExtractor();
		extractor.packetExtract(cntx);

		Object tmpObj = cntx.getStorage().get("net.floodlightcontroller.core.IFloodlightProvider.piPayload");
		Ethernet eth = (Ethernet)tmpObj;
		ARP arp = (ARP) eth.getPayload();
		int ipAddr = arp.getTargetProtocolAddress().getInt();
		
		DatapathId targetDpId = addrMap.get(ipAddr);// Tu jest ID ostatniego nodea w sieci, potrzebne do Dijkstry
		//logger.info("TargetNodeIP: " + targetDpId.toString()); 		
	
		
		
		//TODO LAB 5
		
		Set<DatapathId> allIds = tm.switchService.getAllSwitchDpids();


		logger.info("MOJEJEOEJEO" + linksContainer.keySet().toString());
		//TODO DODANIE PRZELICZANIA KOSZTOW NA LINKACH
		//buildLinkDpidMap(tm.switchService.getAllSwitchDpids(),tm.getSwitchPorts(),tm.linkDiscoveryService.getPortLinks())
		//TWORZE DRZEWO ROZPINAJACE DIJKSTRY - Trzeba dac zrodlo jako docelowy - czyli to test1 = destnation
		BroadcastTree tree = dijkstraa(tm.linkDiscoveryService.getSwitchLinks(),targetDpId,linksContainer,true);
		
		ArrayList<String> path = new ArrayList<>();
		logger.info("TEEEEEEEEEST");
		logger.info(tree.toString());
		//WYLICZAM SCIEZKE OD test2 do test1 (teraz jest 1-->3 - wazne zeby nie robic src = destiantion ustawione
		// na drzewie rozpinajacym bo sie wywroci cale
		Path newroute = buildPath(new PathId(sw.getId(), targetDpId), tree);	
		//TODO przeparsowac newroute na liste Stringow do zmiennej path - mozliwe ze 5min roboty
		logger.info("PATH"+newroute.toString()); 
					/*
					 *TU trzeba wywolac dijkstre ktora wrzuci do listy "path" po kolei id'ki  nodow, (sciezka)
					 *Do zrobienia:
					 *1.w "init" stworzyc graf dijkstry na podstawie:
					 *	-tm.switchService.getAllSwitchDpids() - wszytskie identyfikatory nodeow
					 *  -tm.linkDiscoveryService.getLinks().keySet() - wszystkie linki(srcId,srcPort-dstId,dstPort
					 *  update MZ: dodalem topologyInit(allIds) wyzej i to inicjuje moja liste nodow i linkow -
					 *  teraz trzeba zrobic djikstre i zaczytywanie kosztow to dodam jutro - jak cos to
					 *  linksContaier zawier liste linkow - id przypisywalem losowo bo nie widzialem w oryginalnych
					 *  linkach jakiegos id, a nodes zawiera liste nodow, aczkolwiek nie wiem czy to sie przyda,
					 *  bo one juz siedza w linkach
					 *2. Wywolac tutaj dijkstre i zapisac sciezke do list path(zahardkodowane wartosci sa ponizej zeby stworzyc sciezke z node 1 do node 3)
					 *3. Zeby miec obecne obciazenie na laczach trzeba uzyc modulu StatisticsCollector
					 *  - w nim jest funkcja getBandwidthConsumption()
					 *4. Trzeba znalesc gdzie sa mapowane linki host-switch, tak zeby dla danego adresu ip mozna było wiedzieec, do ktorego switcha jest od podpiety.
					 *   w tm.linkDiscoveryService.getLinks().keySet() sa tylko linki miedzy switchami.
					 *   Poki co mozna to pominac i probowac robic dijkstre do jednego docelowego switcha i miedzy nimi pingowac
					 *   tm.switchService.getSwitch(DatapathId.of(1)).getPorts() tutaj sa porty i adresy fizyczne po stronie switcha,
					 *   ale dalej nie ma tego jak polaczyc z adresem na hoscie
					 */
		List<net.floodlightcontroller.core.types.NodePortTuple> route = newroute.getPath();
		
		//path.add(sw.getId().toString());
		for(net.floodlightcontroller.core.types.NodePortTuple tuple : route)
		{
			path.add(tuple.getNodeId().toString());
		}
		logger.info("ESSSSSSSSSSSSSSTABL: "+path.toString());
					/*if(sw.getId().toString().equals("00:00:00:00:00:00:00:01"))
					{
						path.add("00:00:00:00:00:00:00:01");
						path.add("00:00:00:00:00:00:00:02");
						path.add("00:00:00:00:00:00:00:03");
					}
					else{
						path.add("00:00:00:00:00:00:00:03");
						path.add("00:00:00:00:00:00:00:02");
						path.add("00:00:00:00:00:00:00:01");
					}*/
					//
			
		OFPacketIn pin = (OFPacketIn) msg;
		OFPort outPort=OFPort.of(8);
		ArrayList<OFPort> ports = new ArrayList<> ();
		ports.add(pin.getInPort());
		for(int i = 0; i < path.size(); ++i)
		{
			DatapathId dpid = DatapathId.of(0);
			for(DatapathId dp : allIds)
			{
			/*	logger.info("COMAPRISON:");
				logger.info(dp.toString());
				logger.info(path.get(i));
				logger.info(""+dp.toString().length());
				logger.info(""+(path.get(i).length()));*/
				if (dp.toString().equals(path.get(i)))
				{logger.info("CORRRRRRRRRRRRRRRRRRRRRRRECT");
					dpid = dp;
				}
			}
			IOFSwitch swit = tm.switchService.getSwitch(dpid);
			if (swit == null)
			{
			/*	logger.info("NUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUL");
				logger.info(tm.switchService.getAllSwitchDpids().toString());
				logger.info(dpid.toString());*/
			}
			//swit.getPorts();
			logger.info("ASASASASASASASASASSA");
			logger.info(swit.getPorts().toString());
			if(i<(path.size()-1))
			{
				for (Link link : tm.linkDiscoveryService.getLinks().keySet())
				{
				
					if (link.getSrc().toString().equals(path.get(i)))
					{
						if(link.getDst().toString().equals(path.get(i+1)))
						{
							outPort = link.getSrcPort();
							ports.add(link.getSrcPort());
							ports.add(link.getDstPort());
							logger.info("WWWWWWWWWWWWWWWWW datapSRC/Dst: "+link.getSrc().toString()+"/"+link.getDst().toString() +" SRC PORT TO : " + outPort.toString());
						}
					}
				}
			}
			else
			{
				ports.add(OFPort.of(1));
				ports.add(OFPort.of(7));
			}
			
			logger.info("POCZ");
			logger.info(tm.linkDiscoveryService.getSwitchLinks().toString());
			logger.info(tm.linkDiscoveryService.getLinks().toString());
			logger.info("KONI");								
			Flows.simpleAdd(swit, pin, cntx, ports.get(ports.size()-2), ports.get(ports.size()-3));
		}
		logger.info(tm.switchService.getSwitch(DatapathId.of(1)).getPorts().toString());
		logger.info("USED PORTS: "+ports.toString());
//		OFPort outPort=OFPort.of(0);
//		if (pin.getInPort() == OFPort.of(1)){
//		outPort=OFPort.of(2);
//		} else
//		outPort=OFPort.of(1);
//		Flows.simpleAdd(sw, pin, cntx, outPort);
		logger.info("************* Flow added	 *************");
		logger.info(tm.switchService.getAllSwitchDpids().toString());
		logger.info(tm.switchService.getSwitch(tm.switchService.getAllSwitchDpids().iterator().next()).getPorts().toString());
		return Command.STOP;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		logger = LoggerFactory.getLogger(SdnLabListener.class);
		tm  = new TopologyManager();
        tm.init(context);
        //ITopologyService topology = createMock(ITopologyService.class);
        //context.addService(ITopologyService.class, tm);
        deviceManager = new DeviceManagerImpl();
        context.addService(IDeviceService.class, deviceManager);
        deviceManager.init(context);
        deviceManager.startUp(context);
        
        Set<DatapathId> allIds = tm.switchService.getAllSwitchDpids();
		topologyInit(allIds);
        
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		logger.info("******************* START **************************");

	}
	private void topologyInit(Set<DatapathId> allIds){
        for(DatapathId node : allIds){
        	nodes.put(node.getLong(),node);
        }
        int changeCost = 1;
        for(Link link : tm.linkDiscoveryService.getLinks().keySet()){
        	linksContainer.put(link,changeCost);
        	changeCost++;
        }
	}

	private BroadcastTree dijkstraa(Map<DatapathId, Set<Link>> links, DatapathId root,
            Map<Link, Integer> linkCost,
            boolean isDstRooted) {
        HashMap<DatapathId, Link> nexthoplinks = new HashMap<DatapathId, Link>();
        HashMap<DatapathId, Integer> cost = new HashMap<DatapathId, Integer>();
        int w;
        logger.info("DIJKSTRA"+root.toString());
        for (DatapathId node : links.keySet()) {
            nexthoplinks.put(node, null);
            cost.put(node, MAX_PATH_WEIGHT);
            //log.debug("Added max cost to {}", node);
        }

        HashMap<DatapathId, Boolean> seen = new HashMap<DatapathId, Boolean>();
        PriorityQueue<NodeDist> nodeq = new PriorityQueue<NodeDist>();
        nodeq.add(new NodeDist(root, 0));
        cost.put(root, 0);

        //log.debug("{}", links);

        while (nodeq.peek() != null) {
            NodeDist n = nodeq.poll();
            DatapathId cnode = n.getNode();
            int cdist = n.getDist();

            if (cdist >= MAX_PATH_WEIGHT) break;
            if (seen.containsKey(cnode)) continue;
            seen.put(cnode, true);

            //log.debug("cnode {} and links {}", cnode, links.get(cnode));
            if (links.get(cnode) == null) continue;
            for (Link link : links.get(cnode)) {
                DatapathId neighbor;

                if (isDstRooted == true) {
                    neighbor = link.getSrc();
                } else {
                    neighbor = link.getDst();
                }

                // links directed toward cnode will result in this condition
                if (neighbor.equals(cnode)) continue;

                if (seen.containsKey(neighbor)) continue;

                if (linkCost == null || linkCost.get(link) == null) {
                    w = 1;
                } else {
                    w = linkCost.get(link);
                }

                int ndist = cdist + w; // the weight of the link, always 1 in current version of floodlight.


                if (ndist < cost.get(neighbor)) {
                    cost.put(neighbor, ndist);
                    nexthoplinks.put(neighbor, link);
                    NodeDist ndTemp = new NodeDist(neighbor, ndist);
                    // Remove an object that's already in there.
                    // Note that the comparison is based on only the node id,
                    // and not node id and distance.
                    nodeq.remove(ndTemp);
                    // add the current object to the queue.
                    nodeq.add(ndTemp);
                }
            }
        }

        BroadcastTree ret = new BroadcastTree(nexthoplinks, cost);

        return ret;
	}
	private class NodeDist implements Comparable<NodeDist> {
        private final DatapathId node;
        public DatapathId getNode() {
            return node;
        }

        private final int dist;
        public int getDist() {
            return dist;
        }

        public NodeDist(DatapathId node, int dist) {
            this.node = node;
            this.dist = dist;
        }

		@Override
		public int compareTo(NodeDist o) {
            if (o.dist == this.dist) {
                return (int)(this.node.getLong() - o.node.getLong());
            }
            return this.dist - o.dist;
		}
	}


    private Path buildPath(PathId id, BroadcastTree tree) {
    	
    	net.floodlightcontroller.core.types.NodePortTuple npt;
        DatapathId srcId = id.getSrc();
        DatapathId dstId = id.getDst();
        //set of NodePortTuples on the route
        LinkedList<net.floodlightcontroller.core.types.NodePortTuple> sPorts = new LinkedList<net.floodlightcontroller.core.types.NodePortTuple>();


        Map<DatapathId, Link> nexthoplinks = tree.getLinks();
        logger.info("tutajtutautjtj"+nexthoplinks.get(srcId));
        if ((nexthoplinks != null) && (nexthoplinks.get(srcId) != null)) {
        	logger.info("tutajtutautjtj");
            while (!srcId.equals(dstId)) {
                Link l = nexthoplinks.get(srcId);
                npt = new net.floodlightcontroller.core.types.NodePortTuple(l.getSrc(), l.getSrcPort());
                sPorts.addLast(npt);
                npt = new net.floodlightcontroller.core.types.NodePortTuple(l.getDst(), l.getDstPort());
                sPorts.addLast(npt);
                srcId = nexthoplinks.get(srcId).getDst();
            }
        }
        // else, no path exists, and path equals null

        Path result = null;
        if (sPorts != null && !sPorts.isEmpty()) {
            result = new Path(id, sPorts);

        }

        logger.trace("buildpath: {}", result);
        return result;
}
}


