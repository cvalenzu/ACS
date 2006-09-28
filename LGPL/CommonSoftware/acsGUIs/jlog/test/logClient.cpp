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
*
*
* "@(#) $Id: logClient.cpp,v 1.1 2006/09/28 16:12:24 acaproni Exp $"
*
* who       when      what
* --------  --------  ----------------------------------------------
* acaproni 2006-09-28 created
*/

#include <maciSimpleClient.h>

using namespace maci;

int main(int argc, char *argv[])
{
    // Creates and initializes the SimpleClient object
    SimpleClient client;
    if (client.init(argc,argv) == 0)
        {
        return -1;
        }
    else
        {
        //Must log into manager before we can really do anything
        client.login();
        }

    for (int t=0; t<100; t++) 
	{
	ACS_SHORT_LOG ((LM_INFO, "Test message %d",t));
	}

    client.logout();

    //Sleep for 10 sec to allow everytihng to cleanup and stablize
    ACE_OS::sleep(10);
    return 0;
}
