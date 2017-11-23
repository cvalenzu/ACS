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
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.cosylab.cdb.jdal.HibernateWDALImpl;
import com.cosylab.cdb.jdal.hibernate.HibernateDBUtil;
import com.cosylab.cdb.jdal.hibernate.HibernateUtil;
import com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin;
import com.cosylab.cdb.jdal.hibernate.plugin.PluginFactory;

import alma.ACSErrTypeCommon.BadParameterEx;
import alma.ACSErrTypeCommon.NoResourcesEx;
import alma.acs.tmcdb.AcsService;
import alma.acs.tmcdb.Computer;
import alma.acs.tmcdb.Configuration;
import alma.acs.util.ACSPorts;
import alma.acsdaemon.ServiceDefinitionBuilder;
import alma.acsdaemon.ServicesDaemon;
import alma.acsdaemon.ServicesDaemonHelper;
import alma.acsdaemon.ServicesDaemonOperations;

public class AcsStartRemote {

	private static final String configName;
	static {
		configName = System.getProperty(HibernateWDALImpl.TMCDB_CONFIGURATION_NAME_KEY, 
				System.getenv(HibernateWDALImpl.TMCDB_CONFIGURATION_NAME_KEY));
	}
	
	private HibernateUtil hibU;
	private Configuration config;
	private final String[] args;
	
	public AcsStartRemote(String[] args) {
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
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getListForConfiguration(Session session, Class<T> type)
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
		return services;
	}
	
	public void startServiceAt(AcsService service, Computer computer) {
	}
	
	private ServicesDaemonOperations getServiceDaemonRef(Computer comp) {
		String loc = "corbaloc::" + comp.getNetworkName() + ":" + ACSPorts.getServicesDaemonPort() + "/ACSServicesDaemon";
		org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
		org.omg.CORBA.Object object = orb.string_to_object(loc);
		ServicesDaemon daemon = ServicesDaemonHelper.narrow(object);
		return daemon;
		
	}
	
	public static void main(String[] args) {
		AcsStartRemote start = new AcsStartRemote(args);
		List<AcsService> services = start.getServicesDeployment();
		for (AcsService s : services)
			System.out.println("" + s.getServiceType() + " " + s.getServiceInstanceName() + " " + s.getComputer().getNetworkName());
		HibernateUtil.getInstance(Logger.getAnonymousLogger()).getSessionFactory().close();
		ServicesDaemonOperations daemon = start.getServiceDaemonRef(services.get(0).getComputer());
		try {
			ServiceDefinitionBuilder builder = daemon.create_service_definition_builder((short) 0);
			System.out.println(builder.get_services_definition());
		} catch (BadParameterEx e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoResourcesEx e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
