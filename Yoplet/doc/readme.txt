###############################################
#		Applet project					      #
#										      #
#		Author : 7uc0@lostinthegarden.org     #
###############################################

what to do :

edit the build.properties file to :
- set the proper key generation info
- set the http test folder path
- set the http test url

edit the conf/browser/ie.property and conf/browser/firefox.property files and set the proper executable path.
in order to switch from one to another, just add the 

testing the applet :
execute the following targets in this order : build gen-key run-test. If the above parameters are properly set, 
it will launch the browser against the tested url, and you'll be asked to accept/deny the corresponding certificate.

Enjoy

7uc0
