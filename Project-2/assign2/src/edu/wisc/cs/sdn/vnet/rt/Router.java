package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.Ethernet;

/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class Router extends Device
{	
	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
	private ArpCache arpCache;
	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Router(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache();
	}
	
	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable()
	{ return this.routeTable; }
	
	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile)
	{
		if (!routeTable.load(routeTableFile, this))
		{
			System.err.println("Error setting up routing table from file "
					+ routeTableFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static route table");
		System.out.println("-------------------------------------------------");
		System.out.print(this.routeTable.toString());
		System.out.println("-------------------------------------------------");
	}
	
	/**
	 * Load a new ARP cache from a file.
	 * @param arpCacheFile the name of the file containing the ARP cache
	 */
	public void loadArpCache(String arpCacheFile)
	{
		if (!arpCache.load(arpCacheFile))
		{
			System.err.println("Error setting up ARP cache from file "
					+ arpCacheFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static ARP cache");
		System.out.println("----------------------------------");
		System.out.print(this.arpCache.toString());
		System.out.println("----------------------------------");
	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
				etherPacket.toString().replace("\n", "\n\t"));
		
		/********************************************************************/
		/* TODO: Handle packets                                             */
		
		// Step 1: Check if it's an IPv4 packet
		if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4)
		{
			System.out.println("Not IPv4 packet, dropping");
			return; // Drop non-IPv4 packets
		}
		
		// Step 2: Get the IPv4 packet from the payload
		net.floodlightcontroller.packet.IPv4 ipPacket = 
			(net.floodlightcontroller.packet.IPv4) etherPacket.getPayload();
		
		// Step 3: Verify checksum
		short originalChecksum = ipPacket.getChecksum();
		ipPacket.resetChecksum();
		byte[] serialized = ipPacket.serialize();
		
		// Deserialize to get the recalculated checksum
		net.floodlightcontroller.packet.IPv4 tempPacket = new net.floodlightcontroller.packet.IPv4();
		tempPacket.deserialize(serialized, 0, serialized.length);
		short calculatedChecksum = tempPacket.getChecksum();
		
		// Compare checksums
		if (originalChecksum != calculatedChecksum)
		{
			System.out.println("Invalid checksum, dropping (expected: " + calculatedChecksum + ", got: " + originalChecksum + ")");
			return; // Drop packet with invalid checksum
		}
		
		// Restore the original checksum to the packet
		ipPacket.setChecksum(originalChecksum);
		
		// Step 4: Decrement TTL and check if it becomes 0
		byte ttl = ipPacket.getTtl();
		ttl--;
		
		if (ttl == 0)
		{
			System.out.println("TTL is 0, dropping");
			return; // Drop packet with TTL = 0
		}
		
		// Update TTL in packet
		ipPacket.setTtl(ttl);
		
		// Reset checksum so it will be recalculated
		ipPacket.setChecksum((short) 0);
		
		// Step 5: Check if packet is destined for one of router's interfaces
		int destAddr = ipPacket.getDestinationAddress();
		for (Iface iface : this.interfaces.values())
		{
			if (destAddr == iface.getIpAddress())
			{
				System.out.println("Packet destined for router interface, dropping");
				return; // Drop packet destined for router
			}
		}
		
		// If we get here, packet passed all checks
		System.out.println("Packet passed all checks");
		
		// Step 6: Forward the packet
		// Lookup the route entry with longest prefix match
		RouteEntry routeEntry = this.routeTable.lookup(destAddr);
		
		if (routeEntry == null)
		{
			System.out.println("No matching route entry, dropping");
			return; // Drop packet if no route matches
		}
		
		// Step 7: Determine next-hop IP address
		int nextHopIP;
		if (routeEntry.getGatewayAddress() == 0)
		{
			// Gateway is 0.0.0.0, destination is directly connected
			nextHopIP = destAddr;
		}
		else
		{
			// Use gateway as next hop
			nextHopIP = routeEntry.getGatewayAddress();
		}
		
		// Step 8: Lookup MAC address for next-hop IP in ARP cache
		ArpEntry arpEntry = this.arpCache.lookup(nextHopIP);
		
		if (arpEntry == null)
		{
			System.out.println("No ARP entry for next hop, dropping");
			return; // Drop packet if no ARP entry
		}
		
		// Step 9: Update Ethernet header
		// Set destination MAC to next hop's MAC address
		etherPacket.setDestinationMACAddress(arpEntry.getMac().toBytes());
		
		// Set source MAC to outgoing interface's MAC address
		Iface outIface = routeEntry.getInterface();
		etherPacket.setSourceMACAddress(outIface.getMacAddress().toBytes());
		
		// Step 10: Send the packet out the correct interface
		System.out.println("Forwarding packet out interface: " + outIface.getName());
		this.sendPacket(etherPacket, outIface);
		
		/********************************************************************/
	}
}
