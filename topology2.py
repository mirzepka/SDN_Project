
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
        switches.append(self.addSwitch( 's5' ))
        switches.append(self.addSwitch( 's6' ))
        switches.append(self.addSwitch( 's7' ))

        # Add hosts 
        hosts = []
        hosts.append(self.addHost( 'h1' ))
        hosts.append(self.addHost( 'h2' ))

        # Add host-switch links
        self.addLink( hosts[0], switches[0])
        self.addLink( hosts[1], switches[6])

        # Add switch-switch links
        self.addLink( switches[0], switches[1])
        self.addLink( switches[1], switches[2])
        self.addLink( switches[0], switches[3])
        self.addLink( switches[2], switches[3])
        self.addLink( switches[2], switches[6])
        self.addLink( switches[3], switches[6])
        self.addLink( switches[0], switches[4])
        self.addLink( switches[3], switches[4])
        self.addLink( switches[4], switches[5])
        self.addLink( switches[3], switches[5])
        self.addLink( switches[5], switches[6])

topos = { 'mytopo': ( lambda: MyTopo() ) }
