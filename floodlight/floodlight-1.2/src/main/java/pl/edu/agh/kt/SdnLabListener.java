package pl.edu.agh.kt;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.topology.TopologyManager;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdnLabListener implements IFloodlightModule, IOFMessageListener {

	protected IFloodlightProviderService floodlightProvider;
	protected static Logger logger;
	protected TopologyManager tm;
	
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

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
			FloodlightContext cntx) {
		logger.info("************* NEW PACKET IN *************");
		PacketExtractor extractor = new PacketExtractor();
		extractor.packetExtract(cntx);

		//TODO LAB 5
		Flows.sendPacketOut(sw);
			
		Set<DatapathId> allIds = tm.switchService.getAllSwitchDpids();
		//logger.info(allIds.toString());
		
		ArrayList<String> path = new ArrayList<>();
		
					
					/*
					 *TU trzeba wywolac dijkstre ktora wrzuci do listy "path" po kolei id'ki  nodow, (sciezka)
					 *Do zrobienia:
					 *1.w "init" stworzyc graf dijkstry na podstawie:
					 *	-tm.switchService.getAllSwitchDpids() - wszytskie identyfikatory nodeow
					 *  -tm.linkDiscoveryService.getLinks().keySet() - wszystkie linki(srcId,srcPort-dstId,dstPort
					 *2. Wywolac tutaj dijkstre i zapisac sciezke do list path(zahardkodowane wartosci sa ponizej zeby stworzyc sciezke z node 1 do node 3)
					 *3. Zeby miec obecne obciazenie na laczach trzeba uzyc modulu StatisticsCollector
					 *  - w nim jest funkcja getBandwidthConsumption()
					 *4. Trzeba znalesc gdzie sa mapowane linki host-switch, tak zeby dla danego adresu ip mozna było wiedzieec, do ktorego switcha jest od podpiety.
					 *   w tm.linkDiscoveryService.getLinks().keySet() sa tylko linki miedzy switchami.
					 *   Poki co mozna to pominac i probowac robic dijkstre do jednego docelowego switcha i miedzy nimi pingowac
					 *   tm.switchService.getSwitch(DatapathId.of(1)).getPorts() tutaj sa porty i adresy fizyczne po stronie switcha,
					 *   ale dalej nie ma tego jak polaczyc z adresem na hoscie
					 */
					if(sw.getId().toString().equals("00:00:00:00:00:00:00:01"))
					{
						path.add("00:00:00:00:00:00:00:01");
						path.add("00:00:00:00:00:00:00:02");
						path.add("00:00:00:00:00:00:00:03");
					}
					else{
						path.add("00:00:00:00:00:00:00:03");
						path.add("00:00:00:00:00:00:00:02");
						path.add("00:00:00:00:00:00:00:01");
					}
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
				logger.info("COMAPRISON:");
				logger.info(dp.toString());
				logger.info(path.get(i));
				logger.info(""+dp.toString().length());
				logger.info(""+(path.get(i).length()));
				if (dp.toString().equals(path.get(i)))
				{logger.info("CORRRRRRRRRRRRRRRRRRRRRRRECT");
					dpid = dp;
				}
			}
			IOFSwitch swit = tm.switchService.getSwitch(dpid);
			if (swit == null)
			{
				logger.info("NUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUL");
				logger.info(tm.switchService.getAllSwitchDpids().toString());
				logger.info(dpid.toString());
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
        
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		logger.info("******************* START **************************");

	}

}
