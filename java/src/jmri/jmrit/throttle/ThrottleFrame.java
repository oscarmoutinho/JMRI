package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.configurexml.LoadXmlConfigAction;
import jmri.configurexml.StoreXmlConfigAction;
import jmri.jmrit.XmlFile;
import jmri.jmrit.jython.Jynstrument;
import jmri.jmrit.jython.JynstrumentFactory;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import jmri.util.iharder.dnd.FileDrop;
import jmri.util.iharder.dnd.FileDrop.Listener;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Should be named ThrottlePanel but was already existing with that name and
 * don't want to break dependencies (particularly in Jython code)
 *
 * @author Glen Oberhauser
 * @author Andrew Berridge Copyright 2010
 * @version $Revision$
 */
public class ThrottleFrame extends JDesktopPane implements ComponentListener, AddressListener {

    /**
     *
     */
    private static final long serialVersionUID = -2843734025583850839L;
    private final Integer BACKPANEL_LAYER = Integer.valueOf(Integer.MIN_VALUE);
    private final Integer PANEL_LAYER_FRAME = Integer.valueOf(1);
    private final Integer PANEL_LAYER_PANEL = Integer.valueOf(2);

    private static int NEXT_FRAME_KEY = KeyEvent.VK_RIGHT;
    private static int PREV_FRAME_KEY = KeyEvent.VK_LEFT;

    private static int ADDRESS_PANEL_INDEX = 0;
    private static int CONTROL_PANEL_INDEX = 1;
    private static int FUNCTION_PANEL_INDEX = 2;
    private static int NUM_FRAMES = 3;

    private JInternalFrame[] frameList;
    private int activeFrame;

    private ThrottleWindow throttleWindow;

    private ControlPanel controlPanel;
    private FunctionPanel functionPanel;
    private AddressPanel addressPanel;
    private BackgroundPanel backgroundPanel;
    private FrameListener frameListener;

    private String title;
    private String lastUsedSaveFile = null;
    private static String DefaultThrottleFileName = "JMRI_ThrottlePreference.xml";

    public static String getDefaultThrottleFolder() {
        return FileUtil.getUserFilesPath() + "throttle" + File.separator;
    }

    public static String getDefaultThrottleFilename() {
        return getDefaultThrottleFolder() + DefaultThrottleFileName;
    }

    public ThrottleFrame(ThrottleWindow tw) {
        super();
        throttleWindow = tw;
        initGUI();
        jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesListPanel().getTableModel().addThrottleFrame(this);
    }

    public ThrottleWindow getThrottleWindow() {
        return throttleWindow;
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public FunctionPanel getFunctionPanel() {
        return functionPanel;
    }

    public AddressPanel getAddressPanel() {
        return addressPanel;
    }

    public RosterEntry getRosterEntry() {
        return addressPanel.getRosterEntry();
    }

    public void toFront() {
        if (throttleWindow == null) {
            return;
        }
        throttleWindow.toFront(title);
    }

    /**
     * Sets the location of a throttle frame on the screen according to x and y
     * coordinates
     *
     * @see java.awt.Component#setLocation(int, int)
     */
    public void setLocation(int x, int y) {
        if (throttleWindow == null) {
            return;
        }
        throttleWindow.setLocation(new Point(x, y));
    }

    public void setTitle(String txt) {
        title = txt;
    }

    public String getTitle() {
        return title;
    }

    private void saveThrottle(String sfile) {
        // Save throttle: title / window position
        // as strongly linked to extended throttles and roster presence, do not save function buttons and background window as they're stored in the roster entry
        XmlFile xf = new XmlFile() {
        };   // odd syntax is due to XmlFile being abstract
        xf.makeBackupFile(sfile);
        File file = new File(sfile);
        try {
            //The file does not exist, create it before writing
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdir()) // make directory and check result
                {
                    log.error("could not make parent directory");
                }
            }
            if (!file.createNewFile()) // create file, check success
            {
                log.error("createNewFile failed");
            }
        } catch (Exception exp) {
            log.error("Exception while writing the throttle file, may not be complete: " + exp);
        }

