//OptionAction.java
package jmri.jmrit.operations.setup;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to load the options frame.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class OptionAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 7507020443490655869L;

    public OptionAction(String s) {
        super(s);
    }

    OptionFrame f = null;

    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            f = new OptionFrame();
            f.initComponents();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}

/* @(#)OptionAction.java */
