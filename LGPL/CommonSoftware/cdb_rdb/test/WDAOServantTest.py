#!/usr/bin/env python
from Acspy.Common.CDBAccess import cdb
c = cdb()
dao = c.get_DAO_Servant('MACI/Components/CLOCK1')
print "DAO: ", dao.get_string("Name")
wdao = c.get_WDAO_Servant('MACI/Components/CLOCK1')
print "WDAO: ", wdao.get_string("Name")
