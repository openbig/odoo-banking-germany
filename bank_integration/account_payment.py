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


# This is banking platform for OpenERP based on great work of EduSense.
# The platform keeps a lot of ideas of project account_banking but is rewritten
# to be much easier and much closer to basic OpenERP functionality.
# Most of EduSense description are left in the code but could be not releveant
# to current state.

#import time
#import sys
#import sepa
from osv import osv, fields
from tools.translate import _
#from wizard.banktools import get_or_create_bank
#import decimal_precision as dp
#import pooler
#import netsvc

class account_payment(osv.osv):
    _inherit = 'payment.order'
#    _order = 'id'
    _order = "id desc"
    _columns = {
        'exported_file_id': fields.many2one('account.banking.exported.file', 'Exported File', readonly=True, ),
    }
account_payment()

class payment_mode(osv.osv):
    _inherit= 'payment.mode'
#    _description= 'Payment Mode'
    _columns = {
        'banking_settings_id': fields.many2one('bank.integration.settings','Export Settings', ondelete='set null', help='Settings for Payment Export'),
    }
payment_mode()

# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
