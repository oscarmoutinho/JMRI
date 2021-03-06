// SecurityElementTest.java
package jmri.jmrix.loconet;

import jmri.Sensor;
import jmri.Turnout;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Generated by JBuilder
 * <p>
 * Title: SecurityElementTest </p>
 * <p>
 * Description: </p>
 * <p>
 * Copyright: Copyright (c) 2002</p>
 *
 * @author Bob Jacobsen
 * @version $Revision$
 */
public class SecurityElementTest extends TestCase {

    public void testConnectDefault() {
        Assert.assertEquals("12 connect left num ", 11, testSE12.attachAnum);
        Assert.assertEquals("12 connect left leg ", SecurityElement.B, testSE12.attachAleg);
        Assert.assertEquals("12 connect right num ", 13, testSE12.attachBnum);
        Assert.assertEquals("12 connect right leg ", SecurityElement.A, testSE12.attachBleg);
        Assert.assertEquals("12 connect C num ", 0, testSE12.attachCnum);
        Assert.assertEquals("12 connect C leg ", SecurityElement.NONE, testSE12.attachCleg);

        Assert.assertEquals("number of listeners ", 11, controller.numListeners());
    }

    public void testDirectionLogic() {
        testSE10.newDsStateOnA = Sensor.INACTIVE;
        testSE10.newDsStateOnB = Sensor.INACTIVE;
        testSE10.newDsStateOnC = Sensor.INACTIVE;
        testSE10.currentDsStateHere = Sensor.INACTIVE;

        testSE10.setDsState(Sensor.ACTIVE);
        testSE10.doUpdate();
        Assert.assertEquals("drop-in test ", SecurityElement.NONE, testSE10.newDirection);

        testSE10.setDsState(Sensor.INACTIVE);
        testSE10.doUpdate();
        Assert.assertEquals("lift-out test ", SecurityElement.NONE, testSE10.newDirection);

        testSE10.currentDsStateOnA = Sensor.ACTIVE;
        testSE10.newDsStateOnA = Sensor.ACTIVE;
        testSE10.currentDsStateHere = Sensor.INACTIVE;
        testSE10.newDsStateHere = Sensor.ACTIVE;   // note that setDsState() calls update already
        testSE10.makeAReservation = true;
        testSE10.doUpdate();
        Assert.assertEquals("from A test ", SecurityElement.AX, testSE10.newDirection);
        testSE10.doUpdate();  // 2nd update should hold reservation
        Assert.assertEquals("hold A reservation ", SecurityElement.AX, testSE10.newDirection);
    }

    public void testSpeedsForOccupied() {
        testSE10.newDsStateHere = Sensor.ACTIVE;
        testSE10.newTurnoutStateHere = Turnout.CLOSED;
        testSE10.newSpeedAX = 99;
        testSE10.newSpeedXA = 99;
        testSE10.doUpdate();

        Assert.assertEquals("AX speed ", 0, testSE10.currentSpeedAX);
        Assert.assertEquals("XA speed ", 0, testSE10.currentSpeedXA);
        Assert.assertEquals("messages sent ", 1, controller.outbound.size());
        Assert.assertEquals("message contents ", "E4 0A 00 0A 00 01 00 00 00 00",
                controller.outbound.elementAt(0).toString());
    }

    public void testSlowToBothSides() {
        testSE10.newDsStateHere = Sensor.INACTIVE;
        testSE10.newTurnoutStateHere = Turnout.CLOSED;
        testSE10.newSpeedLimitFromA = 24;
        testSE10.newSpeedLimitFromB = 28;
        testSE10.newSpeedAX = 999;
        testSE10.newSpeedXA = 999;
        testSE10.doUpdate();

        Assert.assertEquals("AX speed ", 48, testSE10.currentSpeedAX);
        Assert.assertEquals("XA speed ", 44, testSE10.currentSpeedXA);
        Assert.assertEquals("messages sent ", 1, controller.outbound.size());
        Assert.assertEquals("message contents ", "E4 0A 00 0A 00 00 00 30 2C 00",
                controller.outbound.elementAt(0).toString());
    }

