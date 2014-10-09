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

from osv import osv, fields
import time

class import_wizard(osv.osv_memory):
    _inherit = 'account.banking.bank.import'

    _columns = {
        'date_from':fields.date('From Date', 
            help="Start date of transactions to import from Hibiscus into bank statement. Used only in Hibiscus OnLine import. For other import ignored."),
        'date_to':fields.date('To Date',  
            help="End date of transactions to import from Hibiscus into bank statement. Used only in Hibiscus OnLine import. For other import ignored."),
        }

    _defaults = {
        'date_from': lambda *a: time.strftime('%Y-%m-%d'),
        'date_to': lambda *a: time.strftime('%Y-%m-%d'),
        }
import_wizard()
# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
