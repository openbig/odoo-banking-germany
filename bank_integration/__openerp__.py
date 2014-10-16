##############################################################################
#
#    Copyright (C) 2012 OpenGLOBE (<http://www.openglobe.pl>).
#    All Rights Reserved
#    Copyright (C) 2009 EduSense BV (<http://www.edusense.nl>).
#    All Rights Reserved
#
#    WARNING: This program as such is intended to be used by professional
#    programmers who take the whole responsability of assessing all potential
#    consequences resulting from its eventual inadequacies and bugs
#    End users who are looking for a ready-to-use solution with commercial
#    garantees and support are strongly adviced to contract EduSense BV
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
{
    'name': 'Bank Integration',
    'version': '1.11 (7.0)',
    'license': 'GPL-3',
    'author': 'Grzegorz Grzelak - OpenBIG.org (based on EduSense and Therp)',
    'website': 'http://www.openbig.org',
    'category': 'Account Banking',
    'depends': ['base', 'account', 'base_iban', 'account_payment','account_voucher'],
    'init_xml': [],
    'update_xml': [
        'security/ir.model.access.csv',
#        'data/account_banking_data.xml',
        'wizard/bank_import_view.xml',
        'wizard/account_payment_pay_view.xml',
        'account_payment_view.xml',
        'account_bank_statement_view.xml',
        'bank_integration_view.xml',
#        'account_banking_workflow.xml',
    ],
    'demo_xml': [],
    'description': '''
Basic platform for banking operations. 
======================================

Based on Edusense and Therp work but simplified.
    
This module as much as possible doesn't break certified functionality. Fe. invoice reconciliation is made exactly the same way as in certified modules (by journal vouchers). Thera are almost none additional fields for original objects. 

The goal of rewriting Therp and Edusense framework was to create very clear platform. It assumes that for very specific needs you have to create extension modules or include this extension in parser module. The module doesn't change or extend any of original functionality of bank statement or payment order. It only import data creating records in their original shape, and export data not touching original object if you not wish it.

Module is trying to be independent of account_payment_extension and account_discount_payment_extension. But real result must be observed.

For this platform you have to create or use extension modules as parsers. Parser can contain import or export or import/export method designed for specific bank file format or specific online communication. You can also create some other special extension modules not related to specific bank but related to your country or something like that.

Parser will appear after installation as a selection choice in bank integration configuration settings. In this settings you can set accounts for imported bank statements and set the way import should match the transactions to Partner or to invoice. By now there is no options for export. Settings form reserves the place for specific parser settings too.

To import bank statement from file you have to run the wizard in bank statement object (form or tree) or run it from menu Bank and Cash.

To export payment order you have to select a payment method which conatains bank integration settings and use usual button Make Payment (it makes a conflict with account_payment_extension and should be removed) or additional export wizard (it makes no confict).

IMPORT SETTINGS:    
================
Partner Matching options:
-------------------------
1. No matching - partner is not recognized in bank statement at all. 

    Matching of partner must be done manually.

2. Match Only By Bank Account.

    The most reliable way. If we have bank account assigned to partner in OpenERP and the same bank account is in bank statement import file we can be sure about partner assignment. Farther options does this matching too but they do more if they are not successful.

3. Partner Matching by Name

    Platform tries to find the partner by name. If 2. is not successful it compares the name of partner achieved in import file to Partner name in OpenERP. It is rather problematic to have the partner names exactly the same in imported file and in OpenERP but maybe.... And if this matching is successful the platform assigns the bank account to partner in OpenERP so next time assignment is perfect because it is made by option 2.

4. Partner Matching by Invoice Number

    If 2. is unsuccessful platform reviews all open invoices and look for their numbers in any description from bank statement. If platform finds it it assigns the transaction to this partner and it creates bank account for him if needed. Invoice matching depends of farher option. 

5. Create Partner When No Match.

    If 2. is not successful platform creates new partner with given bank account. New Partner is marked with (?) before name so user will know that it needs fixing. This option can be used when Company expects incoming payments from unknown partners.

    Usual situation is that payment is from customer invoice and we already have this partner in OpenERP. And we usually don't create a bank account for such partners because it is not common to ask him for bank account when we expect incoming payment. In such case option 2. will not work. Option 3. usually will not work as names are usually written in different way or even not separated of addresses in import files. Option 5. will duplicate the partner (not good). So we created option 6.

6. Match To Unknown Partner.

    If 2. and 3. is not successful the platform creates only bank account (which is always perfectly known from import file) and assigns it to one partner with name "Unknown". All unknown bank account are assigned to one "Unknown" partner. This easy way we collect bank accounts from import file to OpenERP. Then user has to review bank accounts. And he should manually reassign all partner bank accounts assigned to "Unknown" partner to proper partners according to text description. Next best step is to remove imported bank statement and import it again. Now all partners should be matched by bank account.

Invoice Matching options:
-------------------------    
1. No Matching

    Platform will not try to much invoices to transaction. So it would be no matching at all or Partner matching only. 

2. Match One Invoice by Description or Amount

    Platform will try to find the invoice by description. If partner matching is set to "Partner Matching by Invoice Number" it can also create the partner. If partner matching is set to other option searching by description will work only when partner is known. If matching the invoice by description fails the platform will try to match the invoice by amount. It works only when the partner is known. Amount difference can be set in configuration (Set there fe. 3% of amount. So it will catch amounts even with some currency rate difference or cash discount). 

3. Match All Invoices by Description or Within Amount

    (not implemented yet) Intention is to collect all invoices mentioned in description. Or if it is no invoices mentioned in description try to match open invoices whose sum is within or close to transaction amount. Hard to implement as Refunds should be taken into account as well.
    ''',
    'active': False,
    'installable': True,
}
# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
