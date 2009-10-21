/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) Associated Universities Inc., 2002
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307  USA
 *
 * ncTestCompImpl.java
 *
 * Created on April 11, 2003, 2:21 PM
 */
package alma.acs.eventbrowser.model;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Class designed for testing event suppliers.
 * @author original by dfugate, hacked for eventGUI testing by jschwarz
 */
public class EventSupplierImpl implements SupplierCompOperations 
{
	private Logger m_logger;
	private ContainerServices m_containerServices;
	
	private final String channelName = "blar";

	public EventSupplierImpl(Logger logger, ContainerServices cs, String clientName)
			throws Exception {
		m_logger = logger;
		m_containerServices = cs;
	}

	private SimpleSupplier m_supplier = null;

	

	/** Sets up the SimpleSupplier.
	 * @param containerServices Services to components.
	 * @throws ComponentLifecycleException Not thrown.
	 */
	public void initialize(ContainerServices containerServices) throws ComponentLifecycleException {

		try {
			//Instantiate our supplier
			m_supplier = new SimpleSupplier(channelName, //the channel's name
					m_containerServices);
			m_logger.info("SimpleSupplier for '"+channelName+"' channel created.");
		} catch (Exception e) {
			throw new ComponentLifecycleException(e);
		}
	}

	
	/** Sends some events to an event channel.
	 * @param param number of events to send
	 */
	public void sendEvents(short param) {
		m_logger.info("Now sending simplesupplier events...");
		try {
			//first send out some number of events.
			EventDescription t_block = new EventDescription("no name", 32L, 64L);
			for (short i = 0; i < param; i++) {
				m_supplier.publishEvent(t_block);
			}

			//fake a subscription change
			m_supplier.subscription_change(new org.omg.CosNotification.EventType[] {},
					new org.omg.CosNotification.EventType[] {});

		} catch (Exception e) {
			System.err.println(e);
		}
	}

//	public void sendEventsSpecial(NestedFridgeEvent[] eventData) throws CouldntPerformActionEx {
//		try {
//			m_logger.info("Now sending " + eventData.length + " NestedFridgeEvent events...");
//			for (NestedFridgeEvent event : eventData) {
//				m_supplier.publishEvent(event);
//			}
//		} catch (Throwable thr) {
//			m_logger.log(Level.WARNING, "failed to send NestedFridgeEvent. Will not try again.");
//			throw (new AcsJCouldntPerformActionEx(thr)).toCouldntPerformActionEx();
//		}
//	}

	/** Disconnects the supplier. */
	public void cleanUp() {
		m_logger.info("cleanUp() called...");

		try {

			//fake a consumer disconnecting...
			m_supplier.disconnect_structured_push_supplier();
			m_supplier.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