        try {
            Element root = new Element("throttle-config");
            Document doc = XmlFile.newDocument(root, XmlFile.dtdLocation + "throttle-config.dtd");
            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/throttle.xsl"?>
/*			java.util.Map<String,String> m = new java.util.HashMap<String,String>();
             m.put("type", "text/xsl");
             m.put("href", jmri.jmrit.XmlFile.xsltLocation+"throttle.xsl");
             ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
             doc.addContent(0,p);*/
            Element throttleElement = getXml();
            // don't save the loco address or consist address
//			throttleElement.getChild("AddressPanel").removeChild("locoaddress");
//			throttleElement.getChild("AddressPanel").removeChild("locoaddress");
            if ((this.getRosterEntry() != null) && (getDefaultThrottleFolder() + addressPanel.getRosterEntry().getId().trim() + ".xml").compareTo(sfile) == 0) // don't save function buttons labels, they're in roster entry	
            {
                throttleElement.getChild("FunctionPanel").removeChildren("FunctionButton");
            }

            root.setContent(throttleElement);
            xf.writeXML(file, doc);
            setLastUsedSaveFile(sfile);
        } catch (Exception ex) {
            log.warn("Exception while storing throttle xml: " + ex);
        }
    }

    public void loadThrottle(String sfile) {
        if (sfile == null) {
            JFileChooser fileChooser = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("PromptXmlFileTypes"), "xml");
            fileChooser.setCurrentDirectory(new File(getDefaultThrottleFolder()));
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            java.io.File file = LoadXmlConfigAction.getFile(fileChooser);
            if (file == null) {
                return;
            }
            sfile = file.getAbsolutePath();
            if (sfile == null) {
                return;
            }
        }

        boolean switchAfter = false;
        if (!isEditMode) {
            switchMode();
            switchAfter = true;
        }

        try {
            XmlFile xf = new XmlFile() {
            };   // odd syntax is due to XmlFile being abstract
            File f = new File(sfile);
            Element root = xf.rootFromFile(f);
            Element conf = root.getChild("ThrottleFrame");
            // File looks ok
            setLastUsedSaveFile(sfile);
            // close all existing Jynstruments
            Component[] cmps = getComponents();
            for (int i = 0; i < cmps.length; i++) {
                try {
                    if (cmps[i] instanceof JInternalFrame) {
                        JInternalFrame jyf = (JInternalFrame) cmps[i];
                        Component[] cmps2 = jyf.getContentPane().getComponents();
                        for (int j = 0; j < cmps2.length; j++) {
                            if (cmps2[j] instanceof Jynstrument) {
                                ((Jynstrument) cmps2[j]).exit();
                                jyf.dispose();
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.debug("Got exception (no panic) " + ex);
                }
            }
            // and finally load all preferences
            setXml(conf);
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Loading throttle exception: " + ex.getMessage());
            }
        }
//    	checkPosition();
        if (switchAfter) {
            switchMode();
        }
        return;
    }

    /**
     * Place and initialize the GUI elements.
     * <ul>
     * <li> ControlPanel
     * <li> FunctionPanel
     * <li> AddressPanel
     * <li> JMenu
     * </ul>
     *
     */
    private void initGUI() {
        frameListener = new FrameListener();

        controlPanel = new ControlPanel();
        controlPanel.setResizable(true);
        controlPanel.setClosable(true);
        controlPanel.setIconifiable(true);
        controlPanel.setTitle(Bundle.getMessage("ThrottleMenuViewControlPanel"));
        controlPanel.pack();
        controlPanel.setVisible(true);
        controlPanel.setEnabled(false);
        controlPanel.addInternalFrameListener(frameListener);

        functionPanel = new FunctionPanel();
        functionPanel.setResizable(true);
        functionPanel.setClosable(true);
        functionPanel.setIconifiable(true);
        functionPanel.setTitle(Bundle.getMessage("ThrottleMenuViewFunctionPanel"));

        // assumes button width of 54, height of 30 (set in class FunctionButton) with
        // horiz and vert gaps of 5 each (set in FunctionPanel class)
        // with 3 buttons across and 6 rows high
        int width = 3 * (FunctionButton.BUT_WDTH) + 2 * 3 * 5 + 10; 		// = 192
        int height = 6 * (FunctionButton.BUT_HGHT) + 2 * 6 * 5 + 20;	// = 240 (but there seems to be another 10 needed for some LAFs)

        if (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle()
                && jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingFunctionIcon()) {
            width = FunctionButton.BUT_WDTH * 3 + 2 * 3 * 5 + 10;
            height = FunctionButton.BUT_IMG_SIZE * 2 + FunctionButton.BUT_HGHT * 4 + 2 * 6 * 5 + 20;
        }
        functionPanel.setSize(width, height);
        functionPanel.setLocation(controlPanel.getWidth(), 0);
        functionPanel.setVisible(true);
        functionPanel.setEnabled(false);
        functionPanel.addInternalFrameListener(frameListener);

        addressPanel = new AddressPanel();
        addressPanel.setResizable(true);
        addressPanel.setClosable(true);
        addressPanel.setIconifiable(true);
        addressPanel.setTitle(Bundle.getMessage("ThrottleMenuViewAddressPanel"));
        addressPanel.pack();
        //                if (addressPanel.getWidth()<functionPanel.getWidth()) {addressPanel.setSize(functionPanel.getWidth(),addressPanel.getHeight());}
        addressPanel.setLocation(controlPanel.getWidth(), functionPanel.getHeight());
        addressPanel.setVisible(true);
        addressPanel.addInternalFrameListener(frameListener);
        functionPanel.setAddressPanel(addressPanel); // so the function panel can get access to the roster
        controlPanel.setAddressPanel(addressPanel);

        if (controlPanel.getHeight() < functionPanel.getHeight() + addressPanel.getHeight()) {
            controlPanel.setSize(controlPanel.getWidth(), functionPanel.getHeight() + addressPanel.getHeight());
        }
        if (controlPanel.getHeight() > functionPanel.getHeight() + addressPanel.getHeight()) {
            addressPanel.setSize(addressPanel.getWidth(), controlPanel.getHeight() - functionPanel.getHeight());
        }
        if (!(jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle() && jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingFunctionIcon())
                && (functionPanel.getWidth() < addressPanel.getWidth())) {
            functionPanel.setSize(addressPanel.getWidth(), functionPanel.getHeight());
        }

        addressPanel.addAddressListener(controlPanel);
        addressPanel.addAddressListener(functionPanel);
        addressPanel.addAddressListener(this);
        addressPanel.addAddressListener(jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesListPanel().getTableModel());

        add(controlPanel, PANEL_LAYER_FRAME);
        add(functionPanel, PANEL_LAYER_FRAME);
        add(addressPanel, PANEL_LAYER_FRAME);

        if (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle()) {
            /*        	if ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingTransparentCtl() ) {
             setTransparent(functionPanel);
             setTransparent(addressPanel);
             setTransparent(controlPanel);
             }*/
            if (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingRosterImage()) {
                backgroundPanel = new BackgroundPanel();
                backgroundPanel.setAddressPanel(addressPanel); // reusing same way to do it than existing thing in functionPanel
                addComponentListener(backgroundPanel); // backgroudPanel warned when desktop resized
                addressPanel.addAddressListener(backgroundPanel);
                addressPanel.setBackgroundPanel(backgroundPanel); // so that it's changeable when browsing through rosters
                add(backgroundPanel, BACKPANEL_LAYER);
            }
            addComponentListener(this); // to force sub windows repositionning
        }

        frameList = new JInternalFrame[NUM_FRAMES];
        frameList[ADDRESS_PANEL_INDEX] = addressPanel;
        frameList[CONTROL_PANEL_INDEX] = controlPanel;
        frameList[FUNCTION_PANEL_INDEX] = functionPanel;
        activeFrame = ADDRESS_PANEL_INDEX;

        setPreferredSize(new Dimension(Math.max(controlPanel.getWidth() + functionPanel.getWidth(), controlPanel.getWidth() + addressPanel.getWidth()),
                Math.max(addressPanel.getHeight() + functionPanel.getHeight(), controlPanel.getHeight())));

        // #JYNSTRUMENT# Bellow prepare drag'n drop receptacle:
        new FileDrop(this, new Listener() {
            public void filesDropped(File[] files) {
                if (isEditMode) {
                    for (int i = 0; i < files.length; i++) {
                        ynstrument(files[i].getPath());
                    }
                }
            }
        });

        KeyListenerInstaller.installKeyListenerOnAllComponents(new FrameCyclingKeyListener(), this);
        try {
            addressPanel.setSelected(true);
        } catch (java.beans.PropertyVetoException ex) {
            log.error("Error selecting InternalFrame:" + ex);
        }
    }

    // #JYNSTRUMENT# here instantiate the Jynstrument, put it in a component, initialize the context and start it
    public JInternalFrame ynstrument(String path) {
        if (path == null) {
            return null;
        }
        Jynstrument it = JynstrumentFactory.createInstrument(path, this); // everything is there
        if (it == null) {
            log.error("Error while creating Jynstrument " + path);
            return null;
        }
        setTransparentBackground(it);
        JInternalFrame newiFrame = new JInternalFrame(it.getClassName());
        newiFrame.add(it);
        newiFrame.addInternalFrameListener(frameListener);
        newiFrame.setDoubleBuffered(true);
        newiFrame.setResizable(true);
        newiFrame.setClosable(true);
        newiFrame.setIconifiable(true);
        newiFrame.getContentPane().addContainerListener(new ContainerListener() {
            public void componentAdded(ContainerEvent e) {
            }

            public void componentRemoved(ContainerEvent e) {
                Container c = e.getContainer();
                while ((!(c instanceof JInternalFrame)) && (!(c instanceof TranslucentJPanel))) {
                    c = c.getParent();
                }
                c.setVisible(false);
                remove(c);
                repaint();
            }
        });
        newiFrame.pack();
        add(newiFrame, PANEL_LAYER_FRAME);
        newiFrame.setVisible(true);
        return newiFrame;
    }

    // make sure components are inside this frame bounds
    private void checkPosition(Component comp) {
        if ((this.getWidth() < 1) || (this.getHeight() < 1)) {
            return;
        }

        Rectangle pos = comp.getBounds();

        if (pos.width > this.getWidth()) { // Component largest than container
            pos.width = this.getWidth() - 2;
            pos.x = 1;
        }
        if (pos.x + pos.width > this.getWidth()) // Component to large
        {
            pos.x = this.getWidth() - pos.width - 1;
        }
        if (pos.x < 0) // Component to far on the left
        {
            pos.x = 1;
        }

        if (pos.height > this.getHeight()) { // Component higher than container
            pos.height = this.getHeight() - 2;
            pos.y = 1;
        }
        if (pos.y + pos.height > this.getHeight()) // Component to low
        {
            pos.y = this.getHeight() - pos.height - 1;
        }
        if (pos.y < 0) // Component to high
        {
            pos.y = 1;
        }

        comp.setBounds(pos);
    }

    public void makeAllComponentsInBounds() {
        Component[] cmps = getComponents();
        for (int i = 0; i < cmps.length; i++) {
            checkPosition(cmps[i]);
        }
    }

    private HashMap<Container, JInternalFrame> contentPanes;

    private static class TranslucentJPanel extends JPanel {

        /**
         *
         */
        private static final long serialVersionUID = -7447697024958214244L;
        private Color TRANS_COL = new Color(100, 100, 100, 100);

        public TranslucentJPanel() {
            super();
            setOpaque(false);
        }

        public void paintComponent(Graphics g) {
            g.setColor(TRANS_COL);
            g.fillRoundRect(0, 0, getSize().width, getSize().height, 10, 10);
            super.paintComponent(g);
        }
    }

    private void playRendering() {
        Component[] cmps = getComponentsInLayer(PANEL_LAYER_FRAME);
        contentPanes = new HashMap<Container, JInternalFrame>();
        for (int i = 0; i < cmps.length; i++) {
            if ((cmps[i] instanceof JInternalFrame) && (cmps[i].isVisible())) {
                JInternalFrame jif = (JInternalFrame) cmps[i];
                Dimension cpSize = jif.getContentPane().getSize();
                Point cpLoc = jif.getContentPane().getLocationOnScreen();

                TranslucentJPanel pane = new TranslucentJPanel();
                pane.setLayout(new BorderLayout());
                contentPanes.put(pane, jif);
                pane.add(jif.getContentPane(), BorderLayout.CENTER);
                setTransparent(pane, true);

                jif.setContentPane(new JPanel());
                jif.setVisible(false);
                Point loc = new Point(cpLoc.x - this.getLocationOnScreen().x, cpLoc.y - this.getLocationOnScreen().y);
                add(pane, PANEL_LAYER_PANEL);
                pane.setLocation(loc);
                pane.setSize(cpSize);
            }
        }
    }

    private void editRendering() {
        Component[] cmps = getComponentsInLayer(PANEL_LAYER_PANEL);
        for (int i = 0; i < cmps.length; i++) {
            if (cmps[i] instanceof JPanel) {
                JPanel pane = (JPanel) cmps[i];
                JInternalFrame jif = contentPanes.get(pane);
                jif.setContentPane((Container) pane.getComponent(0));
                setTransparent(jif, false);
                jif.setVisible(true);
                remove(pane);
            }
        }
    }

    private boolean isEditMode = true;
    private boolean willSwitch = false;

    public void switchMode() {
        if (isVisible()) {
            if (isEditMode) {
                playRendering();
            } else {
                editRendering();
            }
            isEditMode = !isEditMode;
            willSwitch = false;
        } else {
            willSwitch = !willSwitch;
        }
        throttleWindow.updateGUI();
    }

    /**
     * Handle my own destruction.
     * <ol>
     * <li> dispose of sub windows.
     * <li> notify my manager of my demise.
     * </ol>
     *
     */
    public void dispose() {
        log.debug("Disposing " + getTitle());
        addressPanel.removeAddressListener(this);
        jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesListPanel().getTableModel().removeThrottleFrame(this, addressPanel.getCurrentAddress());
        // check for any special disposing in InternalFrames
        controlPanel.destroy();
        functionPanel.destroy();
        // dispose of this last because it will release and destroy throttle.
        addressPanel.destroy();
    }

    public void saveRosterChanges() {
        RosterEntry rosterEntry = addressPanel.getRosterEntry();
        if (rosterEntry == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("ThrottleFrameNoRosterItemMessageDialog"), Bundle.getMessage("ThrottleFrameNoRosterItemTitleDialog"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("ThrottleFrameRosterChangeMesageDialog"), Bundle.getMessage("ThrottleFrameRosterChangeTitleDialog"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        functionPanel.saveFunctionButtonsToRoster(rosterEntry);
        controlPanel.saveToRoster(rosterEntry);
    }

    /**
     * An extension of InternalFrameAdapter for listening to the closing of of
     * this frame's internal frames.
     *
     * @author glen
     */
    class FrameListener extends InternalFrameAdapter {

        /**
         * Listen for the closing of an internal frame and set the "View" menu
         * appropriately. Then hide the closing frame
         *
         * @param e The InternalFrameEvent leading to this action
         */
        public void internalFrameClosing(InternalFrameEvent e) {
            if (e.getSource() == controlPanel) {
                throttleWindow.getViewControlPanel().setSelected(false);
                controlPanel.setVisible(false);
            } else if (e.getSource() == addressPanel) {
                throttleWindow.getViewAddressPanel().setSelected(false);
                addressPanel.setVisible(false);
            } else if (e.getSource() == functionPanel) {
                throttleWindow.getViewFunctionPanel().setSelected(false);
                functionPanel.setVisible(false);
            } else {
                try { // #JYNSTRUMENT#, Very important, clean the Jynstrument
                    if ((e.getSource() instanceof JInternalFrame)) {
                        Component[] cmps = ((JInternalFrame) e.getSource()).getContentPane().getComponents();
                        int i = 0;
                        while ((i < cmps.length) && (!(cmps[i] instanceof Jynstrument))) {
                            i++;
                        }
                        if ((i < cmps.length) && (cmps[i] instanceof Jynstrument)) {
                            ((Jynstrument) cmps[i]).exit();
                        }
                    }
                } catch (Exception exc) {
                    if (log.isDebugEnabled()) {
                        log.debug("Got exception, can ignore :" + exc);
                    }
                }
            }
        }

        /**
         * Listen for the activation of an internal frame record this property
         * for correct processing of the frame cycling key.
         *
         * @param e The InternalFrameEvent leading to this action
         */
        public void internalFrameActivated(InternalFrameEvent e) {
            if (e.getSource() == controlPanel) {
                activeFrame = CONTROL_PANEL_INDEX;
            } else if (e.getSource() == addressPanel) {
                activeFrame = ADDRESS_PANEL_INDEX;
            } else if (e.getSource() == functionPanel) {
                activeFrame = FUNCTION_PANEL_INDEX;
            }
        }
    }

    /**
     * Collect the prefs of this object into XML Element
     * <ul>
     * <li> Window prefs
     * <li> ControlPanel
     * <li> FunctionPanel
     * <li> AddressPanel
     * </ul>
     *
     *
     * @return the XML of this object.
     */
    public Element getXml() {
        boolean switchAfter = false;
        if (!isEditMode) {
            switchMode();
            switchAfter = true;
        }

        Element me = new Element("ThrottleFrame");

        if (((javax.swing.plaf.basic.BasicInternalFrameUI) getControlPanel().getUI()).getNorthPane() != null) {
            Dimension bDim = ((javax.swing.plaf.basic.BasicInternalFrameUI) getControlPanel().getUI()).getNorthPane().getPreferredSize();
            me.setAttribute("border", Integer.toString(bDim.height));
        }

        java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(1);

//        children.add(WindowPreferences.getPreferences(this));  // not required as it is in ThrottleWindow
        children.add(controlPanel.getXml());
        children.add(functionPanel.getXml());
        children.add(addressPanel.getXml());

        // Save Jynstruments
        Component[] cmps = getComponents();
        for (int i = 0; i < cmps.length; i++) {
            try {
                if (cmps[i] instanceof JInternalFrame) {
                    Component[] cmps2 = ((JInternalFrame) cmps[i]).getContentPane().getComponents();
                    int j = 0;
                    while ((j < cmps2.length) && (!(cmps2[j] instanceof Jynstrument))) {
                        j++;
                    }
                    if ((j < cmps2.length) && (cmps2[j] instanceof Jynstrument)) {
                        Jynstrument jyn = (Jynstrument) cmps2[j];
                        Element elt = new Element("Jynstrument");
                        elt.setAttribute("JynstrumentFolder", FileUtil.getPortableFilename(jyn.getFolder()));
                        java.util.ArrayList<Element> jychildren = new java.util.ArrayList<Element>(1);
                        jychildren.add(WindowPreferences.getPreferences((JInternalFrame) cmps[i]));
                        Element je = jyn.getXml();
                        if (je != null) {
                            jychildren.add(je);
                        }
                        elt.setContent(jychildren);
                        children.add(elt);
                    }
                }
            } catch (Exception ex) {
                log.debug("Got exception (no panic) " + ex);
            }
        }
        me.setContent(children);
        if (switchAfter) {
            switchMode();
        }
        return me;
    }

    public Element getXmlFile() {
        if (getLastUsedSaveFile() == null) // || (getRosterEntry()==null))
        {
            return null;
        }
        Element me = new Element("ThrottleFrame");
        me.setAttribute("ThrottleXMLFile", FileUtil.getPortableFilename(getLastUsedSaveFile()));
        return me;
    }

    /**
     * Set the preferences based on the XML Element.
     * <ul>
     * <li> Window prefs
     * <li> Frame title
     * <li> ControlPanel
     * <li> FunctionPanel
     * <li> AddressPanel
     * </ul>
     *
     *
     * @param e The Element for this object.
     */
    @SuppressWarnings("unchecked")
    public void setXml(Element e) {
        if (e == null) {
            return;
        }

        String sfile = e.getAttributeValue("ThrottleXMLFile");
        if (sfile != null) {
            loadThrottle(FileUtil.getExternalFilename(sfile));
            return;
        }

        boolean switchAfter = false;
        if (!isEditMode) {
            switchMode();
            switchAfter = true;
        }

        int bSize = 23;
        // Get InternalFrame border size
        if (e.getAttribute("border") != null) {
            bSize = Integer.parseInt((e.getAttribute("border").getValue()));
        }
        if (e.getChild("window") != null) // Old format
        {
            throttleWindow.setXml(e);
        }
        Element controlPanelElement = e.getChild("ControlPanel");
        controlPanel.setXml(controlPanelElement);
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanel.getUI()).getNorthPane() != null) {
            ((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanel.getUI()).getNorthPane().setPreferredSize(new Dimension(0, bSize));
        }
        Element functionPanelElement = e.getChild("FunctionPanel");
        functionPanel.setXml(functionPanelElement);
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanel.getUI()).getNorthPane() != null) {
            ((javax.swing.plaf.basic.BasicInternalFrameUI) functionPanel.getUI()).getNorthPane().setPreferredSize(new Dimension(0, bSize));
        }
        Element addressPanelElement = e.getChild("AddressPanel");
        addressPanel.setXml(addressPanelElement);
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanel.getUI()).getNorthPane() != null) {
            ((javax.swing.plaf.basic.BasicInternalFrameUI) addressPanel.getUI()).getNorthPane().setPreferredSize(new Dimension(0, bSize));
        }

        List<Element> jinsts = e.getChildren("Jynstrument");
        if ((jinsts != null) && (jinsts.size() > 0)) {
            for (int i = 0; i < jinsts.size(); i++) {
                JInternalFrame jif = ynstrument(FileUtil.getExternalFilename(jinsts.get(i).getAttributeValue("JynstrumentFolder")));
                Element window = jinsts.get(i).getChild("window");
                if (jif != null) {
                    if (window != null) {
                        WindowPreferences.setPreferences(jif, window);
                    }
                    Component[] cmps2 = jif.getContentPane().getComponents();
                    int j = 0;
                    while ((j < cmps2.length) && (!(cmps2[j] instanceof Jynstrument))) {
                        j++;
                    }
                    if ((j < cmps2.length) && (cmps2[j] instanceof Jynstrument) && (jinsts.get(i) != null)) {
                        ((Jynstrument) cmps2[j]).setXml(jinsts.get(i));
                    }

                    jif.repaint();
                }
            }
        }
        setFrameTitle();
        if (switchAfter) {
            switchMode();
        }
    }

    /**
     * setFrameTitle - set the frame title based on type, text and address
     */
    public void setFrameTitle() {
        String addr = Bundle.getMessage("ThrottleFrameTitle");
        if (addressPanel.getThrottle() != null) {
            addr = addressPanel.getCurrentAddress().toString();
        }
        if (throttleWindow.getTitleTextType().compareTo("address") == 0) {
            throttleWindow.setTitle(addr);
        } else if (throttleWindow.getTitleTextType().compareTo("text") == 0) {
            throttleWindow.setTitle(throttleWindow.getTitleText());
        } else if (throttleWindow.getTitleTextType().compareTo("addressText") == 0) {
            throttleWindow.setTitle(addr + " " + throttleWindow.getTitleText());
        } else if (throttleWindow.getTitleTextType().compareTo("textAddress") == 0) {
            throttleWindow.setTitle(throttleWindow.getTitleText() + " " + addr);
        } else if (throttleWindow.getTitleTextType().compareTo("rosterID") == 0) {
            if ((addressPanel.getRosterEntry() != null) && (addressPanel.getRosterEntry().getId() != null)
                    && (addressPanel.getRosterEntry().getId().length() > 0)) {
                throttleWindow.setTitle(addressPanel.getRosterEntry().getId());
            } else {
                throttleWindow.setTitle(addr);
            }
        }
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
//		checkPosition ();
    }

    public void componentShown(ComponentEvent e) {
        throttleWindow.setCurrentThrottleFrame(this);
        if (willSwitch) {
            switchMode();
            repaint();
        }
        throttleWindow.updateGUI();
    }

    public void saveThrottle() {
        if (getRosterEntry() != null) {
            saveThrottle(getDefaultThrottleFolder() + addressPanel.getRosterEntry().getId().trim() + ".xml");
        } else if (getLastUsedSaveFile() != null) {
            saveThrottle(getLastUsedSaveFile());
        }
    }

    public void saveThrottleAs() {
        JFileChooser fileChooser = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("PromptXmlFileTypes"), "xml");
        fileChooser.setCurrentDirectory(new File(getDefaultThrottleFolder()));
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        java.io.File file = StoreXmlConfigAction.getFileName(fileChooser);
        if (file == null) {
            return;
        }
        saveThrottle(file.getAbsolutePath());
    }

    /**
     * A KeyAdapter that listens for the key that cycles through the
     * JInternalFrames.
     *
     * @author glen
     */
    class FrameCyclingKeyListener extends KeyAdapter {

        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void keyReleased(KeyEvent e) {
            if (e.isControlDown() && e.getKeyCode() == NEXT_FRAME_KEY) {
                try {
                    activeFrame = (activeFrame + 1) % NUM_FRAMES;
                    frameList[activeFrame].setSelected(true);
                } catch (java.beans.PropertyVetoException ex) {
                    log.warn("Exception selecting internal frame:" + ex);
                }
            } else if (e.isControlDown() && e.getKeyCode() == PREV_FRAME_KEY) {
                try {
                    activeFrame--;
                    if (activeFrame < 0) {
                        activeFrame = NUM_FRAMES - 1;
                    }
                    frameList[activeFrame].setSelected(true);
                } catch (java.beans.PropertyVetoException ex) {
                    log.warn("Exception selecting internal frame:" + ex);
                }
            }
        }
    }

    public void notifyAddressChosen(LocoAddress l) {
    }

    public void notifyAddressReleased(LocoAddress la) {
        setLastUsedSaveFile(null);
        setFrameTitle();
        throttleWindow.updateGUI();
    }

    public void notifyAddressThrottleFound(DccThrottle throttle) {
        if ((jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle())
                && (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isAutoLoading())
                && (addressPanel != null) && (addressPanel.getRosterEntry() != null)
                && ((getLastUsedSaveFile() == null) || (getLastUsedSaveFile().compareTo(getDefaultThrottleFolder() + addressPanel.getRosterEntry().getId().trim() + ".xml") != 0))) {
            loadThrottle(getDefaultThrottleFolder() + addressPanel.getRosterEntry().getId().trim() + ".xml");
        }
        setFrameTitle();
        throttleWindow.updateGUI();
    }

    public void notifyConsistAddressChosen(int newAddress, boolean isLong) {
    }

    public void notifyConsistAddressReleased(int address, boolean isLong) {
    }

    public void notifyConsistAddressThrottleFound(DccThrottle throttle) {
    }

    public String getLastUsedSaveFile() {
        return lastUsedSaveFile;
    }

    public void setLastUsedSaveFile(String lusf) {
        lastUsedSaveFile = lusf;
        throttleWindow.updateGUI();
    }

    // some utilities to turn a component background transparent
    public static void setTransparentBackground(JComponent jcomp) {
        if (jcomp instanceof JPanel) //OS X: Jpanel components are enough
        {
            jcomp.setBackground(new Color(0, 0, 0, 0));
        }
        setTransparentBackground(jcomp.getComponents());
    }

    public static void setTransparentBackground(Component[] comps) {
        for (int i = 0; i < comps.length; i++) {
            try {
                if (comps[i] instanceof JComponent) {
                    setTransparentBackground((JComponent) comps[i]);
                }
            } catch (Exception e) { // Do nothing, just go on
            }
        }
    }

// some utilities to turn a component background transparent
    public static void setTransparent(JComponent jcomp) {
        setTransparent(jcomp, true);
    }

    public static void setTransparent(JComponent jcomp, boolean transparency) {
        if (jcomp instanceof JPanel) //OS X: Jpanel components are enough
        {
            jcomp.setOpaque(!transparency);
        }
        setTransparent(jcomp.getComponents(), transparency);
    }

    private static void setTransparent(Component[] comps, boolean transparency) {
        for (int i = 0; i < comps.length; i++) {
            try {
                if (comps[i] instanceof JComponent) {
                    setTransparent((JComponent) comps[i], transparency);
                }
            } catch (Exception e) { // Do nothing, just go on
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottleFrame.class.getName());
}
