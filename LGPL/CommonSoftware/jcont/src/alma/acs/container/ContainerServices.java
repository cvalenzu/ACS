/*
 *    ALMA - Atacama Large Millimiter Array
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
 */
package alma.acs.container;

import java.util.List;

import si.ijs.maci.ComponentSpec;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.ComponentDescriptor;
import alma.acs.component.ComponentQueryDescriptor;
import alma.acs.component.ComponentStateManager;
import alma.entities.commonentity.EntityT;

/**
 * Through this interface, the container offers services to its hosted components.
 * The component must call these methods explicitly; in this respect, 
 * <code>ContainerServices</code> is different from the other services that the container
 * provides without the component implementation knowing about it. 
 * It can be thought of as a callback handle or a library.
 * <p>
 * Currently, methods are added to this interface as the functionality becomes available. 
 * At some point we will have to declutter the interface by introducing 2nd-level interfaces
 * that harbor cohesive functionality. For example, instead of calling 
 * <code>myContainerServices.getComponent(...)</code>, the new call will then be something like
 * <code>myContainerServices.communication().getComponent(...)</code>.
 * <p>
 * created on Oct 24, 2002 12:56:36 PM
 * @author hsommer
 */
public interface ContainerServices extends ContainerServicesBase
{
	/////////////////////////////////////////////////////////////
	// basic needs (the rest comes from the base class)
	/////////////////////////////////////////////////////////////
		
        
	/**
	 * Delivers the <code>ComponentStateManager</code> object
	 * through which the component and the container administrate the
	 * state of the component.
	 * <p>
	 * The component needs to access the <code>ComponentStateManager</code>
	 * if it wishes to change its state. 
	 * If it doesn't, only the container will change the state based on 
	 * the information it has available.
	 * 
	 * @return the state manager
	 * @see alma.ACS.ComponentStates
	 */
	public ComponentStateManager getComponentStateManager();

	
	/////////////////////////////////////////////////////////////
	// access to other components
	/////////////////////////////////////////////////////////////
		
	/**
	 * Gets a component specified by its instance name.
	 * Delegates to {@link si.ijs.maci.ManagerOperations#get_component(int, java.lang.String, boolean, org.omg.CORBA.IntHolder) get_component}.
	 * <ul>
	 * <li> This method is necessarily generic and will require a cast to the 
	 * 		requested interface, like in <code>HelloComponent helloComp = 
	 * 		HelloComponentHelper.narrow(containerServices.getComponent("HELLOCOMP1"));</code>.
	 * 
	 * <li> the more general method <code>get_service</code> offered by the manager
	 * 		is deliberately not represented here. It would currently (Oct.03) offer
	 * 		access to "LogFactory", "NotifyEventChannelFactory", "ArchivingChannel",
	 * 		"LoggingChannel", "InterfaceRepository", "CDB", "ACSLogSvc", "PDB";
	 * 		It seems that if such access is needed, specialized methods should be added
	 * 		to this interface, like {@link #getCDB()}.
	 * 		
	 * <li> if requested, we may come up with some additional way 
	 * 		(e.g. a code-generated singleton class) 
	 * 		to give type safe access to other components, like done with EJB xxxHome.
	 * 
	 * <li> the Container will later shortcut calls for components inside the same process. 
	 *     Like in EJB and CORBA, the implementation must therefore not assume receiving a 
	 *     by-value copy of any parameter from the other component.
	 * </ul>
	 * 
	 * @param componentUrl  the ACS CURL of the deployed component instance.
	 * @return  the CORBA proxy for the component.
	 * @throws AcsJContainerServicesEx  if something goes wrong.
	 */
	public org.omg.CORBA.Object getComponent(String componentUrl) 
			throws AcsJContainerServicesEx;
	
	/**
	 * Gets a non-sticky reference to a component.
	 * This is typically used by "weak clients" such as graphical user interfaces that only want to observe the running system
	 * without interfering with its functioning.
	 * <p>
	 * A non-sticky reference does not bind the Manager to keep alive the Component, and the Client requesting a non-sticky reference 
	 * is not considered when checking for reference counts.
	 * The non-sticky reference should not be released, as that call will fail.
	 * The Manager can deactivate Components independently of any non-sticky reference.
	 * Since a non-sticky reference is not considered in reference counting, it will also not activate the component if it is 
	 * not already active. As a consequence, asking for a non-sticky reference to a not-active Component throws an exception. 
	 * The client represented by id (the handle) must have adequate access rights to access the component.
	 * @param curl the component URL (component instance name)
	 * @return  the CORBA proxy for the component.
	 * @throws AcsJContainerServicesEx if something goes wrong
	 * @since ACS 6.0
	 */
	public org.omg.CORBA.Object getComponentNonSticky(String curl) 
			throws AcsJContainerServicesEx;


