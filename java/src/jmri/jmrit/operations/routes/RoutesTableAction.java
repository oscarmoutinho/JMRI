// RoutesTableAction.java
package jmri.jmrit.operations.routes;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a RoutesTableFrame object.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class RoutesTableAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5090064971975194515L;

    public RoutesTableAction(String s) {
        super(s);
    }

    public RoutesTableAction() {
        this(Bundle.getMessage("MenuRoutes"));	// NOI18N
    }

    static RoutesTableFrame f = null;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void actionPerformed(ActionEvent e) {
        // create a route table frame
        if (f == null || !f.isVisible()) {
            f = new RoutesTableFrame();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)RoutesTableAction.java */
