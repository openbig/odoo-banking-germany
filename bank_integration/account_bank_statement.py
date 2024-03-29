# -*- encoding: utf-8 -*-
##############################################################################
#
#    Copyright (C) 2012 OpenGLOBE (<http://www.openglobe.pl>).
#    All Rights Reserved
#    Copyright (C) 2009 EduSense BV (<http://www.edusense.nl>).
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
import decimal_precision as dp
#import pooler
#import netsvc

class account_bank_statement(osv.osv):
    _inherit = 'account.bank.statement'
    _columns = {
        'imported_file_id': fields.many2one('account.banking.imported.file', 'Imported File', readonly=True, ),
    }

#    def button_confirm_bank(self, cr, uid, ids, context=None):
#        if context is None: context = {}
#        obj_seq = self.pool.get('ir.sequence')
#        if not isinstance(ids, list): ids = [ids]
#        noname_ids = self.search(cr, uid, [('id','in',ids),('name','=','/')])
#        for st in self.browse(cr, uid, noname_ids, context=context):
#                if st.journal_id.sequence_id:
#                    year = self.pool.get('account.period').browse(cr, uid, self._get_period(cr, uid, st.date)).fiscalyear_id.id
#                    c = {'fiscalyear_id': year}
#                    st_number = obj_seq.get_id(cr, uid, st.journal_id.sequence_id.id, context=c)
#                    self.write(cr, uid, ids, {'name': st_number})
#        
#        return super(account_bank_statement, self).button_confirm_bank(cr, uid, ids, context)

account_bank_statement()
# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
