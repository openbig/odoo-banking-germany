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


# This is banking platform for OpenERP based on great work of EduSense and Therp.
# The platform keeps a lot of ideas of project account_banking but is rewritten
# to be much easier and much closer to basic OpenERP functionality.
# Most of EduSense description are left in the code but could be not releveant
# to current state.

import time
#import sys
#import sepa
from osv import osv, fields
from tools.translate import _
#from wizard.banktools import get_or_create_bank
import decimal_precision as dp
#import pooler
import netsvc

#def warning(title, message):
#    '''Convenience routine'''
#    return {'warning': {'title': title, 'message': message}}

class bank_integration_settings(osv.osv):
    '''Default Journal for Bank Account'''
    _name = 'bank.integration.settings'
#    _description = __doc__

    def _get_parser_list(self, cr, uid, context):
        bank_parsers_obj = self.pool.get('bank.parsers')
        return bank_parsers_obj.parser_selection(cr, uid, context)

    _columns = {
        'name': fields.char('Settings Name', size=64, required=True),
        'active' : fields.boolean('Active'),
        'company_id': fields.many2one('res.company', 'Company', select=True, required=True),
        'partner_bank_id': fields.many2one('res.partner.bank', 'Bank Account', select=True, required=True),
        'journal_id': fields.many2one('account.journal', 'Journal', help="Journal for Bank Statements created by Import."),     #, required=True),
        'default_credit_account_id': fields.many2one(
            'account.account', 'Default credit account', select=True,
            help=('The account to use when an unexpected payment was signaled. '
                  'This can happen when a direct debit payment is cancelled '
                  'by a customer, or when no matching payment can be found. '
                  ' Mind that you can correct movements before confirming them.' ),
            required=True
        ),
        'default_debit_account_id': fields.many2one(
            'account.account', 'Default debit account',
            select=True, required=True,
            help=('The account to use when an unexpected payment is received. '
                  'This can be needed when a customer pays in advance or when '
                  'no matching invoice can be found. Mind that you can correct '
                  'movements before confirming them.' ),
        ),
        'costs_account_id': fields.many2one(
            'account.account', 'Bank Costs Account', select=True,
            help=('The account used for bank costs.'),
        ),
        'income_account_id': fields.many2one(   # Added in account_bank_integration
            'account.account', 'Bank Income Account', select=True,
            help=('The account used for bank percentage.'),
        ),
#         'invoice_journal_id': fields.many2one(
#            'account.journal', 'Costs Journal', 
#            help=('This is the journal used to create invoices for bank costs.'),
#        ),
        'bank_partner_id': fields.many2one(
            'res.partner', 'Bank Partner',
            help=('The partner to use for bank costs. Banks are not partners '
                  'by default. You will most likely have to create one.'),
        ),
        'partner_matching' : fields.selection([     # Added in account_bank_integration
                ('no_matching', 'No Matching'), 
                ('by_bank_only', 'Match Only By Bank Account'), 
                ('by_name', 'Partner Matching by Name'), 
                ('by_invoice', 'Partner Matching by Invoice Number'), 
                ('partner_create', 'Create Partner When No Match'),
                ('partner_unknown', 'Match to Unknown Partner'),
            ],
            'Partner Matching', help = "No Matching - No partner matching. " \
                    "\nMatch Only By Bank Account - Look only for known Partners by Bank Account. Applied before all next options."\
                    "\nPartner Matching by Name - Look for Partner by Partner Name in description. If no matching, it search for partner's reference in description."\
                    "\nPartner Matching by Invoice Number - Look for Invoice Numbers in Description and match Partners and Invoice."\
                    "\nCreate Partner When No Match - Create Partner from Bank Statement data."\
                    "\nMatch to Unknown Partner - Assign transaction to partner with name 'Unknown' for farther processing. "\
                    "Like reassigning the bank account to existing Partner and matching the transaction to him."
        ),
        'invoice_matching' : fields.selection([ # Added in account_bank_integration
                ('no_matching', 'No Matching'), 
                ('one_matching', 'Match One Invoice by Description or Amount'), 
                ('full_matching', 'Match All Invoices by Description or Within Amount'), 
            ],
            'Invoice Matching', help = "No Match - Bank Integration will not try to match invoices at all. \
                    \nMatch One Invoice by Description or Amount - Bank Integration will try to find one invoice by description or Amount with tolerance.\
                    \nMatch All Invoices by Descritpion or Within Amount (not implemented yet)"
        ),
        'amount_tolerance' : fields.float('Amount Tolerance', 
                help = "Tolerance of amount for matching the invoice by amount. Tolerance is percentage (for 3% enter 3.00). \
It will mean that wizard match invoice to transfer amount when difference is less than 3%."),
        'action_after_export' : fields.selection([ # Added in account_bank_integration
                ('no_action', 'No Action'), 
                ('set_done', 'Set Order to Done'),
            ],
            'Action After Export', help = "No Action - Do not change Order state. \
                    \nSet Order to Done - After successful export change state to Done."
        ),
        'parser': fields.selection(
#            parser_types, 'File Format', required=True,
            _get_parser_list, 'File Format', required=True, # GG
            ),
        'payment_mode_ids' : fields.one2many('payment.mode','banking_settings_id', string="Payment Modes",),
    }

    _defaults = {
        'company_id': lambda self,cr,uid,c: self.pool.get('res.users').browse(cr, uid, uid, c).company_id.id,
        'partner_matching': 'by_bank_only',
        'invoice_matching': 'one_matching',
        'default_credit_account_id':lambda self,cr,uid,c: self.pool.get('res.users').browse(cr, uid, uid, c).company_id.partner_id.property_account_payable.id,
        'default_debit_account_id': lambda self,cr,uid,c: self.pool.get('res.users').browse(cr, uid, uid, c).company_id.partner_id.property_account_receivable.id,
#        'payment_delay' : 1,
        'active' : True,
        'amount_tolerance' : 3.0,
        'action_after_export' : 'set_done',
        #'multi_currency': lambda *a: False,
    }
