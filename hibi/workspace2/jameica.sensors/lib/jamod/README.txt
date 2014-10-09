Java Modbus Library (jamod)
Copyright (c) 2002-2004, Dieter Wimberger

jamod is free software; you can distribute itself under the
terms of the BSD-style license received along with this
distribution.

This work was initially sponsored and supported by:
- Institute for Automation, University of Leoben
   http://automation.unileoben.ac.at
- TDE Thonhauser Data Engineering GMBH
   http://www.tde.at

The package has been tested against available Modbus reference
implementations, and remote industrial I/O attached to a bus
coupler.


How to get started:
-------------------
Please refer to the Development section of the documentation, which
is part of every release, as well as available online at:
http://jamod.sourceforge.net

For building, you will require 

A note for TINI deployment:
---------------------------
This library can be compiled and deployed on a TINI, however, the
required class library and utilities are not included in this
distribution. If you want to compile the sources against the TINI
and assemble the .tini file then you have to copy the files
- tini.jar
- tiniclasses.jar
- tini.db
from your <tini development kit>/bin directory to the lib directory of
this project.
You can then compile a tini version with the target "tini-compile",
package the file with the target "tini-file" and ftp this file
(build/jamod.tini) to the TINI for execution.
This has been verified for and on a TINI running the 1.02c firmware.

To mark the application class in the jamod.tini deployment package, you
can override or edit the property "tini.appclass"
<property name="tini.appclass" value="net.wimpi.modbus.cmd.TCPSlaveTest"/>


A note on the Java Comm API:
----------------------------
This package contains classes which provide serial transport capabilities.
For satisfying the dependencies on the Comm API on compile time (ONLY!), 
it is distributed with the comm.jar. 
If you plan to use the serial transport, then you will have to install
the a Java Comm API implementation. 

You can find the reference implementation from Sun (Win32, Solaris) at
http://java.sun.com/products/javacomm
Implementations for other platforms (Linux), are referenced from their
page.
 
More documentation:
-------------------
Please see the documentation in the docs directory, or on the network:
http://jamod.sourceforge.net

Probably check out the projects development page on SourceForge:
http://www.sourceforge.net/projects/jamod


