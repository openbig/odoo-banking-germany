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
#import xmlrpclib
#import urllib
import logging
#from wizard.banktools import get_or_create_bank
#import decimal_precision as dp

# Needed to expand Iban numer to 4 character grouping. To be able to compare to ban account numbers in OpenERP db.
def _pretty_iban(iban_str):
    "return iban_str in groups of four characters separated by a single space"
    res = []
    while iban_str:
        res.append(iban_str[:4])
        iban_str = iban_str[4:]
    return ' '.join(res)

#For initialization wizard
class hibiscus_config_wizard(osv.osv_memory):
    _name = 'hibiscus.config.wizard'

    _columns = {
#        'name': fields.char('Name',size=64, required=True),
        'company_id': fields.many2one('res.company', 'Company', select=True, required=True),
        'server': fields.char('Server Name', size=64, required=True),
        'port': fields.char('Port Number', size=5, required=True),
        'user': fields.char('User Name', size=32, required=True),
        'password': fields.char('Password', size=32, required=True),
#        'active': fields.boolean('Active'),
        'secure': fields.boolean('Secure Connection'),
    }
    _defaults = {
#        'active': lambda *a: True,
        'server': lambda *a: 'localhost',
        'port': lambda *a: '8080',
        'secure' : lambda *a: True,
        'company_id': lambda self,cr,uid,c: self.pool.get('res.users').browse(cr, uid, uid, c).company_id.id,
    }
#    def action_cancel(self,cr,uid,ids,conect=None):
#        return {
#                'view_type': 'form',
#                "view_mode": 'form',
#                'res_model': 'ir.actions.configuration.wizard',
#                'type': 'ir.actions.act_window',
#                'target':'new',
#        }

    def create_bank_account(self, cr, uid, account, wizard, bank_name, account_code, account_number, state,  context):
        settings_obj = self.pool.get('bank.integration.settings')
        partner_bank_obj = self.pool.get('res.partner.bank')
        bank_obj = self.pool.get('res.bank')
        bank_name = (bank_name or _("Bank from Hibiscus"))
        bank_ids = bank_obj.search(cr, uid, [('bic','=',account_code)], context=context)
        if not len(bank_ids):
            bank_id = bank_obj.create(cr, uid, {
                'name' : bank_name,
                'bic' : account_code,
                })
        else:
            bank_id = bank_ids[0]
        search_account_number = account_number[0].isalpha() and _pretty_iban(account_number) or account_number
        partner_bank_ids = partner_bank_obj.search(cr, uid, [('acc_number','=',search_account_number)], context=context)
        if not len(partner_bank_ids):
            partner_bank_id = partner_bank_obj.create(cr, uid, {
                'acc_number': account_number,
#            'iban': account['iban'],
                'owner_name' : account['name'],
                'bank': bank_id,
                'name': _("Currency: ") + account['waehrung'] \
                    + _(" Sub Account: ") + account['unterkonto'] \
                    + _(" Description: ") + account['bezeichnung'] \
                    + _(" Customer No: ") + account['kundennummer'] \
                    + _(" Comment: ") + account['kommentar'],
                'partner_id': wizard.company_id.partner_id.id,
                'state': state,
                'bank_bic': account_code,
                'bank_name': bank_name,
                 })
        else:
            partner_bank_id = partner_bank_ids[0]
        settings_obj.create(cr, uid, {
            'name' : _("Hibiscus OnLine for ") + account_number,
            'active': True,
            'partner_bank_id' : partner_bank_id,
            'parser' : "hibi_ol",
            'hibiscus_account_id' : int(account['id']),
            'hibiscus_server' : wizard.server,
            'hibiscus_port' : wizard.port,
            'hibiscus_user' : wizard.user,
            'hibiscus_password' : wizard.password,
            'hibiscus_secure' : wizard.secure,
            'company_id' : wizard.company_id.id,
#            'journal_id': journal_id,
            })


    def action_create(self, cr, uid, ids, context=None):
        logger = logging.getLogger('HIBI_CONFIG')
        hibiscus_tools_obj = self.pool.get('hibiscus.tools')
        settings_obj = self.pool.get('bank.integration.settings')
        partner_bank_obj = self.pool.get('res.partner.bank')
        bank_obj = self.pool.get('res.bank')
        wizard = self.browse(cr, uid, ids[0], context=context)
        server = hibiscus_tools_obj.get_server(cr, uid, wizard.server, wizard.user, wizard.password, wizard.port, wizard.secure)
        try:
            accounts = getattr(server,'hibiscus.xmlrpc.konto.find')()
