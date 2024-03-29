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
#import netsvc
import logging
from operator import itemgetter
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
(bank_bic
    or
bank_code)

ref    - ref and note is used for searching for invoice number in some configuration option.
note *  Put whole line here
sequence
}
'''

# Hibiscus doesn't allow to enter to text fields some characters. This method clean up text before export. 
#It is no warning about not exported caracters.
def str_clear(str):
    if not str:
        return ''
    res=""
    for c in str:
        if c.isalnum() or c in ["$","%","&","*","+",",",".","/","-"," ",]:
            res += c
    return res

# Needed to expand Iban numer to 4 character grouping. To be able to compare to ban account numbers in OpenERP db.
def _pretty_iban(iban_str):
    "return iban_str in groups of four characters separated by a single space"
    res = []
    while iban_str:
        res.append(iban_str[:4])
        iban_str = iban_str[4:]
    return ' '.join(res)

class bank_parsers(osv.osv_memory):
    _inherit = "bank.parsers"
    
    def parser_selection(self, cr, uid, context=None):
        res = super(bank_parsers, self).parser_selection(cr, uid, context=context)
#        res.append(('hibi_csv','Hibiscus CSV Parser'))   # selection must be exactly the same as method name.
        res.append(('hibi_ol','Hibiscus OnLine Parser'))   # selection must be exactly the same as method name.
        return res   # [('hibiscus_csv_parser','Hibiscus CSV Parser'),]
#        return res += [ ('hibiscus_csv_parser','Hibiscus CSV Parser'),]

    #checking for new or modified bank mapping details
    def hibi_ol_import(self, cr, uid, wizard, context=None):
        logger = logging.getLogger('HIBI')
        config = wizard.banking_settings_id
        if not config.hibiscus_account_id:
             raise osv.except_osv('Error!','You have to set Hibiscus Account ID in Import Settings using Import Hbiscus Accounts Wizard.')
        hibiscus_tools_obj = self.pool.get('hibiscus.tools')
        server = hibiscus_tools_obj.get_server(cr, uid, config.hibiscus_server, config.hibiscus_user, config.hibiscus_password, \
                            config.hibiscus_port, config.hibiscus_secure)
        hibiscus_account_id = config.hibiscus_account_id

        try:
            accounts = getattr(server,'hibiscus.xmlrpc.konto.find')()
        except Exception, e:
            raise osv.except_osv(_('Error!'),_('Cannot import Hibiscus account (to check currency): %s')%e)
        currency = "EUR"
#        logger.warn("ACCOUNTS: %s", accounts)
        for account in accounts:
            if int(account['id']) == hibiscus_account_id:
#                logger.warn("CURRENCY EQUAL ID: %s, ACCOUNT WAEHRUNG %s"% (currency,account['waehrung']))
                if len(account['waehrung']):
                    currency = account['waehrung']
                break

        date_from = wizard.date_from and datetime.strptime(wizard.date_from, "%Y-%m-%d").strftime("%d.%m.%Y") or ""
        date_to = wizard.date_to and datetime.strptime(wizard.date_to, "%Y-%m-%d").strftime("%d.%m.%Y") or ""

        args = {"konto_id":hibiscus_account_id}
        if date_from:
            args["datum:min"] = date_from  
        if date_to:
            args["datum:max"] = date_to 
        arg = (args,)
        try:
            file_data = getattr(server,'hibiscus.xmlrpc.umsatz.list')(*arg)
        except Exception, e:
            raise osv.except_osv(_('Error!'),_('Cannot import Hibiscus bank statement: %s')%e)
                #self.report.append('ERROR: Unable to update record ['+str(id2)+']:'+str(value.get('name', '?')))
#        logger = netsvc.Logger()
#        logger.notifyChannel("warning", netsvc.LOG_WARNING,"File data %s"%str(file_data))
        logger.warn("DATA FROM HIBI: %s", file_data)
        if not file_data:
            return False
        lines = file_data  #.split('\n')
#        lines = file_data.split('\n')
        if not len(lines):
            return False
        lines = sorted(lines, key=itemgetter('id'))
#        quotechar = '"'
        delimiter = ':'
        first_line = lines[0]  #.replace(quotechar,'')

#  1      map.put("id", u.getID());

#  0      map.put("konto_id", u.getKonto().getID());
#  1      map.put("empfaenger_name", StringUtil.notNull(u.getGegenkontoName()));
#  2      map.put("empfaenger_konto", StringUtil.notNull(u.getGegenkontoNummer()));
#  3      map.put("empfaenger_blz", StringUtil.notNull(u.getGegenkontoBLZ()));
#  4      map.put("art", StringUtil.notNull(u.getArt()));
#  5      map.put("betrag", HBCI.DECIMALFORMAT.format(u.getBetrag()));
#  6      map.put("valuta", DateUtil.format(u.getValuta()));
#  7      map.put("datum", DateUtil.format(u.getDatum()));
#  8      map.put("zweck", VerwendungszweckUtil.toString(u, " "));
#  9      map.put("saldo", StringUtil.notNull(Double.valueOf(u.getSaldo())));
# 10     map.put("primanota", StringUtil.notNull(u.getPrimanota()));
# 11      map.put("customer_ref", StringUtil.notNull(u.getCustomerRef()));
# 12      map.put("kommentar", StringUtil.notNull(u.getKommentar()));
#      if (kat != null) {
# 13        map.put("umsatz_typ", kat.getName());

#        first_line_data = first_line.split(delimiter)
        balance_start = float(first_line['saldo'])  #.replace(".","").replace(",","."))  # zwischensumme - balance after first line
        balance_start -=  float(first_line['betrag'].replace(".","").replace(",","."))   #  amount of first line

        result = {}
# TODO name ????
        result['name'] = '/'  #  (system will check if statement name is unique).
        result['date'] =  time.strftime('%Y-%m-%d')
        result['balance_start'] = balance_start
        result['balance_end_real'] = False   # Will be entered after last loop
        result['currency'] = currency #'EUR'    # to check if the same as in bank statement journal (which is taken from defaults)
#        result['home_bank_account_iban'] = False  # wizard.banking_settings_id.partner_account_id.iban
        result['home_bank_account'] = wizard.banking_settings_id.partner_bank_id.acc_number
        result['home_bank_swift'] = False
        result['home_bank_code'] = wizard.banking_settings_id.partner_bank_id.bank.bic
        result['lines']=[]
        seq = 1
        last_balance = 0
        for line in lines:
            if not len(line):
                break
#            data = line.split(delimiter)
#            for idx, value in enumerate(data):
#                data[idx] = data[idx].replace(quotechar,'')                
            val={}
# TODO How to recognize the IBAN ??
# TODO How to recognize the bank cost operations ??
            val['name'] = line['primanota']  # (communication - primanota
            val['date'] = line['datum']    # datetime.strptime(data[7], "%d.%m.%Y").strftime("%Y-%m-%d") 
            val['amount'] = float(line['betrag'].replace(".","").replace(",","."))  #data[5])    #.replace(".","").replace(",","."))
            val['type'] = 'general'  # 'customer' or 'supplier', 'bank' Bank has to be converted to General after calculation.
            val['partner_name'] = line['empfaenger_name']  #data[1]
#            val['partner_address_name'] = False
            val['partner_street'] = False
            val['partner_street2'] = False
            val['partner_zip'] = False
            val['partner_city'] = False
            val['partner_country'] = False
#            val['partner_bank_account_iban'] = False  # No spaces inside !!
# account_number[0].isalpha() and _pretty_iban(account_number) or account_number
            bank_account = line['empfaenger_konto']
            val['partner_bank_account'] = bank_account[0].isalpha() and _pretty_iban(bank_account) or bank_account  #data[2]
            val['partner_bank_swift'] = False
            val['partner_bank_code'] = line['empfaenger_blz']    # data[3]
            val['ref'] = line['customer_ref']   # ustr(data[11])
            val['sequence'] = seq
            val['note'] =  ustr( "Primanota: :" + line['primanota']  \
                          +"\nKonto ID: " + line['id']  \
                          +"\nEmpfanger: " + line['empfaenger_name']  \
                          +"\nEmpfanger konto: " + line['empfaenger_konto']  \
                          +"\nEmpfanger konto BLZ: " + line['empfaenger_blz']  \
                          +"\nArt: " +  line['art']  \
                          +"\nDatum: "+ line['datum'] \
                          +"\nBetrag: "+ line['betrag'] \
                          +"\nSaldo: " +  line['saldo']  \
                          +"\nValuta: " +  line['valuta']  \
                          +"\nReferenz: " +  line['customer_ref']  \
                          +"\nUmsatz typ: " +  (('umsatz_typ' in line) and line['umsatz_typ'] or "")  \
                          +"\nZweck: " +  line['zweck']  \
                          +"\nKommentar: " + line['kommentar']
                    )
#                          "\nZweck 2: " +  data[10] + \
#                          "\nWeitere Zweck: " + data[16]

            last_balance = line['saldo']   # data[9]
            seq += 1
            result['lines'].append(val)
        result['balance_end_real'] = float(last_balance)    #.replace(".","").replace(",","."))
        return [result]

    def hibi_ol_export(self, cr, uid, p_order_id, context=None):
        logger = logging.getLogger('HIBI')
        if not p_order_id:
            return False
        result = False
        pay_order = self.pool.get('payment.order').browse(cr, uid, p_order_id, context)
        config = pay_order.mode.banking_settings_id
        hibiscus_tools_obj = self.pool.get('hibiscus.tools')
        server = hibiscus_tools_obj.get_server(cr, uid, config.hibiscus_server, config.hibiscus_user, config.hibiscus_password, \
                            config.hibiscus_port, config.hibiscus_secure)
        hibiscus_account_id = config.hibiscus_account_id
# TODO Take a currency from Hibiscus account
        try:
            accounts = getattr(server,'hibiscus.xmlrpc.konto.find')()
        except Exception, e:
            raise osv.except_osv(_('Error!'),_('Cannot import Hibiscus account (to check currency): %s')%e)
        currency = "EUR"
        logger.warn("ACCOUNTS: %s", accounts)
        for account in accounts:
            if int(account['id']) == hibiscus_account_id:
                logger.warn("CURRENCY EQUAL ID: %s, ACCOUNT WAEHRUNG %s"% (currency,account['waehrung']))
                if len(account['waehrung']):
                    currency = account['waehrung']
                break
        logger.warn("CURRENCY: %s", currency)
        data = ""
        fixed_date = pay_order.date_prefered == "fixed" and pay_order.date_scheduled
        for line in pay_order.line_ids:
# TODO Compare currency to hibiscus currency
            if line.currency.name != currency:
                raise osv.except_osv(_('Error!'),_("Line '%s' has currency: '%s' but Hibiscus account: %s has different currency '%s'") \
                        %(line.name, line.currency.name, (account['kontonummer'] or account['iban']), currency ))
            date = fixed_date or pay_order.date_prefered == "due" and line.ml_maturity_date or line.date
            date = date and datetime.strptime(date, "%Y-%m-%d").strftime("%d.%m.%Y") 
#            kontonummer = line.bank_id.state == 'iban' and line.bank_id.iban or line.bank_id.acc_number
            amount = line.amount_currency
            if amount < 0:
                testschluessel = "05"
            else:
                testschluessel = "51"
            params = { "konto" : hibiscus_account_id,        # kontoID
                    "name" : line.partner_id.name,       # name
                    "betrag" : line.amount_currency + 0.0,   #.replace(".",","),   # betrag
                    "termin" : date,   # termin
                    "blz" : line.bank_id.bank.bic,
#                    "verwendungszweck" : verwendungszweck,
            }
            if line.bank_id.state == "bank":
                params["kontonummer"] = line.bank_id.acc_number   #kontonummer,                      # kto
#                params["blz"] = line.bank_id.bank.bic     # bic as blz   TODO what about iban !!!!!
                params["textschluessel"] = testschluessel
                verwendungszweck = [
                                    "Your ref " + (line.ml_inv_ref and line.ml_inv_ref.supplier_invoice_number and \
                                        str_clear(line.ml_inv_ref.supplier_invoice_number) or "NONREF"),   # zweck1
                                    line.ml_inv_ref and line.ml_inv_ref.reference and str_clear(line.ml_inv_ref.reference) or "NONREF",          # zweck2
                                ]
                if line.communication:   # zweck3
                    verwendungszweck.append(str_clear(line.communication))
                if line.communication2:   # zweck4
                    verwendungszweck.append(str_clear(line.communication2))
                params['verwendungszweck'] = verwendungszweck
            if line.bank_id.state == "iban":
                params["kontonummer"] = line.bank_id.acc_number.replace(" ","")
                # for iban all descriptions in first verwendungszweck item.
                verwendungszweck = [
                        "Your ref " + (line.ml_inv_ref and line.ml_inv_ref.supplier_invoice_number and \
                                str_clear(line.ml_inv_ref.supplier_invoice_number) or "NONREF") +   # zweck1
                        (line.ml_inv_ref and line.ml_inv_ref.reference and (" " + str_clear(line.ml_inv_ref.reference)) or "") +  # zweck2
                        (line.communication and (" " + str_clear(line.communication)) or "") + # zweck 3
                        (line.communication2 and (" " + str_clear(line.communication2)) or "") #  zweck 4 
                ]
                params['verwendungszweck'] = verwendungszweck 

#                params["bic"] = line.bank_id.bank.bic     # bic as blz   TODO what about iban !!!!!
            arg = (params,)

            logger.warn("PARAMS: %s", params)
            kind_of_transfer = _("transfer")
            if line.bank_id.state == "iban":
                kind_of_transfer = _("SEPA transfer")
                try:
                    result = getattr(server,'hibiscus.xmlrpc.sepaueberweisung.create')(*arg)
                except Exception, e:
                    raise osv.except_osv(_('Error!'),_(('Cannot create SEPA transfer for "%s" because:\n%s')%(line.name,e,)))
            elif amount >= 0:
                try:
                    result = getattr(server,'hibiscus.xmlrpc.ueberweisung.create')(*arg)
                except Exception, e:
                    raise osv.except_osv(_('Error!'),_(('Cannot create transfer for "%s" because:\n%s')%(line.name,e,)))
            else:
                kind_of_transfer = _("direct debit")
                try:
                    result = getattr(server,'hibiscus.xmlrpc.lastschrift.create')(*arg)
#                result = getattr(server,'hibiscus.xmlrpc.ueberweisung.createParams')
                except Exception, e:
                    raise osv.except_osv(_('Error!'),_(('Cannot create direct debit for "%s" because:\n%s')%(line.name,e,)))
            logger.warn("Transfer Result: %s", result)
            if result:
                try:
                    id = int(result)
                except Exception, e:
                    raise osv.except_osv(_('Error!'),_('Cannot create %s for "%s" because:\n%s')%(kind_of_transfer, line.name,result,))
#            if result:
#                raise osv.except_osv(_('Error!'),_(result.encode('UTF-8')))
            data += ustr(arg)
        return data
bank_parsers()

# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
