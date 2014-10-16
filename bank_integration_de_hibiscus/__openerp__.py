##############################################################################
#
#    Copyright (C) 2012 OpenGLOBE (<http://www.openglobe.pl>).
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
    'name': 'Account Bank Integration - Hibiscus Parser',
    'version': '0.02 (7.0)',
    'license': 'GPL-3',
    'author': 'OpenGLOBE',
    'website': 'http://www.openglobe.pl',
    'category': 'Account Banking',
    'depends': ['base', 'account', 'base_iban', 'account_payment', 'bank_integration'],
    'init_xml': [],
    'update_xml': [
#        'security/ir.model.access.csv',
#        'data/account_banking_data.xml',
        'bank_integration_view.xml',
        'hibiscus_config_wizard_view.xml',
        'wizard/bank_import_view.xml',
#        'account_banking_workflow.xml',
    ],
    'demo_xml': [],
    'description': '''
Hibiscus Online and CSV Parser Module for bank integration.

The module contains two pairs of parser

1. Online Hibiscus parser import and export.
2. CSV Hibiscus parser import and export.

For SSLv3 on Ubuntu requires:

sudo apt-get install python-dev

sudo apt-get install python-m2crypto
    ''',
    'active': False,
    'installable': True,
}
# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