#            accounts = getattr(server,'hibiscus.xmlrpc.konto.list')()  # deprecated
        except Exception, e:
            raise osv.except_osv(_('Error!'),_('Cannot import Hibiscus accounts: %s')%e)
        logger.warn("ACCOUNTS: %s", accounts)
#        logger = netsvc.Logger()
#        netsvc.Logger().notifyChannel("warning", netsvc.LOG_WARNING,"accounts: %s"%accounts)
        for account in accounts:
# Fields Available in dictionary
#kundennummer
#name
#blz
#saldo_available
#unterkonto
#saldo_datum
#bic
#iban
#saldo
#kontonummer
#waehrung
#bezeichnung
#id
#kommentar
            bank_name = False
            try:
                bank_name = getattr(server,'hibiscus.xmlrpc.konto.getBankname')(account['blz'])
            except Exception, e:
                pass
            bank_name = bank_name or (account['bic'] and ("BIC: " +account['bic']) or ("BLZ: " + account['blz']))
            setting_ids = settings_obj.search(cr, uid, [('parser','=','hibi_ol'), ('hibiscus_server','=',wizard.server), \
                                            ('hibiscus_account_id','=',int(account['id'])), \
                                            ('company_id','=',wizard.company_id.id)], context=context)
            if setting_ids:
                for setting in settings_obj.browse(cr, uid, setting_ids):
                    if setting.partner_bank_id.acc_number != account['kontonummer'] and \
                        setting.partner_bank_id.acc_number.replace(" ","") != account['iban']:
                        settings_obj.unlink(cr, uid, [setting.id], context=context)
            setting_ids = settings_obj.search(cr,uid,[('parser','=','hibi_ol'),('company_id','=',wizard.company_id.id)], context=context)
            setting_exists_for_konto = False
            setting_exists_for_iban = False
#            netsvc.Logger().notifyChannel("WIZARD", netsvc.LOG_WARNING,"Wizard server: %s"%wizard.server)

            for setting in settings_obj.browse(cr, uid, setting_ids):
                if setting.partner_bank_id.acc_number == account['kontonummer'] \
                        and setting.partner_bank_id.bank.bic == account['blz']:
#                        and setting.partner_bank_id.bank.code == account['blz']:
                    settings_obj.write(cr, uid, setting.id, {
                            'hibiscus_account_id' : int(account['id']),
                            'hibiscus_server'   : wizard.server,
                            'hibiscus_port'     : wizard.port,
                            'hibiscus_user'     : wizard.user,
                            'hibiscus_password' : wizard.password,
#                            'active':active,
                            'hibiscus_secure'   : wizard.secure,
                    })
                    bank_name = bank_name or setting.partner_bank_id.bank.name
                    bank_obj.write(cr, uid, setting.partner_bank_id.bank.id, {
#                            'bic' : account['bic'],
                            'bic' : account['blz'],
                            'name' : bank_name, 
                    })
                    setting_exists_for_konto = True
                logger.warn("ACCOUNT: %s BIC: %s", setting.partner_bank_id.acc_number.replace(" ",""), setting.partner_bank_id.bank.bic)
                if account['iban'] and account['bic'] and setting.partner_bank_id.acc_number.replace(" ","") == account['iban'] \
                        and setting.partner_bank_id.bank.bic == account['bic']:
#                        and setting.partner_bank_id.bank.code == account['blz']:
                    settings_obj.write(cr, uid, setting.id, {
                            'hibiscus_account_id' : int(account['id']),
                            'hibiscus_server'   : wizard.server,
                            'hibiscus_port'     : wizard.port,
                            'hibiscus_user'     : wizard.user,
                            'hibiscus_password' : wizard.password,
#                            'active':active,
                            'hibiscus_secure'   : wizard.secure,
                    })
                    bank_name = bank_name or setting.partner_bank_id.bank.name
                    bank_obj.write(cr, uid, setting.partner_bank_id.bank.id, {
#                            'bic' : account['bic'],
                            'bic' : account['bic'],
                            'name' : bank_name, 
                    })
                    setting_exists_for_iban = True
            if not setting_exists_for_konto:
                self.create_bank_account(cr, uid, account, wizard, bank_name, \
                        account_code=account['blz'], account_number=account['kontonummer'], state="bank", context=context)
            if not setting_exists_for_iban and account['iban'] and account['bic']:
                self.create_bank_account(cr, uid, account, wizard, bank_name, \
                        account_code=account['bic'], account_number=account['iban'], state="iban", context=context)
                
        return {}
hibiscus_config_wizard()
# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