    public void testDropIn() {
        // drop a loco into the center of a series of blocks

        testSE10.newDsStateHere = Sensor.INACTIVE;
        testSE11.newDsStateHere = Sensor.INACTIVE;
        testSE12.newDsStateHere = Sensor.INACTIVE;
        testSE13.newDsStateHere = Sensor.INACTIVE;
        testSE14.newDsStateHere = Sensor.INACTIVE;
        testSE15.newDsStateHere = Sensor.ACTIVE;
        testSE16.newDsStateHere = Sensor.INACTIVE;
        testSE17.newDsStateHere = Sensor.INACTIVE;
        testSE18.newDsStateHere = Sensor.INACTIVE;
        testSE19.newDsStateHere = Sensor.INACTIVE;
        testSE20.newDsStateHere = Sensor.INACTIVE;

        testSE10.newTurnoutStateHere = Turnout.CLOSED;
        testSE11.newTurnoutStateHere = Turnout.CLOSED;
        testSE12.newTurnoutStateHere = Turnout.CLOSED;
        testSE13.newTurnoutStateHere = Turnout.CLOSED;
        testSE14.newTurnoutStateHere = Turnout.CLOSED;
        testSE15.newTurnoutStateHere = Turnout.CLOSED;
        testSE16.newTurnoutStateHere = Turnout.CLOSED;
        testSE17.newTurnoutStateHere = Turnout.CLOSED;
        testSE18.newTurnoutStateHere = Turnout.CLOSED;
        testSE19.newTurnoutStateHere = Turnout.CLOSED;
        testSE20.newTurnoutStateHere = Turnout.CLOSED;

        testSE15.doUpdate();

        Assert.assertEquals("AX speed ", 0, testSE15.currentSpeedAX);
        Assert.assertEquals("XA speed ", 0, testSE15.currentSpeedXA);
        Assert.assertEquals("messages sent ", 1, controller.outbound.size());
        Assert.assertEquals("SE15 message contents ", "E4 0A 00 0F 00 01 00 00 00 00",
                controller.outbound.elementAt(0).toString());
        // start the propagation
        controller.forwardMessage(0);
        Assert.assertEquals("messages sent ", 3, controller.outbound.size());
        Assert.assertEquals("SE14 message contents ", "E4 0A 00 0E 00 04 00 14 14 00",
                controller.outbound.elementAt(1).toString());
        Assert.assertEquals("SE16 message contents ", "E4 0A 00 10 00 02 00 14 14 00",
                controller.outbound.elementAt(2).toString());

        // 1st message of 2nd turn
        controller.forwardMessage(1);  // from SE14
        Assert.assertEquals("messages sent ", 4, controller.outbound.size());
        Assert.assertEquals("SE13 message contents ", "E4 0A 00 0D 00 00 00 28 14 00",
                controller.outbound.elementAt(3).toString());

        // 2nd message of 2nd turn
        controller.forwardMessage(2);  // from SE16
        Assert.assertEquals("messages sent ", 5, controller.outbound.size());
        Assert.assertEquals("SE17 message contents ", "E4 0A 00 11 00 00 00 14 28 00",
                controller.outbound.elementAt(4).toString());

        // 1st message of 3rd turn
        controller.forwardMessage(3);  // from SE13
        Assert.assertEquals("messages sent ", 7, controller.outbound.size());
        Assert.assertEquals("SE12 message contents ", "E4 0A 00 0C 00 00 00 3C 14 00",
                controller.outbound.elementAt(5).toString());

        // 2nd message of 3rd turn
        controller.forwardMessage(4);  // from SE14
        Assert.assertEquals("messages sent ", 9, controller.outbound.size());
        Assert.assertEquals("SE14 message contents ", "E4 0A 00 0E 00 04 00 14 28 00",
                controller.outbound.elementAt(6).toString());

        // enough detail, check final state
        int next = 4;
        while ((++next) < controller.outbound.size()) {
            controller.forwardMessage(next);
        }

        Assert.assertEquals("total number of messages", 29, next);

        Assert.assertEquals("final SE10 AX ", 70, testSE10.currentSpeedAX);
        Assert.assertEquals("final SE10 XA ", 20, testSE10.currentSpeedXA);

        Assert.assertEquals("final SE11 AX ", 70, testSE11.currentSpeedAX);
        Assert.assertEquals("final SE11 XA ", 40, testSE11.currentSpeedXA);

        Assert.assertEquals("final SE12 AX ", 60, testSE12.currentSpeedAX);
        Assert.assertEquals("final SE12 XA ", 60, testSE12.currentSpeedXA);

        Assert.assertEquals("final SE13 AX ", 40, testSE13.currentSpeedAX);
        Assert.assertEquals("final SE13 XA ", 70, testSE13.currentSpeedXA);

        Assert.assertEquals("final SE14 AX ", 20, testSE14.currentSpeedAX);
        Assert.assertEquals("final SE14 XA ", 70, testSE14.currentSpeedXA);

        Assert.assertEquals("final SE15 AX ", 0, testSE15.currentSpeedAX);
        Assert.assertEquals("final SE15 XA ", 0, testSE15.currentSpeedXA);

        Assert.assertEquals("final SE16 AX ", 70, testSE16.currentSpeedAX);
        Assert.assertEquals("final SE16 XA ", 20, testSE16.currentSpeedXA);

        Assert.assertEquals("final SE17 AX ", 70, testSE17.currentSpeedAX);
        Assert.assertEquals("final SE17 XA ", 40, testSE17.currentSpeedXA);

        Assert.assertEquals("final SE18 AX ", 60, testSE18.currentSpeedAX);
        Assert.assertEquals("final SE18 XA ", 60, testSE18.currentSpeedXA);

        Assert.assertEquals("final SE19 AX ", 40, testSE19.currentSpeedAX);
        Assert.assertEquals("final SE19 XA ", 70, testSE19.currentSpeedXA);

        Assert.assertEquals("final SE20 AX ", 20, testSE20.currentSpeedAX);
        Assert.assertEquals("final SE20 XA ", 70, testSE20.currentSpeedXA);
    }

    // test infrastructure
    SecurityElement testSE10;
    SecurityElement testSE11;
    SecurityElement testSE12;
    SecurityElement testSE13;
    SecurityElement testSE14;
    SecurityElement testSE15;
    SecurityElement testSE16;
    SecurityElement testSE17;
    SecurityElement testSE18;
    SecurityElement testSE19;
    SecurityElement testSE20;

    public SecurityElementTest(String s) {
        super(s);
    }

    LocoNetInterfaceScaffold controller;  // holds dummy for testing

    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        controller = new LocoNetInterfaceScaffold();
        testSE10 = new SecurityElement(10);
        testSE11 = new SecurityElement(11);
        testSE12 = new SecurityElement(12);
        testSE13 = new SecurityElement(13);
        testSE14 = new SecurityElement(14);
        testSE15 = new SecurityElement(15);
        testSE16 = new SecurityElement(16);
        testSE17 = new SecurityElement(17);
        testSE18 = new SecurityElement(18);
        testSE19 = new SecurityElement(19);
        testSE20 = new SecurityElement(20);

    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
