
from mininet.topo import Topo

class MyTopo( Topo ):
    "Simple topology example."

    def __init__( self ):

        # Initialize topology
        Topo.__init__( self )

        # Add switches
        switches = []
        switches.append(self.addSwitch( 's1' ))
        switches.append(self.addSwitch( 's2' ))
        switches.append(self.addSwitch( 's3' ))
        switches.append(self.addSwitch( 's4' ))
#        switches.append(self.addSwitch( 's5' ))
#        switches.append(self.addSwitch( 's6' ))
#        switches.append(self.addSwitch( 's7' ))

        # Add hosts 
        hosts = []
        hosts.append(self.addHost( 'h1' ))
        hosts.append(self.addHost( 'h2' ))
        hosts.append(self.addHost( 'h3' ))
        hosts.append(self.addHost( 'h4' ))
        hosts.append(self.addHost( 'h11' ))
        hosts.append(self.addHost( 'h111' ))
#        hosts.append(self.addHost( 'h5' ))
#        hosts.append(self.addHost( 'h6' ))
#        hosts.append(self.addHost( 'h7' ))

        # Add host-switch links
        for idx in range(len(switches)):
            self.addLink( hosts[idx], switches[idx])
        self.addLink( hosts[-1], switches[0])
        self.addLink( hosts[-2], switches[0])

        # Add switch-switch links
        self.addLink( switches[0], switches[1])
        self.addLink( switches[1], switches[2])
        self.addLink( switches[2], switches[3])
        self.addLink( switches[0], switches[3])
#        self.addLink( switches[2], switches[6])
#        self.addLink( switches[3], switches[6])
#        self.addLink( switches[0], switches[4])
#        self.addLink( switches[3], switches[4])
#        self.addLink( switches[4], switches[5])
#        self.addLink( switches[3], switches[5])
#        self.addLink( switches[5], switches[6])

topos = { 'mytopo': ( lambda: MyTopo() ) }
