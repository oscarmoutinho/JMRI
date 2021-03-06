/**
 * JmriSRCPServerMenu.java
 */
package jmri.jmris.srcp;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "JMRI SRCP Server" menu containing the Server interface to the JMRI
 * system-independent tools
 *
 * @author	Paul Bender Copyright 2009
 * @version $Revision$
 */
public class JmriSRCPServerMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = -8023053965239852858L;

    public JmriSRCPServerMenu(String name) {
        this();
        setText(name);
    }

    public JmriSRCPServerMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.srcp.JmriSRCPServerBundle");

        setText(rb.getString("MenuServer"));
        add(new jmri.jmris.srcp.JmriSRCPServerAction(rb.getString("MenuItemStartServer")));

    }
}
