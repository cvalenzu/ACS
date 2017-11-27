/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 */
package alma.acs.tmcdb.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import alma.ACSErr.Completion;
import alma.ACSErrTypeCommon.BadParameterEx;
import alma.acs.tmcdb.AcsService;
import alma.acs.tmcdb.AcsServiceServiceType;
import alma.acs.tmcdb.Computer;
import alma.acs.tmcdb.Configuration;
import alma.acs.tmcdb.Container;
import alma.acs.tmcdb.ContainerStartupOption;
import alma.acs.util.ACSPorts;
import alma.acsdaemon.ContainerDaemon;
import alma.acsdaemon.ContainerDaemonHelper;
import alma.acsdaemon.ContainerDaemonOperations;
import alma.acsdaemon.DaemonCallback;
import alma.acsdaemon.DaemonCallbackHelper;
import alma.acsdaemon.DaemonCallbackPOA;
import alma.acsdaemon.ServicesDaemon;
import alma.acsdaemon.ServicesDaemonHelper;
import alma.acsdaemon.ServicesDaemonOperations;
import alma.acsdaemonErrType.FailedToStartContainerEx;
import alma.acsdaemonErrType.ServiceAlreadyRunningEx;

import com.cosylab.acs.maci.ServiceDaemon;
import com.cosylab.cdb.jdal.HibernateWDALImpl;
import com.cosylab.cdb.jdal.hibernate.HibernateDBUtil;
import com.cosylab.cdb.jdal.hibernate.HibernateUtil;
import com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin;
import com.cosylab.cdb.jdal.hibernate.plugin.PluginFactory;

public class AcsStartRemote {

	private static final String configName;
	private static final short acsInstance;
	static {
		configName = System.getProperty(HibernateWDALImpl.TMCDB_CONFIGURATION_NAME_KEY, 
				System.getenv(HibernateWDALImpl.TMCDB_CONFIGURATION_NAME_KEY));
		acsInstance = Short.parseShort(System.getenv("ACS_INSTANCE"));
	}
	
	private class AcsServiceComparator implements Comparator<AcsService>{

		@Override
		public int compare(AcsService o1, AcsService o2) {
			switch (o1.getServiceType()){
			case NAMING:
				if (o2.getServiceType() == AcsServiceServiceType.NAMING)
					return 0;
				else
					return -1;
			case IFR:
				if (o2.getServiceType() == AcsServiceServiceType.NAMING)
					return 1;
				else if (o2.getServiceType() == AcsServiceServiceType.IFR)
					return 0;
				else 
					return -1;
			case NOTIFICATION:
				if (o2.getServiceType() == AcsServiceServiceType.NAMING || 
						o2.getServiceType() == AcsServiceServiceType.IFR )
					return 1;
				else if (o2.getServiceType() == AcsServiceServiceType.NAMING)
					return 0;
				else 
					return -1;
			case LOGGING:
				if (o2.getServiceType() == AcsServiceServiceType.NAMING || 
						o2.getServiceType() == AcsServiceServiceType.IFR ||
						o2.getServiceType() == AcsServiceServiceType.NAMING)
					return 1;
				else if (o2.getServiceType() == AcsServiceServiceType.LOGGING)
					return 0;
				else 
					return -1;
			case LOGPROXY:
				if (o2.getServiceType() == AcsServiceServiceType.NAMING || 
						o2.getServiceType() == AcsServiceServiceType.IFR ||
						o2.getServiceType() == AcsServiceServiceType.NAMING ||
						o2.getServiceType() == AcsServiceServiceType.LOGGING)
					return 1;
				else if (o2.getServiceType() == AcsServiceServiceType.LOGPROXY)
					return 0;
				else 
					return -1;
			case CDB:
				if (o2.getServiceType() == AcsServiceServiceType.NAMING || 
						o2.getServiceType() == AcsServiceServiceType.IFR ||
						o2.getServiceType() == AcsServiceServiceType.NAMING ||
						o2.getServiceType() == AcsServiceServiceType.LOGGING ||
						o2.getServiceType() == AcsServiceServiceType.LOGPROXY)
					return 1;
				else if (o2.getServiceType() == AcsServiceServiceType.CDB)
					return 0;
				else 
					return -1;
			case MANAGER:
				if (o2.getServiceType() == AcsServiceServiceType.NAMING || 
						o2.getServiceType() == AcsServiceServiceType.IFR ||
						o2.getServiceType() == AcsServiceServiceType.NAMING ||
						o2.getServiceType() == AcsServiceServiceType.LOGGING ||
						o2.getServiceType() == AcsServiceServiceType.LOGPROXY ||
						o2.getServiceType() == AcsServiceServiceType.CDB)
					return 1;
				else if (o2.getServiceType() == AcsServiceServiceType.MANAGER)
					return 0;
				else 
					return -1;
			case ALARM:
				if (o2.getServiceType() == AcsServiceServiceType.NAMING || 
						o2.getServiceType() == AcsServiceServiceType.IFR ||
						o2.getServiceType() == AcsServiceServiceType.NAMING ||
						o2.getServiceType() == AcsServiceServiceType.LOGGING ||
						o2.getServiceType() == AcsServiceServiceType.LOGPROXY ||
						o2.getServiceType() == AcsServiceServiceType.CDB ||
						o2.getServiceType() == AcsServiceServiceType.MANAGER )
					return 1;
				else if (o2.getServiceType() == AcsServiceServiceType.ALARM)
					return 0;
				else 
					return -1;
			}
			return 0;
		}
		
	}
	
