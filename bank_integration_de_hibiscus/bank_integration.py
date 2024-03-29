# -*- encoding: utf-8 -*-
##############################################################################
#
#    Copyright (C) 2012 OpenGLOBE (<http://www.openglobe.pl>).
#    All Rights Reserved
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
##############################################################################


#import time
#import sys
#import sepa
from osv import osv, fields
from tools.translate import _
import xmlrpclib
import urllib
#from wizard.banktools import get_or_create_bank
#import decimal_precision as dp
#import pooler
#import logging

class bank_integration_settings(osv.osv):
    _inherit = 'bank.integration.settings'

    _columns = {
        'hibiscus_account_id'    : fields.integer('Hibiscus Account ID', help ="Bank Account ID in Hibiscus system. Can be fetched by wizard 'Get Hibiscus accounts.'"),
        'hibiscus_server': fields.char('Server Name', size=64),
        'hibiscus_port': fields.char('Port Number', size=5),
        'hibiscus_user': fields.char('User Name', size=32),
        'hibiscus_password': fields.char('Password', size=32),
        'hibiscus_secure': fields.boolean('Secure Connection'),
    }
    _defaults = {
        'hibiscus_port': lambda *a: '8080',
        'hibiscus_server': lambda *a: 'localhost',
        'hibiscus_secure': lambda *a: True,
    }
bank_integration_settings()

class hibiscus_tools(osv.osv_memory):
    _name = 'hibiscus.tools'

    #creating url
    def get_server(self, cr, uid, server, user, pwd, port, secure):
#        logger = logging.getLogger('GET SERVER')
        if not server:
             raise osv.except_osv('Error!','Invalid Server')
        if not user:
             raise osv.except_osv('Error!','Invalid User Name')
        if not pwd:
             raise osv.except_osv('Error!','Invalid Password')
        if not port:
             raise osv.except_osv('Error!','Invalid Port')
        protocol = secure and 'https' or 'http'
        url = protocol + "://" + user + ':' + pwd + '@' +server+':'+port+'/xmlrpc/'
#        return url

    #connect to the hibiscus server
        server = None

#        logger.warn("URL: %s", url)
        if secure:
            server = xmlrpclib.ServerProxy(url)
            try:
                accounts = getattr(server,'hibiscus.xmlrpc.konto.find')()
            except Exception, e:
                if str(e) == "wrong version number":
                    from M2Crypto import m2xmlrpclib, SSL
                    ctx = SSL.Context('sslv3')
                    SSL.Connection.clientPostConnectionCheck = None
                    server = xmlrpclib.ServerProxy(url,transport = m2xmlrpclib.SSL_Transport(ctx))
                else:
                    raise osv.except_osv(_('Error!'),_('Cannot import Hibiscus accounts: %s')%e)
        else:
            server = xmlrpclib.ServerProxy(url)
        return server
hibiscus_tools()
# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
