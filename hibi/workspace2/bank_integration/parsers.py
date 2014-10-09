# -*- encoding: utf-8 -*-
##############################################################################
#
#  Copyright (C) 2009 EduSense BV (<http://www.edusense.nl>).
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

'''
 Inherit this class and add parser as a method. 
 The names of the methods will be selection for wizards.
 You have to return a list of following data:
result = {

 For Bank Statement:
name  (system will check if statement name is unique).
date
balance_start
balance_end_real
currency

For bank Statement lines:

name (communication) *
date *
amount *
type
partner_name *   If address cannot be resolved put everything to partner_name
##### partner_address_name,  Not valid !!!
partner_street, If full address cannot be resolved put everything in street
partner_street2, 
partner_zip, 
partner_city, 
partner_country

bank_account_iban
    * or
bank_account
bank_bic

ref
note *  
sequence
'''

class bank_parsers(osv.osv_memory):
    _name = "bank.parsers"
    
    def parser_selection(self, cr, uid, context=None):
        return []  # append this variable in your module
#        return [ ('demo','DEMO Parser'),]  # append this variable in your module

#  Example of inheriting:
#    def parser_selection(self, cr, uid, context=None):
#        res = super(bank_parsers, self).parser_selection(cr, uid, context=context)
#        res.append(('hibiscus_csv','Hibiscus CSV Parser'))
#        return res
#
#    def hibiscus_csv_import(self, cr, uid, data, context=None):
#    .
#    .    see demo parser below
#    .
#    return res

#    def hibiscus_csv_export(self, cr, uid, data, context=None):
#    .
#    .    see demo parser below
#    .
#    return res

    def demo_export(self, cr, uid, p_order_id, context=None):
        if not p_order_id:
            return False
        pay_order = self.pool.get('payment.order').browse(cr, uid, p_order_id, context)

        return 'aaaa'


    def demo_import(self, cr, uid, data, context=None):

        result = {}
        result['name'] = 'BS002'  #  (system will check if statement name is unique).
        result['date'] =  time.strftime('%Y-%m-%d')
        result['balance_start'] = 324324.00
        result['balance_end_real'] = 334455.00
        result['currency'] = 'EUR'    # to check if the same as in bank statement journal (which is taken from defaults)
#        result['home_bank_account_iban'] = 'PL97 1140 2004 0000 3102 7097 6119'
        result['home_bank_account'] = 'PL97 1140 2004 0000 3102 7097 6119'
        result['home_bank_swift'] = ''
        result['home_bank_code'] = ''

 
        result['lines'] = []
        result['lines'].append({})
#        result['lines'][0] = {}

        result['lines'][0]['name'] = 'Op1'  # (communication)
        result['lines'][0]['date'] = time.strftime('%Y-%m-%d')
        result['lines'][0]['amount'] = 22
        result['lines'][0]['type'] = 'general'  # 'customer' or 'supplier', 'bank' Bank has to be converted to General after calculation.
        result['lines'][0]['partner_name'] = 'Molit'
#        result['lines'][0]['partner_address_name'] = 'adddddd'
        result['lines'][0]['partner_street'] = 'str 1'
        result['lines'][0]['partner_street2'] = 'str 2'
        result['lines'][0]['partner_zip'] = '4444'
        result['lines'][0]['partner_city'] = 'Och City'
        result['lines'][0]['partner_country'] = 'PL'
#        result['lines'][0]['partner_bank_account_iban'] = ''  # No spaces inside !!
        result['lines'][0]['partner_bank_account'] = '7243232424'
        result['lines'][0]['partner_bank_swift'] = ''
        result['lines'][0]['partner_bank_code'] = '73423442'
        result['lines'][0]['ref'] = "ggggiuoiui"
        result['lines'][0]['note'] = "gggggfsfsd"
        result['lines'][0]['sequence'] = 1

        result['lines'].append({})
        result['lines'][1]['name'] = 'OP2'  # (communication)
        result['lines'][1]['date'] = time.strftime('%Y-%m-%d')
        result['lines'][1]['amount'] = -224.88
        result['lines'][1]['type'] = 'general'  # 'customer' or 'supplier', 'bank' Bank has to be converted to General after calculation.
        result['lines'][1]['partner_name'] = 'WARAT'
#        result['lines'][1]['partner_address_name'] = 'Warat'
        result['lines'][1]['partner_street'] = 'str 1'
        result['lines'][1]['partner_street2'] = 'str 2'
        result['lines'][1]['partner_zip'] = '4444'
        result['lines'][1]['partner_city'] = 'Och City'
        result['lines'][1]['partner_country'] = 'PL'
#        result['lines'][1]['partner_bank_account_iban'] = 'PL69114020040000350273445562'
        result['lines'][1]['partner_bank_account'] = 'PL69 1140 2004 0000 3502 7344 5562'
        result['lines'][1]['partner_bank_swift'] = ''
        result['lines'][1]['partner_bank_code'] = ''
        result['lines'][1]['ref'] = "vvvvvv"
        result['lines'][1]['note'] = "vvvvv"
        result['lines'][1]['sequence'] = 2

        res = [result] # res must be list of statements
        return res

bank_parsers()
# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
