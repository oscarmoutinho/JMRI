# Script Programs DCC Specialties - Hare
# http://www.dccspecialties.com
#
# Script by Brett Scott
# Version 1.50 07/27/2014
# mgbrrclub@gmail.com or info@mgbrr.org and www.mgbrr.org
#
# Many Thanks to Bob Jacobsen and Everyone who Contributes to JMRI.
# 
# Script should work for any DCC System that supports Ops Mode Programming or 
# Programming on Mainline. (Digitrax, NEC, MRC, Lenz)
#
# You should copy this file to ProgramHare????.py Replacing ???? with the 
# Primary Address of your Hare. You should keep this file for backup, and 
# future tweaking and programming.
#
# Each group of program lines is documented. To execute a group of statements, remove 
# the # from the beginning of the proceeding program lines.  Edit the program lines  
# to include necessary information for your Hare.
# 
# Be Sure to remove the # from the "self.waitMsec(750)" line. This gives the Hare sufficient
# time to process each preceding command.
#
# This script uses Locomotive Address 9983.  Please make sure there is NO Locomotive number 9983
# on your layout while running this script.  This script will reprogram several of its CV's.
#
# Instructions
# 1. Turn Off Track Power - Use a Throttle or JMRI to do this.
# 2. Move the Program Jumper J2 From [J2-2 J2-3] To [J2-1 J2-2](Program Mode) on the Hare
# 3. Turn On Track Power - Again, Use a Throttle or JMRI.
# 4. VERY IMPORTANT - Wait 30 to 45 Seconds.
# 5. In JMRI Decoder Pro, Go to Panels > Run Script.  Select the file that corresponds
#    to the Hare you are programming.  Note - Do not have the file open in an editor.
#
# Important - This script can take 3 to 60 seconds to run. This depends on the number of 
#             CV's and addresses being programmed.
# 
#             When the script is complete, a small message window should appear. Usually
#             in the upper left corner of your screen.
#
# 6. Turn Off Track Power.
# 7. Move the program jumper J2 from [J2-1 J2-2] back to [J2-2 J2-3] (Normal Operation).
# 8. Turn Track Power On.
# 9. Test you Hare.
#

import jmri
import javax.swing

class setStartup(jmri.jmrit.automat.AbstractAutomaton) :      
  def init(self):
      return

  def handle(self):
    # Next Two Lines - Reset Hare to Factory Defaults
    programmers.getAddressedProgrammer(True, 9983).writeCV(63, 42, None)	
    self.waitMsec(750)         # time is in milliseconds

    # Next Two Lines - Point Hare to Receive Primary Address, Not Necessary, However, good practice.
    programmers.getAddressedProgrammer(True, 9983).writeCV(63, 0, None)	
    self.waitMsec(750)

    # Next Two Lines - Set Primary Address - Change 1234 to Address Desired
    turnouts.provideTurnout("1234").setState(CLOSED)				
    self.waitMsec(750)

    # Next Two Lines - CV49 - Controls What the Hare Sees as Clear or Thrown (Values 0,1)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(49, 1, None)	
    # self.waitMsec(750)

    # Next Two Lines - CV64 - Power Up Position (Smart Default) (Values 0,2,3)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(64, 0, None)	
    # self.waitMsec(750)

    # Next Two Lines - CV65 - Programmable Point Speed (Values 0-15)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(65, 0, None)	
    # self.waitMsec(750)

    # Next Two Lines - CV66 - Dispatcher Over-Ride(Auto-Throw Lock-Out)
    #                         (Values 0,1)OR(Values 0,1,2,4)DEPENDING ON VERSION
    # programmers.getAddressedProgrammer(True, 9983).writeCV(66, 0, None)	
    # self.waitMsec(750)

    # Next Two Lines - CV67 - Auto Throw Timer(Auto Throw Inhibit Time)(Values 0-255)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(67, 0, None)	
    # self.waitMsec(750)

    # Next Two Lines - CV68 - Semaphore OPS Mode (Values 0,1)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(68, 0, None)	
    # self.waitMsec(750)

    # Next Two Lines - CV69 - Auto Return Enable (Values 0,1,2,4,8) IF VERSION SUPPORTED
    # programmers.getAddressedProgrammer(True, 9983).writeCV(69, 0, None)	
    # self.waitMsec(750)

    # Next Two Lines - CV70 - Auto Return Delay (Values 0-255) IF VERSION SUPPORTED
    # programmers.getAddressedProgrammer(True, 9983).writeCV(70, 0, None)	
    # self.waitMsec(750)


    # Next Two Lines - CV72 - Sets the Number of Routes Available for Programming & Use.
    #                         (Values 0-28) IF VERSION SUPPORTED
    # programmers.getAddressedProgrammer(True, 9983).writeCV(72, 0, None)	
    # self.waitMsec(750)

    # Next Four Lines - Set Route 1 Address - Remove the # Symbols and Change 1234 to Address Desired
    #                   CV50 - Indicates Clear or Thrown Switch Position for Route 1 (Values 0,1,2,3)
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(50, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 2 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(51, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 3 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(52, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 4 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(53, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 5 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(54, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 6 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(55, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 7 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(56, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 8 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(57, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 9 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(58, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 10 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(59, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 11 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(60, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 12 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(61, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 13 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(62, 1, None)	
    # self.waitMsec(750)


    # THE FOLLOWING ROUTES MAY ONLY BE USED, IF YOUR HARE SUPPORTS MORE THAN 13 ROUTES.


    # Next Four Lines - Setup Route 14 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(73, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 15 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(74, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 16 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(75, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 17 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(76, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 18 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(77, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 19 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(78, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 20 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(79, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 21 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(80, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 22 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(81, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 23 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(82, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 24 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(83, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 25 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(84, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 26 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(85, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 27 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(86, 1, None)	
    # self.waitMsec(750)

    # Next Four Lines - Setup Route 28 - Address & Switch Position
    # turnouts.provideTurnout("1234").setState(CLOSED)				
    # self.waitMsec(750)
    # programmers.getAddressedProgrammer(True, 9983).writeCV(87, 1, None)	
    # self.waitMsec(750)

    javax.swing.JFrame("Programming Complete!").show()

    return 

setStartup().start()
