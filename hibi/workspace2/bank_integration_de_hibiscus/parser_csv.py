# -*- encoding: utf-8 -*-
##############################################################################
#
#  Copyright (C) 2012 OpenGLOBE (<http://www.openglobe.pl>).
#  All Rights Reserved
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
##############################################################################

from tools.translate import _
from osv import osv, fields
import time
from datetime import datetime
from tools import ustr   #gg change
#import csv
import base64

'''
FOR IMPORT 

Inherit this class and add parser as a method. 
 The names of the methods will be selection for wizards.
 You have to return a list of following data:
result = {

 For Bank Statement:
name  (system will check if statement name is unique).
date
balance_start     - can be filled or left for manual entry
balance_end_real  - can be filled or left for manual entry
currency

For bank Statement lines:

name (communication) *
date *
amount *
type
partner_name *   If address cannot be resolved put everything to partner_name
partner_address_name, 
partner_street, If full address cannot be resolved put everything in street
partner_street2, 
partner_zip, 
partner_city, 
partner_country

bank_account_iban
    * or
bank_account
    and
bank_bic
    or
bank_code

ref    - ref and note is used for searching for invoice number in some configuration option.
note *  Put whole line here
sequence
}
'''

'''

TO EXPORT
        'date_scheduled': fields.date('Scheduled date if fixed', states={'done':[('readonly', True)]}, help='Select a date if you have chosen Preferred Date to be fixed.'),
        'reference': fields.char('Reference', size=128, required=1, states={'done': [('readonly', True)]}),
        'mode': fields.many2one('payment.mode', 'Payment mode', select=True, required=1, states={'done': [('readonly', True)]}, help='Select the Payment Mode to be applied.'),
        'state': fields.selection([('draft', 'Draft'), ('open', 'Confirmed'),  ('cancel', 'Cancelled'),    ('done', 'Done')], 'State', select=True,
            help='When an order is placed the state is \'Draft\'.\n Once the bank is confirmed the state is set to \'Confirmed\'.\n Then the order is paid the state is \'Done\'.'),
        'line_ids': fields.one2many('payment.line', 'order_id', 'Payment lines', states={'done': [('readonly', True)]}),
        'total': fields.function(_total, string="Total", method=True, type='float'),
        'user_id': fields.many2one('res.users', 'User', required=True, states={'done': [('readonly', True)]}),
        'date_prefered': fields.selection([('now', 'Directly'), ('due', 'Due date'),  ('fixed', 'Fixed date')
            ], "Preferred date", change_default=True, required=True, states={'done': [('readonly', True)]}, help="Choose an option for the Payment Order:'Fixed' stands for a date specified by you.'Directly' stands for the direct execution.'Due date' stands for the scheduled date of execution."),
        'date_created': fields.date('Creation date', readonly=True),
        'date_done': fields.date('Execution date', readonly=True),

LINES
        'name': fields.char('Your Reference', size=64, required=True),
        'communication': fields.char('Communication', size=64, required=True, help="Used as the message between ordering customer and current company. Depicts 'What do you want to say to the recipient about this order ?'"),
        'communication2': fields.char('Communication 2', size=64, help='The successor message of Communication.'),
        'move_line_id': fields.many2one('account.move.line', 'Entry line', domain=[('reconcile_id', '=', False), ('account_id.type', '=', 'payable')], help='This Entry Line will be referred for the information of the ordering customer.'),
        'amount_currency': fields.float('Amount in Partner Currency', digits=(16, 2),       required=True, help='Payment amount in the partner currency'),
        'currency': fields.many2one('res.currency','Partner Currency', required=True),
        'company_currency': fields.many2one('res.currency', 'Company Currency', readonly=True),
        'bank_id': fields.many2one('res.partner.bank', 'Destination Bank account'),
        'order_id': fields.many2one('payment.order', 'Order', required=True,      ondelete='cascade', select=True),
        'partner_id': fields.many2one('res.partner', string="Partner", required=True, help='The Ordering Customer'),
        'amount': fields.function(_amount, string='Amount in Company Currency',  method=True, type='float',  help='Payment amount in the company currency'),
        'ml_date_created': fields.function(_get_ml_created_date, string="Effective Date",     method=True, type='date', help="Invoice Effective Date"),
        'ml_maturity_date': fields.function(_get_ml_maturity_date, method=True, type='date', string='Due Date'),
        'ml_inv_ref': fields.function(_get_ml_inv_ref, method=True, type='many2one', relation='account.invoice', string='Invoice Ref.'),
        'info_owner': fields.function(info_owner, string="Owner Account", method=True, type="text", help='Address of the Main Partner'),
        'info_partner': fields.function(info_partner, string="Destination Account", method=True, type="text", help='Address of the Ordering Customer.'),
        'date': fields.date('Payment Date', help="If no payment date is specified, the bank will treat this payment line directly"),
        'create_date': fields.datetime('Created', readonly=True),
        'state': fields.selection([('normal','Free'), ('structured','Structured')], 'Communication Type', required=True),
        'bank_statement_line_id': fields.many2one('account.bank.statement.line', 'Bank statement line')
'''
# Hibiscus data structure for import and export
#  0 Kontonummer;  - Our
#  1 BLZ;   - Our
#  2 Konto; - Our bank account name in Hibiscus
#  3 Gegenkonto; Partner account  , What about IBAN ?
#  4 Gegenkonto BLZ;  Partner bank account code
#  5 Gegenkonto Inhaber; Partner account owner
#  6 Betrag;   Amount
#  7 Valuta;   Currency date
#  8 Datum;   Date
#  9 Verwendungszweck; payment reference
# 10 Verwendungszweck 2;
# 11 Zwischensumme;
# 12 Primanota;
# 13 Kundenreferenz;  Customer ref
# 14 Kategorie;  umsatz typ
# 15 Kommentar;
# 16 Weitere Verwendungszwecke