bank_integration_settings()

class account_banking_imported_file(osv.osv):
    '''Imported Bank Statements File'''
    _name = 'account.banking.imported.file'
#    _description = __doc__
    _rec_name = 'date'
    _columns = {
        'company_id': fields.many2one('res.company', 'Company', select=True, readonly=True),
        'date': fields.datetime('Import Date', readonly=True, select=True, states={'unfinished': [('readonly', False)]} ),
        'format': fields.char('File Format', size=32, readonly=True, states={'unfinished': [('readonly', False)]} ),
        'file': fields.binary('Raw Data', readonly=True, states={'unfinished': [('readonly', False)]} ),
        'log': fields.text('Import Log', readonly=True, states={'unfinished': [('readonly', False)]} ),
        'user_id': fields.many2one('res.users', 'Responsible User', readonly=True, select=True, states={'unfinished': [('readonly', False)]}),
        'state': fields.selection(
            [('unfinished', 'Unfinished'),
             ('error', 'Error'),
             ('ready', 'Finished'),
            ], 'State', select=True, readonly=True
        ),
        'statement_ids': fields.one2many('account.bank.statement', 'imported_file_id', 'Statements', readonly=False,),
    }
    _defaults = {
        'date': lambda *a: time.strftime('%Y-%m-%d %H:%M:%S'),
        'user_id': lambda self, cursor, uid, context: uid,
    }
account_banking_imported_file()

class account_banking_exported_file(osv.osv):
#    Exported Bank Statements File
    _name = 'account.banking.exported.file'
#    _description = __doc__
    _rec_name = 'date'
    _columns = {
        'company_id': fields.many2one('res.company', 'Company',  select=True, readonly=True ),
        'date': fields.datetime('Export Date', readonly=True, select=True, states={'unfinished': [('readonly', False)]} ),
        'format': fields.char('File Format', size=32, readonly=True, states={'unfinished': [('readonly', False)]}),
        'filename': fields.char('Filename', size=64, readonly = True ,states={'unfinished': [('readonly', False)]}),
        'file': fields.binary('Raw Data', readonly=True, states={'unfinished': [('readonly', False)]}),
        'log': fields.text('Import Log', readonly=True, states={'unfinished': [('readonly', False)]} ),
        'user_id': fields.many2one('res.users', 'Responsible User', readonly=True, select=True, states={'unfinished': [('readonly', False)]} ),
        'state': fields.selection(
            [('unfinished', 'Unfinished'),
             ('error', 'Error'),
             ('ready', 'Finished'),
            ], 'State', select=True, readonly=True
        ),
        'pay_order_ids': fields.one2many('payment.order', 'exported_file_id', 'Payment Orders', readonly=False, ),
    }
    _defaults = {
        'date': lambda *a: time.strftime('%Y-%m-%d %H:%M:%S'),
        'user_id': lambda self, cursor, uid, context: uid,
    }
account_banking_exported_file()

# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