	private class MyCallback extends DaemonCallbackPOA {

		@Override
		public void done(Completion comp) {
			System.out.println("Completed Status:" + comp.type);
			lock.lock();
			notCompleted.signal();
			lock.unlock();
		}

		@Override
		public void working(Completion comp) {
			System.out.println("Status:" + comp.type);
		}
		
	}
	
	private class ORBThread extends Thread {

		@Override
		public void run() {
			orb.run();
		}
		
	}
	
	private HibernateUtil hibU;
	private Configuration config;
	private final String[] args;
	private final HashMap<String, ServicesDaemonOperations> serviceDaemonCache;
	private final HashMap<String, ContainerDaemonOperations> containerDaemonCache;
	private final ReentrantLock lock;
	private final Condition notCompleted;
	private final org.omg.CORBA.ORB orb;
	private final org.omg.PortableServer.POA poa;
	
	public AcsStartRemote(String[] args) throws InvalidName, AdapterInactive {
		this.args = args;
		HibernateWDALPlugin plugin = PluginFactory.getPlugin(Logger.getAnonymousLogger());
		HibernateDBUtil util = new HibernateDBUtil(Logger.getAnonymousLogger(), plugin);
		util.setUp(false, false);
		hibU = HibernateUtil.getInstance(Logger.getAnonymousLogger());
		Session session = hibU.getSessionFactory().openSession();
		try {
		session.beginTransaction();
		config = (Configuration)session.createCriteria(Configuration.class).
				add(Restrictions.eq("configurationName", configName)).uniqueResult();
		} finally {
			if (session != null)
				session.close();
		}
		
		serviceDaemonCache = new HashMap<>();
		containerDaemonCache = new HashMap<>();
		lock = new ReentrantLock();
		notCompleted = lock.newCondition();
		
		orb = org.omg.CORBA.ORB.init(args, null);
		poa = org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
		poa.the_POAManager().activate();
		ORBThread thread = new ORBThread();
		thread.setDaemon(true);
		thread.start();
	}
	