class bank_parsers(osv.osv_memory):
    _inherit = "bank.parsers"
    
    def parser_selection(self, cr, uid, context=None):
        res = super(bank_parsers, self).parser_selection(cr, uid, context=context)
        res.append(('hibi_csv','Hibiscus CSV Parser'))   # selection must be exactly the same as method name.
#        res.append(('hibi_ol','Hibiscus OnLine Parser'))   # selection must be exactly the same as method name.
        return res   # [('hibiscus_csv_parser','Hibiscus CSV Parser'),]
#        return res += [ ('hibiscus_csv_parser','Hibiscus CSV Parser'),] 

# Export Parser for Hibiscus. Will be identified as 'hibi_csv' from selection and + '_export'
    def hibi_csv_export(self, cr, uid, p_order_id, context=None):
        if not p_order_id:
            return False
        quotechar = u'\"'
        delimiter = u';'
        pay_order = self.pool.get('payment.order').browse(cr, uid, p_order_id, context)
# Hard data:
        our_konto = pay_order.mode.bank_id.state == 'iban' and pay_order.mode.bank_id.iban or pay_order.mode.bank_id.acc_number
        our_blz =  pay_order.mode.bank_id.bank.bic
        our_name = pay_order.mode.company_id.partner_id.name

        data = "Eigene Kontonummer;Eigene BLZ;Gegenkonto;Gegenkonto BLZ;Gegenkonto Inhaber;Betrag;Termin;Verwendungszweck;" \
                "Verwendungszweck 2;Weitere Verwendungszwecke\n"
        fixed_date = pay_order.date_prefered == "fixed" and pay_order.date_scheduled
        for line in pay_order.line_ids:
            date = fixed_date or pay_order.date_prefered == "due" and line.ml_maturity_date or line.date
            date = date and datetime.strptime(date, "%Y-%m-%d").strftime("%d.%m.%Y") 
            data += quotechar + ustr(our_konto) + quotechar + delimiter
            data += quotechar + our_blz + quotechar + delimiter
            data += quotechar + (line.bank_id.state == 'bank_iban' and line.bank_id.iban or line.bank_id.acc_number) + quotechar + delimiter
            data += quotechar + line.bank_id.bank.bic + quotechar + delimiter
            data += quotechar + line.partner_id.name + quotechar + delimiter
            data += quotechar + ustr(line.amount_currency).replace(".",",") + quotechar + delimiter
            data += quotechar + date + quotechar + delimiter   # Valuta Termin
            data += quotechar + "Your ref " + (line.ml_inv_ref and line.ml_inv_ref.reference or "NONREF") + quotechar + delimiter            #Verwendungszweck;
            data += quotechar + "Our ref " + (line.name or "NONREF") + quotechar + delimiter            #Verwendungszweck 2;
            data += quotechar + line.communication + (line.communication2 and (" - " + line.communication2) or "") + quotechar            #Weitere Verwendungszwecke
            data += "\n"
        return data

