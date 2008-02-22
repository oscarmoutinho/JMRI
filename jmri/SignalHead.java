// SignalHead.java

package jmri;

/**
 * Represent a single signal head. (Try saying that ten times fast!)
 * A signal may have more than one of these 
 * (e.g. a signal mast consisting of several heads)
 * when needed to represent more complex aspects, e.g. Diverging Appoach
 * etc.
 * <P>
 * Initially, this allows access to explicit appearance information. We
 * don't call this an Aspect, as that's a composite of the appearance
 * of several heads.
 * <P>
 * This class has three bound parameters:
 *<DL>
 *<DT>appearance<DD>The specific color being shown. Values are the
 * various color contants defined in the class. 
 * <p>
 * The appearance constants form a bit mask, so they can
 * be used with hardware that can display e.g. more than one lit
 * color at a time.  Individual implementations may not be
 * able to handle that, however; most of the early ones 
 * probably won't.
 *<DT>lit<DD>Whether the head's lamps are lit or left dark.
 *<P>
 * This differs from the DARK color defined for the appearance
 * parameter, in that it's independent of that.  Lit is 
 * intended to allow you to extinquish a signal head for 
 * approach lighting, while still allowing it's color to be
 * set to a definite value for e.g. display on a panel or
 * evaluation in higher level logic.
 *
 *<DT>held<DD>Whether the head's lamps should be forced to the RED position
 * In higher-level logic.
 *<P>
 * For use in signaling systems, this is a convenient
 * way of storing whether a higher-level of control (e.g. non-vital
 * system or dispatcher) has "held" the signal at stop. It does
 * not effect how this signal head actually works; any appearance can
 * be set and will be displayed even when "held" is set.
 *</dl>
 * 
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Bob Jacobsen Copyright (C) 2002, 2008
 * @version			$Revision: 1.10 $
 */
public interface SignalHead extends NamedBean {

    public static final int DARK        = 0x00;
    public static final int RED         = 0x01;
    public static final int FLASHRED    = 0x02;
    public static final int YELLOW      = 0x04;
    public static final int FLASHYELLOW = 0x08;
    public static final int GREEN       = 0x10;
    public static final int FLASHGREEN  = 0x20;

    /**
     * Appearance is a bound parameter. 
     */
    public int getAppearance();
    public void setAppearance(int newAppearance);

    /**
     * Lit is a bound parameter. It controls
     * whether the signal head's lamps are lit or left dark.
     */
    public boolean getLit();
    public void setLit(boolean newLit);

    /**
     * Held is a bound parameter. It controls
     * what mechanisms can control the head's appearance.
     * The actual semantics are defined by those external mechanisms.
     */
    public boolean getHeld();
    public void setHeld(boolean newHeld);

}


/* @(#)SignalHead.java */
