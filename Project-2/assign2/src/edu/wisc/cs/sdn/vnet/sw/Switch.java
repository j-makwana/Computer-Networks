package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.MACAddress;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import java.util.HashMap;

/**
 * @author Aaron Gember-Jacobson
 */
public class Switch extends Device
{	
	/**
	 * Inner class to store MAC table entries with timestamps
	 */
	private class MacEntry {
		Iface iface;
		long timestamp;
		
		MacEntry(Iface iface, long timestamp) {
			this.iface = iface;
			this.timestamp = timestamp;
		}
	}

	
	private HashMap<MACAddress, MacEntry> macTable;
	private static final long TIMEOUT_MS = 15000; // 15 seconds in milliseconds

	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.macTable = new HashMap<>();
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
	
	/********************************************************************/
	// Get MAC addresses from the packet
	MACAddress sourceAddress = etherPacket.getSourceMAC();
	MACAddress destinationAddress = etherPacket.getDestinationMAC();
	
	// Step 1: Learn - Store source MAC with current timestamp (this resets timeout)
	long currentTime = System.currentTimeMillis();
	this.macTable.put(sourceAddress, new MacEntry(inIface, currentTime));
	
	// Step 2: Forward - Check if we know where to send the packet
	if(macTable.containsKey(destinationAddress)){
		MacEntry entry = macTable.get(destinationAddress);
		
		// Check if entry has expired (older than 15 seconds)
		if(currentTime - entry.timestamp > TIMEOUT_MS){
			// Entry expired - remove it and flood
			System.out.println("MAC entry expired for " + destinationAddress);
			macTable.remove(destinationAddress);
			// Fall through to flooding
		} else {
			// Entry is valid - forward to the specific interface
			if(!entry.iface.equals(inIface)){
				System.out.println("Forwarding to interface: " + entry.iface.getName());
				sendPacket(etherPacket, entry.iface);
			}
			return; // Don't flood
		}
		
	}

	
	// If we reach here, either destination not in table or entry expired - flood
	System.out.println("Flooding packet to all interfaces except " + inIface.getName());
	for(Iface iface: this.interfaces.values()){
		if(!iface.equals(inIface)){
			sendPacket(etherPacket, iface);
		}
	}
	}
}