	/**
	 * Gets the default component specified by the component type.
	 * The type is the IDL type, such as <code>IDL:alma/PS/PowerSupply:1.0</code>.
	 * <p>
	 * A default instance for the given type must have been declared beforehand
	 * (either statically in the CDB config files, or dynamically),
	 * otherwise an exception is thrown.
	 * <p>
	 * To get more information on the returned component, call
	 * {@link #getComponentDescriptor} with the instance name that
	 * you can get from the component using {@link alma.ACS.ACSComponentOperations#name}.
	 * <p>
	 * Delegates to {@link si.ijs.maci.ManagerOperations#get_default_component}.
	 * @param componentIDLType 
	 * @return 
	 * @throws AcsJContainerServicesEx 
	 */
	public org.omg.CORBA.Object getDefaultComponent(String componentIDLType) 
			throws AcsJContainerServicesEx;
	

	/**
	 * Gets a component that will run collocated with a given component.
	 * @param compUrl  the component's name (URL)
	 * @param targetCompUrl  the name (URL) of the target component, in whose container we also want <code>compUrl</code> to run.
	 * @return  the component reference, which should be cast using the appropriate CORBA narrow operation. Never null.
	 * @throws AcsJContainerServicesEx If the call failed and no component reference could be obtained.
	 * @since ACS 5.0.3
	 */
	public org.omg.CORBA.Object getCollocatedComponent(String compUrl, String targetCompUrl) throws AcsJContainerServicesEx;
	
	/**
	 * Dynamic version of {@link #getCollocatedComponent(String, String)}.
	 * @param compSpec  the description of the component to be created
	 * @param markAsDefaul  if true, the new component will become the default component for its IDL type.
	 * @param targetCompUrl targetCompUrl  the name (URL) of the target component, in whose container we also want <code>compUrl</code> to run.
	 * @return 
	 * @throws AcsJContainerServicesEx If the call failed and no component reference could be obtained.
	 * @since ACS 6.0.4
	 */
	public org.omg.CORBA.Object getCollocatedComponent(ComponentQueryDescriptor compSpec, boolean markAsDefaul, String targetCompUrl) throws AcsJContainerServicesEx;
	
	
	/**
	 * Gets a component whose instance is not registered in the CDB 
	 * at deployment time.
	 * <p>
	 * The fields of <code>compSpec</code> can be filled in at various levels of detail,
	 * allowing the manager to blend in missing data from static deployment information.
	 * <b>For a detailed description of the various options, 
	 * please refer to the ACS documentation.</b>
	 * <p>
	 * To get more information on the returned component, call
	 * {@link #getComponentDescriptor} with the instance name that
	 * you've specified or that you can get from the component in case it was 
	 * assigned (see {@link alma.ACS.ACSComponentOperations#name()}).
	 * <p>
	 * Delegates to {@link si.ijs.maci.ManagerOperations#get_dynamic_component}.
	 * 
	 * @param compSpec  the description of the component to be created
	 * @param markAsDefault  if true, the new component will become the default component for its IDL type.	
	 */
	public org.omg.CORBA.Object getDynamicComponent(ComponentQueryDescriptor compSpec, boolean markAsDefault) 
			throws AcsJContainerServicesEx;

    /**
	 * More powerful and thus more dangerous version of {@link #getDynamicComponent(ComponentQueryDescriptor, boolean)}
	 * which exposes a CORBA struct directly. 
	 * 
	 * @param compSpec  the description of the component to be created
	 * @param markAsDefault  if true, the new component will become the default component for its IDL type.
	 * @deprecated use {@link #getDynamicComponent(ComponentQueryDescriptor, boolean)} instead. 
	 * 				if you need to set not only the standard fields <code>component_name</code> or <code>component_type</code>,
	 * 				but also the more advanced values <code>component_code</code> or <code>container_name</code>,
	 * 				please send a request to ACS so that access to these fields be included in the 
	 * 				<code>ComponentQueryDescriptor</code> given to the recommended version of this method.    	
	 */
	public org.omg.CORBA.Object getDynamicComponent(ComponentSpec compSpec, boolean markAsDefault) 
			throws AcsJContainerServicesEx;


	
	
	/**
	 * Finds components by their instance name (curl) and/or by their type.
	 * Wildcards can be used for the curl and type.
	 * This method returns a possibly empty array of component curls; 
	 * for each curl, you may then call {@link #getComponent(String) getComponent} 
	 * to obtain the reference.
	 * 
	 * @param curlWildcard (<code>null</code> is understood as "*")
	 * @param typeWildcard (<code>null</code> is understood as "*")
	 * @return the curls of the component(s) that match the search.
	 * @see si.ijs.maci.ManagerOperations#get_component_info(int, int[], java.lang.String, java.lang.String, boolean)
	 */
	public String[] findComponents(String curlWildcard, String typeWildcard)  
		throws AcsJContainerServicesEx;


