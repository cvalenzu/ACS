#ifndef _ACS_IMP_BASE_HANDLER_IMPL_H_
#define _ACS_IMP_BASE_HANDLER_IMPL_H_

/*******************************************************************************
*    ALMA - Atacama Large Millimiter Array
*    (c) European Southern Observatory, 2002
*    Copyright by ESO (in the framework of the ALMA collaboration)
*    and Cosylab 2002, All rights reserved
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
*    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
*
* "@(#) $Id: acsImpBaseHandlerImpl.h,v 1.2 2008/10/29 08:37:57 msekoran Exp $"
*
* who       when      what
* --------  --------  ----------------------------------------------
* azagar   2008-09-30 created
*/

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

#include "acsDaemonImpl.h"
#include "acsRequest.h"
#include "acsServiceController.h"

#include "ACSAlarmSystemInterfaceFactory.h"


template <class T> class ACSImpBaseHandlerImpl : public virtual POA_acsdaemon::ImpBase {

  public:
    
    /**
     * Constructor
     */
    ACSImpBaseHandlerImpl(ACSServiceType iservice) : service(iservice) {
        context = new ACSDaemonContext(acsServices[service].impname);
    }
  
    /**
     * Destructor
     */
    virtual ~ACSImpBaseHandlerImpl() {
        ACS_SHORT_LOG((LM_DEBUG, "DESTROYING ACSImpHandlerImpl!"));
        delete context;
    }

    /**
     * Sets ACS Daemon service
     */
    void setService(ACSDaemonServiceImpl<T> *serviceimpl)
    {
        serviceimpl = serviceimpl;
    }

    /**
     * Initialize handler
     */
    void initialize(CORBA::ORB_ptr orb) {
        context->initialize(orb);
	
	// initialize the AlarmSystemInterfaceFactory
	ACSAlarmSystemInterfaceFactory::init(NULL);
    }

    /**
     * Dispose handler
     */
    void dispose(CORBA::ORB_ptr orb) {
	ACSAlarmSystemInterfaceFactory::done();
	context->dispose(orb);
    }

    /**
     * Get the name of this container handler
     */
    std::string getName() {
        return acsServices[service].impname;
    }

    /**
     * Get the type string of this container handler
     */
    std::string getType() {
        return acsServices[service].imptype;
    }
    
    /**
     * Return the port where this services handler listens for connections
     */
    std::string getPort() {
        return acsServices[service].impport;
    }
    
    /*************************** CORBA interface *****************************/

    void shutdown() {
        if (serviceimpl->isProtected())
	    {
            throw ::maciErrType::NoPermissionEx();
	    }
        ACS_SHORT_LOG ((LM_INFO, "Shutting down the ACS Imp on remote request..."));
        serviceimpl->shutdown(false);
    }

    bool ping() {
        return true;
    }

  protected:
    ACSServiceType service;
    ACSDaemonServiceImpl<T> *serviceimpl; // ACS daemon service
    ACSDaemonContext *context;
};



#endif
