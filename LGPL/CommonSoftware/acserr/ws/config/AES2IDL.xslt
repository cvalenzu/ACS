<!-- edited with XMLSPY v5 rel. 2 U (http://www.xmlspy.com) by Bogdan Jeram (E.S.O.) -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acserr="Alma/ACSError">
	<xsl:output method="text" version="1.0" encoding="ASCII"/>
	<xsl:template match="/acserr:Type">
<xsl:text>#ifndef  _</xsl:text>
<xsl:value-of select="@name"/>
<xsl:text>_IDL_</xsl:text>
<xsl:text>
#define  _</xsl:text>
<xsl:value-of select="@name"/>
<xsl:text>_IDL_
	
/*******************************************************************************
* ALMA - Atacama Large Millimiter Array
* (c) European Southern Observatory, 2003 
*
*This library is free software; you can redistribute it and/or
*modify it under the terms of the GNU Lesser General Public
*License as published by the Free Software Foundation; either
*version 2.1 of the License, or (at your option) any later version.
*
*This library is distributed in the hope that it will be useful,
*but WITHOUT ANY WARRANTY; without even the implied warranty of
*MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*Lesser General Public License for more details.
*
*You should have received a copy of the GNU Lesser General Public
*License along with this library; if not, write to the Free Software
*Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
*
* "@(#) $Id: AES2IDL.xslt,v 1.9 2007/05/23 08:55:56 nbarriga Exp $"
*************  THIS FILE IS AUTOMATICALLY GENERATED !!!!!!
*/

#include &lt;acserr.idl&gt;

#pragma prefix "</xsl:text>
<xsl:value-of select="@_prefix"/>
<xsl:text>"
 	
module ACSErr 
{
	// type
	const ACSErr::ACSErrType </xsl:text>
	<xsl:value-of select="@name"/> 
	<xsl:text> = </xsl:text>	
	<xsl:value-of select="@type"/>
	<xsl:text>;
}; // module ACSErr
</xsl:text>
<xsl:text>
module </xsl:text>
<xsl:value-of select="@name"/> 
<xsl:text>
{
	</xsl:text>
	<xsl:for-each select="acserr:Code | acserr:ErrorCode">
	<xsl:text> const ACSErr::ErrorCode </xsl:text>
			<xsl:value-of select="@name"/>
			<xsl:text> = </xsl:text>
			<xsl:number value="position()-1"/>
			<xsl:text>;
	</xsl:text>
	</xsl:for-each>
<xsl:if test="count(//acserr:ErrorCode[not(@_suppressExceptionGeneration)]) > 0">
	<xsl:text>
	// excption for type:
	</xsl:text>
	
	<xsl:text>exception </xsl:text>
			<xsl:value-of select="@name"/>
		<xsl:text>Ex {
		ACSErr::ErrorTrace errorTrace;	
	};
	</xsl:text>

	<xsl:text>
	// excptions for codes:
	</xsl:text>

	<xsl:for-each select="acserr:ErrorCode[not(@_suppressExceptionGeneration)]">
<xsl:text>exception </xsl:text>
			<xsl:value-of select="@name"/>
		<xsl:text>Ex {
		ACSErr::ErrorTrace errorTrace;	
	};
	
	</xsl:text>
	</xsl:for-each>
</xsl:if>	
	<xsl:text>
}; // module </xsl:text>
<xsl:value-of select="@name"/>

<xsl:text>

#endif

</xsl:text>
</xsl:template>
</xsl:stylesheet>