	/**
	 * Gets the component descriptor which contains meta data for the component.
	 * (Not to be confused with a component descriptor in the EJB sense.)
	 * <p>
	 * To be used primarily after retrieval of a component with
	 * {@link #getDefaultComponent(String) getDefaultComponent} 
	 * or {@link #getDynamicComponent(ComponentQueryDescriptor, boolean) getDynamicComponent},
	 * when some data like the instance name is not known.
	 * The idea behind having this method separately is that in many cases,
	 * components are not interested in this information, and are happier to 
	 * get back from these methods directly the remote reference to another component,
	 * instead of always dealing with a <code>ComponentDescriptor</code>.
	 * 
	 * @param componentUrl  the unique name of a component  
	 * @return the descriptor from which the various data items can be obtained.
	 * @throws ContainerException  if <code>componentUrl</code> is unknown 
	 * 				or anything else goes wrong
	 * @see si.ijs.maci.ComponentInfo	
	 */
	public ComponentDescriptor getComponentDescriptor(String componentUrl)
		throws AcsJContainerServicesEx;
	
	
	/**
	 * Releases the specified component. This involves notification of the manager,
	 * as well as calling <code>_release()</code> on the CORBA stub for that component.
	 * If the curl is not known to the container, the request will be ignored.
	 * <p>
	 * This method will return only when the component has actually been released,
	 * which may take some time in case there are still active requests being processed.
	 * 
	 * @param componentUrl  the name/curl of the component instance as used by the manager  
	 */
	public void releaseComponent(String componentUrl);
	
	
	public static interface ComponentListener {
		/**
		 * Called to find out whether a filter should be applied 
		 * such that only notifications arrive for components to which the caller holds a reference.
		 */
		boolean includeForeignComponents();
		
		/**
		 * Called when components become available
		 */
		void componentsAvailable(List<ComponentDescriptor> comps);
		
		/**
		 * Called when components become unavailable
		 */
		void componentsUnavailable(List<String> compNames); 
	}
	
	/**
	 * Allows a client to register a callback object that gets notified when some  
	 * component(s) in use by the client (= components the client requested previously)
	 * dies or comes back to life (with <code>ComponentListener#includeForeignComponents()==false</code>). 
	 * <p>
	 * If the client wants to get notified even for components that it does not hold a reference to,
	 * then <code>ComponentListener#includeForeignComponents()</code> should return <code>true</code>.
	 * {@link ContainerServices}
	 * @param listener
	 * @see si.ijs.maci.ClientOperations#components_available(si.ijs.maci.ComponentInfo[])
	 * @since ACS 6.0
	 */
	public void registerComponentListener(ComponentListener listener);
	

	/////////////////////////////////////////////////////////////
	// support for XML entities and binding classes
	/////////////////////////////////////////////////////////////
		
	/**
	 * Creates a unique id and sets it on the given (binding class) entity object.
	 * The id will be redundantly stored in an encrypted format so that later it can be verified that 
	 * the id is indeed the one originally assigned. 
	 * 
	 * @param entity 	must be freshly created and 
	 */
	public void assignUniqueEntityId(EntityT entity) throws AcsJContainerServicesEx;
	
	
	/**
	 * Converts a "flat-XML" component interface 
	 * (as obtained from the various <code>getComponent</code> methods)
	 * to a "transparent-XML" component interface.
	 * This is only applicable to components that contain XML entities
	 * in their IDL interface methods, and for which the build process
	 * has been set up to generate XML binding classes and the "transparent-XML" interface
	 * in addition to the standard Java-IDL compiler output.
	 * <p>
	 * The container can fulfill this request in two different ways:
	 * <ol>
	 * <li><b>Remote component</b>:
	 * 		if the specified component runs in a different container, 
	 * 		a dynamic wrapper object will be created around the provided CORBA stub.
	 * 		The wrapper object will translate between entity object parameters 
	 * 		in the form of serialized XML (as found in <code>flatXmlIF</code>) 
	 * 		and the corresponding Java binding classes (in <code>transparentXmlIF</code>).
	 * <li><b>Collocated component</b>:
	 * 		if the specified component runs in the same container, the container
	 * 		may choose to shortcut the (de-) serialization of XML binding classes.
	 * 		In this case, Java binding classes are transported in memory,
	 * 		and all communication with the other component is done without CORBA. 
	 * </ol>
	 * In either case, the returned <code>Object</code> implements the 
	 * <code>transparentXmlIF</code> interface. 
	 * The client component that calls this method should only cast to that interface,
	 * and does not need to know which of the two transport mechanisms are being used.   
	 * 
	 * @param transparentXmlIF  component interface with XML binding classes.
	 * @param componentReference  reference to the component to be wrapped, as 
	 * 								obtained through <code>getComponent(String)</code>, 
	 * 								thus implements <code>flatXmlIF</code>. 
	 * @param flatXmlIF  component interface where entity objects are represented as 
	 * 					serialized XML inside a CORBA struct.
	 * @return the object that implements <code>transparentXmlIF</code>.
	 */
	public <T> T getTransparentXmlComponent(
		Class<T> transparentXmlIF,
		org.omg.CORBA.Object componentReference,
		Class flatXmlIF) 
		throws AcsJContainerServicesEx;

    
    /////////////////////////////////////////////////////////////
    // other
    /////////////////////////////////////////////////////////////
}