	public void cleanup() {
		orb.shutdown(false);
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<T> getListForConfiguration(Session session, Class<T> type)
	{
		List<T> result = null;
		result = session.createCriteria(type.getClass()).add(Restrictions.eq("configuration", config)).list();
        return result;
	}
	
	public List<AcsService> getServicesDeployment() {
		Session session = hibU.getSessionFactory().openSession();
		List<AcsService> services = null;
		try {
			session.beginTransaction();
			session.refresh(config);
			Hibernate.initialize(config.getAcsServices());
			services = new ArrayList<AcsService>(config.getAcsServices());
			for (AcsService s : services)
				s.getComputer().getNetworkName();
		} finally {
			if (session != null)
				session.close();
		}
		services.sort(new AcsServiceComparator());
		return services;
	}
	
	public List<Container> getContainersDeployment() {
		Session session = hibU.getSessionFactory().openSession();
		List<Container> containers = null;
		try {
			session.beginTransaction();
			session.refresh(config);
			Hibernate.initialize(config.getContainers());
			containers = new ArrayList<Container>(config.getContainers());
			for (Container c: containers) {
				c.getComputer().getNetworkName();
				for (ContainerStartupOption cs: c.getContainerStartupOptions()) {
					cs.getOptionType();
				}
			}
		} finally {
			if (session != null)
				session.close();
		}
		return containers;
	}
	
	
	public void startServices() {
		List<AcsService> services = getServicesDeployment();
		for (AcsService s : services) {
			System.out.println("Starting: " + s.getServiceType() + " " + s.getServiceInstanceName() + " " + s.getComputer().getNetworkName());
			try {
				lock.lock();
				startService(s);
				notCompleted.await();
			} catch (BadParameterEx e) {
				e.printStackTrace();
			} catch (ServiceAlreadyRunningEx e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ServantNotActive e) {
				e.printStackTrace();
			} catch (WrongPolicy e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
	}
	
	public void startService(AcsService service) throws BadParameterEx, ServiceAlreadyRunningEx, ServantNotActive, WrongPolicy {
		ServicesDaemonOperations daemon = getServiceDaemonRef(service.getComputer());
		
		DaemonCallback callback = DaemonCallbackHelper.narrow(poa.servant_to_reference(new MyCallback()));
		switch (service.getServiceType()) {
		case NAMING:
			daemon.start_naming_service(callback, acsInstance);
			break;
		case IFR:
			daemon.start_interface_repository(true, false, callback, acsInstance);
			break;
		case NOTIFICATION:
			daemon.start_notification_service(service.getServiceInstanceName(), callback, acsInstance);
			break;
		case LOGGING:
			daemon.start_logging_service("", callback, acsInstance);
			break;
		case LOGPROXY:
			daemon.start_acs_log(callback, acsInstance);
			break;
		case CDB:
			daemon.start_rdb_cdb(callback, acsInstance, true, configName);
			break;
		case ALARM:
			daemon.start_alarm_service(callback, acsInstance);
			break;
		case MANAGER:
			daemon.start_manager("", callback, acsInstance, true);
			break;
		}
	}
	
	public void startContainers() throws BadParameterEx, FailedToStartContainerEx {
		List<Container> containers = getContainersDeployment();
		for (Container c: containers) {
			//FIXME Add better handling of more than 1 startup option per container
			String startupOptions = "";
			if (c.getContainerStartupOptions() != null && c.getContainerStartupOptions().size() > 0)
				startupOptions = c.getContainerStartupOptions().iterator().next().getOptionValue();
			String[] typeModifiers = new String[0];
			if (c.getTypeModifiers() != null)
				typeModifiers = c.getTypeModifiers().split(",");
			ContainerDaemonOperations daemon = getContainerDaemonRef(c.getComputer());
			daemon.start_container(c.getImplLang().toString(), c.getPath() + "/" + c.getContainerName(), acsInstance, typeModifiers, startupOptions);
		}
	}
	
	private ServicesDaemonOperations getServiceDaemonRef(Computer comp) {
		if (!serviceDaemonCache.containsKey(comp.getNetworkName())) {
			String loc = "corbaloc::" + comp.getNetworkName() + ":" + ACSPorts.getServicesDaemonPort() + "/ACSServicesDaemon";
			org.omg.CORBA.Object object = orb.string_to_object(loc);
			ServicesDaemon daemon = ServicesDaemonHelper.narrow(object);
			serviceDaemonCache.put(comp.getNetworkName(), daemon);
		}
		return serviceDaemonCache.get(comp.getNetworkName());
		
	}
	
	private ContainerDaemonOperations getContainerDaemonRef(Computer comp) {
		if (!containerDaemonCache.containsKey(comp.getNetworkName())) {
			String loc = "corbaloc::" + comp.getNetworkName() + ":" + ACSPorts.getContainerDaemonPort() + "/ACSContainerDaemon";
			org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
			org.omg.CORBA.Object object = orb.string_to_object(loc);
			ContainerDaemon daemon = ContainerDaemonHelper.narrow(object);
			containerDaemonCache.put(comp.getNetworkName(), daemon);
		}
		return containerDaemonCache.get(comp.getNetworkName());
	}
	
	public static void main(String[] args) throws BadParameterEx, FailedToStartContainerEx, InvalidName, AdapterInactive {
		AcsStartRemote start = new AcsStartRemote(args);
		start.startServices();
		start.startContainers();
		start.cleanup();
	}

}