# Import Parser for Hibiscus. Will be identified as 'hibi_csv' from selection and + '_import'
    def hibi_csv_import(self, cr, uid, import_wizard, context=None):
        if not import_wizard.file:
            raise osv.except_osv( _('ERROR!'),
                _("You have to select a file for settings: %(settings)s with file format %(format)s.") \
                                % {'settings': settings.name, 'format': settings.parser})
#            logger = netsvc.Logger()
#            netsvc.Logger().notifyChannel("warning", netsvc.LOG_WARNING,"The line: %s"%line)
        statements_file = import_wizard.file
        file_data = base64.decodestring(statements_file)
        lines = file_data.split('\n')
        if not len(lines[1]):
            return False
        quotechar = '"'
        delimiter = ';'
        first_line = lines[1].replace(quotechar,'')
        first_line_data = first_line.split(delimiter)
        balance_start = float(first_line_data[11].replace(".","").replace(",","."))  # zwischensumme - balance after first line
        balance_start -=  float(first_line_data[6].replace(".","").replace(",","."))   #  amount of first line

        result = {}
# TODO name ????
        result['name'] = '/'  #  (system will check if statement name is unique).
        result['date'] =  time.strftime('%Y-%m-%d')
        result['balance_start'] = balance_start
        result['balance_end_real'] = False   # Will be entered after last loop
        result['currency'] = False #'EUR'    # to check if the same as in bank statement journal (which is taken from defaults)
        result['home_bank_account_iban'] = False
        result['home_bank_account'] = first_line_data[0]
        result['home_bank_swift'] = False
        result['home_bank_code'] = first_line_data[1]

        result['lines'] = []
        seq = 0
        last_balance = 0
        for line in lines:
            if seq == 0:
                seq += 1
                continue
            if not len(line):
                break
            data = line.split(delimiter)
#            data =[]
            for idx, value in enumerate(data):
                data[idx] = data[idx].replace(quotechar,'')                
            val={}
# TODO How to recognize the IBAN ??
# TODO How to recognize the bank cost operations ??
            val['name'] = data[12]  # (communication)
            val['date'] = datetime.strptime(data[8], "%d.%m.%Y").strftime("%Y-%m-%d") 
            val['amount'] = float(data[6].replace(".","").replace(",","."))
            val['type'] = 'general'  # 'customer' or 'supplier', 'bank' Bank has to be converted to General after calculation.
            val['partner_name'] = data[5]
#            val['partner_address_name'] = False
            val['partner_street'] = False
            val['partner_street2'] = False
            val['partner_zip'] = False
            val['partner_city'] = False
            val['partner_country'] = False
            val['partner_bank_account_iban'] = False  # No spaces inside !!
            val['partner_bank_account'] = data[3]
            val['partner_bank_swift'] = False
            val['partner_bank_code'] = data[4]
            val['ref'] = ustr(data[13])
            val['sequence'] = seq
            val['note'] =  ustr( "Primanota: " + data[12] + \
                          "\nKonto: " + data[0] + \
                          "\nEmpfanger: " + data[2] + \
                          "\nEmpfanger konto: " + data[3] + \
                          "\nEmpfanger konto BLZ: " + data[4] + \
                          "\nSaldo: " +  data[11] + \
                          "\nValuta: " +  data[7] + \
                          "\nReferenz: " +  data[13] + \
                          "\nUmsatz typ: " +  data[14] + \
                          "\nZweck: " +  data[9] + \
                          "\nZweck 2: " +  data[10] + \
                          "\nKommentar: " + data[15] + \
                          "\nWeitere Zweck: " + data[16]
                    )

            last_balance = data[11]
            seq += 1
            result['lines'].append(val)
        result['balance_end_real'] = float(last_balance.replace(".","").replace(",","."))
        return [result]

bank_parsers()

# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
