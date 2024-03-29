# -*- coding: utf-8 -*-
##############################################################################
#
#    OpenERP, Open Source Management Solution
#    Copyright (C) 2004-2010 Tiny SPRL (<http://tiny.be>).
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU Affero General Public License as
#    published by the Free Software Foundation, either version 3 of the
#    License, or (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU Affero General Public License for more details.
#
#    You should have received a copy of the GNU Affero General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
##############################################################################

from osv import osv,fields
import netsvc
import base64
from tools.translate import _
import time

class account_payment_make_payment(osv.osv_memory):
    _inherit = "account.payment.make.payment"
#    _description = "Account make payment"
    _columns = {
        'state': fields.selection(
            [
                ('create', 'Create'),
                ('finish', 'Finish')
            ],
            'State',
            readonly=True,
        ),
       'order_number': fields.integer('Number of exported orders'),
#       'filename': fields.char('Filename', size=64),
#       'files': fields.binary(string='File', readonly = True ),
        }
    _defaults = {
        'state' : 'create'
    }

    def launch_wizard(self, cr, uid, ids, context=None):
        """
        Search for a wizard to launch according to the type.
        If type is manual. just confirm the order.
        """
#        logger = netsvc.Logger()
#        netsvc.Logger().notifyChannel("in launch wizard", netsvc.LOG_WARNING,"The line: %s"%context['active_id'])

        obj_payment_order = self.pool.get('payment.order')
        if context is None:
            context = {}
        settings_obj = self.pool.get('bank.integration.settings')
        exported_file_obj = self.pool.get('account.banking.exported.file')
        bank_parsers_obj = self.pool.get('bank.parsers')
        order_no =0
        for order in obj_payment_order.browse(cr, uid, context['active_ids'], context):
#        order = obj_payment_order.browse(cr, uid, context['active_id'], context)
        # get the parser to parse the file
            if order.state != 'open':
                raise osv.except_osv( _('ERROR!'),_('Order %s is not in Confirmed state.') % order.reference)
            if not order.mode.banking_settings_id or not order.mode.banking_settings_id.active:
# TODO    SET DONE   !!!!!
                obj_payment_order.set_done(cr, uid, [order.id], context)
                continue

            for line in order.line_ids:
                if not line.bank_id:
                    raise osv.except_osv( _('ERROR!'), _('Line %s has no bank account.') % line.name)
            parser_code = order.mode.banking_settings_id.parser + "_export"
            if not hasattr(bank_parsers_obj,parser_code):
                raise osv.except_osv( _('ERROR!'),
                    _('Unable to find export method for parser %(parser)s. Parser class not found.') % {'parser': parser_code})
            parser = getattr(bank_parsers_obj,parser_code)
            if not parser:
                raise osv.except_osv( _('ERROR!'),_('Unable to import parser %(parser)s. Parser class not found.') % {'parser': parser_code})
            file_data = parser(cr, uid, context['active_id'], context=context)
            file = base64.encodestring(file_data)
            filename = order.reference.replace("/","_") + "_" + parser_code +'.csv'
#        netsvc.Logger().notifyChannel("in launch wizard", netsvc.LOG_WARNING,"FILENAME: %s"%filename)
            export_result = {
                'company_id': order.mode.company_id.id,
#             'date': ,
                'format': parser_code,
                'log': "log ....",
                'user_id': uid,
                'state': 'ready',
                'pay_order_ids': [
                        [6, 0, [order.id]]
                    ],
                'filename' : filename,
                'file': file,
            }
            file_id = exported_file_obj.create(cr, uid, export_result, context)
#        netsvc.Logger().notifyChannel("in launch wizard", netsvc.LOG_WARNING,"FILENAME file _id: %s"%filename)
            order_no +=1
    #        netsvc.Logger().notifyChannel("in launch wizard", netsvc.LOG_WARNING,"After write FILENAME file _id: %s"%filename)
    
# TODO    SET DONE   !!!!!
            if order.mode.banking_settings_id.action_after_export == 'set_done' and order.state == 'open':
                obj_payment_order.set_done(cr, uid, [order.id], context)
        self.write(cr, uid, [ids[0]], {
#                    'file': file,
#                    'filename' : filename,
                'order_number' : order_no,
                'state': 'finish',
        }, context)
        return {
                'name': _('Exported file'),
                'view_type': 'form',
                'view_mode': 'form',
                'res_model': self._name,
                'domain': [],
                'context': dict(context, active_ids=ids),
                'type': 'ir.actions.act_window',
                'target': 'new',
                'res_id': ids[0] or False,
            }
account_payment_make_payment()
# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
