// SignalMastIcon.java
package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalMast;
import jmri.Transit;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.SignalMastItemPanel;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a {@link jmri.SignalMast}.
 * <p>
 * The icons displayed are loaded from the {@link jmri.SignalAppearanceMap} in
 * the {@link jmri.SignalMast}.
 *
 * @see jmri.SignalMastManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2009, 2014
 * @version $Revision$
 */
public class SignalMastIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 1381602315927376318L;

    public SignalMastIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(editor);
        _control = true;
        debug = log.isDebugEnabled();
    }

    private NamedBeanHandle<SignalMast> namedMast;
    private boolean debug;

    public void setShowAutoText(boolean state) {
        _text = state;
        _icon = !_text;
    }

    public Positionable deepClone() {
        SignalMastIcon pos = new SignalMastIcon(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        SignalMastIcon pos = (SignalMastIcon) p;
        pos.setSignalMast(getNamedSignalMast().getName());
        pos._iconMap = cloneMap(_iconMap, pos);
        pos.setClickMode(getClickMode());
        pos.setLitMode(getLitMode());
        pos.useIconSet(useIconSet());
        return super.finishClone(pos);
    }

    /**
     * Attached a signalmast element to this display item
     *
     * @param sh Specific SignalMast handle
     */
    public void setSignalMast(NamedBeanHandle<SignalMast> sh) {
        if (namedMast != null) {
            getSignalMast().removePropertyChangeListener(this);
        }
        namedMast = sh;
        if (namedMast != null) {
            getIcons();
            displayState(mastState());
            getSignalMast().addPropertyChangeListener(this, namedMast.getName(), "SignalMast Icon");
        }
    }

    /**
     * Taken from the layout editor Attached a numbered element to this display
     * item
     *
     * @param pName Used as a system/user name to lookup the SignalMast object
     */
    public void setSignalMast(String pName) {
        SignalMast mMast = (SignalMast) InstanceManager.signalMastManagerInstance().getNamedBean(pName);
        if (mMast == null) {
            log.warn("did not find a SignalMast named " + pName);
        } else {
            setSignalMast(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, mMast));
        }
    }

    private void getIcons() {
        _iconMap = new java.util.HashMap<String, NamedIcon>();
        java.util.Enumeration<String> e = getSignalMast().getAppearanceMap().getAspects();
        boolean error = false;
        while (e.hasMoreElements()) {
            String aspect = e.nextElement();
            error = loadIcons(aspect);
        }
        if (error) {
            JOptionPane.showMessageDialog(_editor.getTargetFrame(),
                    java.text.MessageFormat.format(Bundle.getMessage("SignalMastIconLoadError"),
                            new Object[]{getSignalMast().getDisplayName()}),
                    Bundle.getMessage("SignalMastIconLoadErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
        //Add in specific appearances for dark and held
        loadIcons("$dark");
        loadIcons("$held");
    }

    private boolean loadIcons(String aspect) {
        String s = getSignalMast().getAppearanceMap().getImageLink(aspect, useIconSet);
        if (s.equals("")) {
            if (aspect.startsWith("$")) {
                log.debug("No icon found for specific appearance " + aspect);
            } else {
                log.error("No icon found for appearance " + aspect);
            }
            return true;
        } else {
            if (!s.contains("preference:")) {
                s = s.substring(s.indexOf("resources"));
            }
            NamedIcon n;
            try {
                n = new NamedIcon(s, s);
            } catch (java.lang.NullPointerException e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("SignalMastIconLoadError2", new Object[]{aspect, s, getNameString()}), Bundle.getMessage("SignalMastIconLoadErrorTitle"), JOptionPane.ERROR_MESSAGE);
                log.error(Bundle.getMessage("SignalMastIconLoadError2", aspect, s, getNameString()));
                return true;
            }
            _iconMap.put(s, n);
            if (_rotate != 0) {
                n.rotate(_rotate, this);
            }
            if (_scale != 1.0) {
                n.scale(_scale, this);
            }
        }
        return false;
    }

    public NamedBeanHandle<SignalMast> getNamedSignalMast() {
        return namedMast;
    }

    public SignalMast getSignalMast() {
        if (namedMast == null) {
            return null;
        }
        return namedMast.getBean();
    }

    public jmri.NamedBean getNamedBean() {
        return getSignalMast();
    }

    /**
     * Get current appearance of the mast
     *
     * @return An aspect from the SignalMast
     */
    public String mastState() {
        if (getSignalMast() == null) {
            return "<empty>";
        } else {
            return getSignalMast().getAspect();
        }
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (debug) {
            log.debug("property change: " + e.getPropertyName()
                    + " current state: " + mastState());
        }
        displayState(mastState());
        _editor.getTargetPanel().repaint();
    }

//    public String getPName() { return namedMast.getName(); }
    public String getNameString() {
        String name;
        if (getSignalMast() == null) {
            name = Bundle.getMessage("NotConnected");
        } else if (getSignalMast().getUserName() == null) {
            name = getSignalMast().getSystemName();
        } else {
            name = getSignalMast().getUserName() + " (" + getSignalMast().getSystemName() + ")";
        }
        return name;
    }

    ButtonGroup litButtonGroup = null;

    /**
     * Pop-up just displays the name
     */
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {

            JMenu clickMenu = new JMenu(Bundle.getMessage("WhenClicked"));
            ButtonGroup clickButtonGroup = new ButtonGroup();
            JRadioButtonMenuItem r;
            r = new JRadioButtonMenuItem(Bundle.getMessage("ChangeAspect"));
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setClickMode(0);
                }
            });
            clickButtonGroup.add(r);
            if (clickMode == 0) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            clickMenu.add(r);

            r = new JRadioButtonMenuItem(Bundle.getMessage("AlternateLit"));
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setClickMode(1);
                }
            });
            clickButtonGroup.add(r);
            if (clickMode == 1) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            clickMenu.add(r);
            r = new JRadioButtonMenuItem(Bundle.getMessage("AlternateHeld"));
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setClickMode(2);
                }
            });
            clickButtonGroup.add(r);
            if (clickMode == 2) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            clickMenu.add(r);
            popup.add(clickMenu);

            // add menu to select handling of lit parameter
            JMenu litMenu = new JMenu(Bundle.getMessage("WhenNotLit"));
            litButtonGroup = new ButtonGroup();
            r = new JRadioButtonMenuItem(Bundle.getMessage("ShowAppearance"));
            r.setIconTextGap(10);
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setLitMode(false);
                    displayState(mastState());
                }
            });
            litButtonGroup.add(r);
            if (!litMode) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            litMenu.add(r);
            r = new JRadioButtonMenuItem(Bundle.getMessage("ShowDarkIcon"));
            r.setIconTextGap(10);
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setLitMode(true);
                    displayState(mastState());
                }
            });
            litButtonGroup.add(r);
            if (litMode) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            litMenu.add(r);
            popup.add(litMenu);

            java.util.Enumeration<String> en = getSignalMast().getSignalSystem().getImageTypeList();
            if (en.hasMoreElements()) {
                JMenu iconSetMenu = new JMenu(Bundle.getMessage("SignalMastIconSet"));
                ButtonGroup iconTypeGroup = new ButtonGroup();
                setImageTypeList(iconTypeGroup, iconSetMenu, "default");
                while (en.hasMoreElements()) {
                    setImageTypeList(iconTypeGroup, iconSetMenu, en.nextElement());
                }
                popup.add(iconSetMenu);
            }
            popup.add(new jmri.jmrit.signalling.SignallingSourceAction(Bundle.getMessage("SignalMastLogic"), getSignalMast()));
            JMenu aspect = new JMenu(Bundle.getMessage("ChangeAspect"));
            final java.util.Vector<String> aspects = getSignalMast().getValidAspects();
            for (int i = 0; i < aspects.size(); i++) {
                final int index = i;
                aspect.add(new AbstractAction(aspects.elementAt(index)) {
                    /**
                     *
                     */
                    private static final long serialVersionUID = -2880512365482915995L;

                    public void actionPerformed(ActionEvent e) {
                        getSignalMast().setAspect(aspects.elementAt(index));
                    }
                });
            }
            popup.add(aspect);
            addTransitPopup(popup);
        } else {
            final java.util.Vector<String> aspects = getSignalMast().getValidAspects();
            for (int i = 0; i < aspects.size(); i++) {
                final int index = i;
                popup.add(new AbstractAction(aspects.elementAt(index)) {
                    /**
                     *
                     */
                    private static final long serialVersionUID = -5139716882196677458L;

                    public void actionPerformed(ActionEvent e) {
                        getSignalMast().setAspect(aspects.elementAt(index));
                    }
                });
            }
        }
        return true;
    }

    private void addTransitPopup(JPopupMenu popup) {
        if ((InstanceManager.sectionManagerInstance().getSystemNameList().size()) > 0
                && jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).isAdvancedRoutingEnabled()) {

            if (tct == null) {
                tct = new jmri.jmrit.display.layoutEditor.TransitCreationTool();
            }
            popup.addSeparator();
            String addString = Bundle.getMessage("MenuTransitCreate");
            if (tct.isToolInUse()) {
                addString = Bundle.getMessage("MenuTransitAddTo");
            }
            popup.add(new AbstractAction(addString) {
                /**
                 *
                 */
                private static final long serialVersionUID = 8999105169303183319L;

                public void actionPerformed(ActionEvent e) {
                    try {
                        tct.addNamedBean(getSignalMast());
                    } catch (jmri.JmriException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("TransitErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            if (tct.isToolInUse()) {
                popup.add(new AbstractAction(Bundle.getMessage("MenuTransitAddComplete")) {
                    /**
                     *
                     */
                    private static final long serialVersionUID = 4260649479001420722L;

                    public void actionPerformed(ActionEvent e) {
                        Transit created;
                        try {
                            tct.addNamedBean(getSignalMast());
                            created = tct.createTransit();
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("TransitCreatedMessage", created.getDisplayName()), Bundle.getMessage("TransitCreatedTitle"), JOptionPane.INFORMATION_MESSAGE);
                        } catch (jmri.JmriException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("TransitErrorTitle"), JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                });
                popup.add(new AbstractAction(Bundle.getMessage("MenuTransitCancel")) {
                    /**
                     *
                     */
                    private static final long serialVersionUID = -388638543659271424L;

                    public void actionPerformed(ActionEvent e) {
                        tct.cancelTransitCreate();
                    }
                });
            }
            popup.addSeparator();
        }
    }

    static jmri.jmrit.display.layoutEditor.TransitCreationTool tct;

    private void setImageTypeList(ButtonGroup iconTypeGroup, JMenu iconSetMenu, final String item) {
        JRadioButtonMenuItem im;
        im = new JRadioButtonMenuItem(item);
        im.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                useIconSet(item);
            }
        });
        iconTypeGroup.add(im);
        if (useIconSet.equals(item)) {
            im.setSelected(true);
        } else {
            im.setSelected(false);
        }
        iconSetMenu.add(im);

    }

    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {
        return false;
    }

    SignalMastItemPanel _itemPanel;

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("SignalMast"));
        popup.add(new AbstractAction(txt) {
            /**
             *
             */
            private static final long serialVersionUID = -4184564062800372222L;

            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });
        return true;
    }

    protected void editItem() {
        makePalettteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("SignalMast")));
        _itemPanel = new SignalMastItemPanel(_paletteFrame, "SignalMast", getFamily(),
                PickListModel.signalMastPickModelInstance(), _editor);
        ActionListener updateAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // _iconMap keys with local names - Let SignalHeadItemPanel figure this out
        _itemPanel.init(updateAction, _iconMap);
        _itemPanel.setSelection(getSignalMast());
        _paletteFrame.add(_itemPanel);
        _paletteFrame.pack();
        _paletteFrame.setVisible(true);
    }

    void updateItem() {
        setSignalMast(_itemPanel.getTableSelection().getSystemName());
        setFamily(_itemPanel.getFamilyName());
        _paletteFrame.dispose();
        _paletteFrame = null;
        _itemPanel.dispose();
        _itemPanel = null;
        invalidate();
    }

    /**
     * Change the SignalMast aspect when the icon is clicked.
     *
     * @param e
     */
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
            return;
        }
        performMouseClicked(e);
    }

    /**
     * This was added in so that the layout editor can handle the mouseclicked
     * when zoomed in
     */
    public void performMouseClicked(java.awt.event.MouseEvent e) {
        if (e.isMetaDown() || e.isAltDown()) {
            return;
        }
        if (getSignalMast() == null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        switch (clickMode) {
            case 0:
                java.util.Vector<String> aspects = getSignalMast().getValidAspects();
                int idx = aspects.indexOf(getSignalMast().getAspect()) + 1;
                if (idx >= aspects.size()) {
                    idx = 0;
                }
                getSignalMast().setAspect(aspects.elementAt(idx));
                return;
            case 1:
                getSignalMast().setLit(!getSignalMast().getLit());
                return;
            case 2:
                getSignalMast().setHeld(!getSignalMast().getHeld());
                return;
            default:
                log.error("Click in mode " + clickMode);
        }
    }

    String useIconSet = "default";

    public void useIconSet(String icon) {
        if (icon == null) {
            icon = "default";
        }
        if (useIconSet.equals(icon)) {
            return;
        }
        //clear the old icon map out.
        _iconMap = null;
        useIconSet = icon;
        getIcons();
        displayState(mastState());
        _editor.getTargetPanel().repaint();
    }

    public String useIconSet() {
        return useIconSet;
    }

    /**
     * Set display of ClipBoard copied or duplicated mast
     */
    public void displayState(int s) {
        displayState(mastState());
    }

    /**
     * Drive the current state of the display from the state of the underlying
     * SignalMast object.
     */
    public void displayState(String state) {
        updateSize();
        if (debug) {
            if (getSignalMast() == null) {
                log.debug("Display state " + state + ", disconnected");
            } else {
                log.debug("Display state " + state + " for " + getSignalMast().getSystemName());
            }
        }
        if (isText()) {
            if (getSignalMast().getHeld()) {
                if (isText()) {
                    super.setText(Bundle.getMessage("Held"));
                }
                return;
            } else if (getLitMode() && !getSignalMast().getLit()) {
                super.setText(Bundle.getMessage("Dark"));
                return;
            }
            super.setText(state);
        }
        if (isIcon()) {
            if ((state != null) && (getSignalMast() != null)) {
                String s = getSignalMast().getAppearanceMap().getImageLink(state, useIconSet);
                if ((getSignalMast().getHeld()) && (getSignalMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD) != null)) {
                    s = getSignalMast().getAppearanceMap().getImageLink("$held", useIconSet);
                } else if (getLitMode() && !getSignalMast().getLit() && (getSignalMast().getAppearanceMap().getImageLink("$dark", useIconSet) != null)) {
                    s = getSignalMast().getAppearanceMap().getImageLink("$dark", useIconSet);
                }
                if (s.equals("")) {
                    /*We have no appearance to set, therefore we will exit at this point.
                     This can be considered normal if we are requesting an appearance 
                     that is not support or configured, such as dark or held */
                    return;
                }
                if (!s.contains("preference:")) {
                    s = s.substring(s.indexOf("resources"));
                }

                // tiny global cache, due to number of icons
                if (_iconMap == null) {
                    getIcons();
                }
                NamedIcon n = _iconMap.get(s);
                super.setIcon(n);
                updateSize();
                setSize(n.getIconWidth(), n.getIconHeight());
            }
        } else {
            super.setIcon(null);
        }
        return;
    }

    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }

    protected void rotateOrthogonal() {
        super.rotateOrthogonal();
        // bug fix, must repaint icons that have same width and height
        displayState(mastState());
        repaint();
    }

    public void rotate(int deg) {
        super.rotate(deg);
        if (getSignalMast() != null) {
            displayState(mastState());
        }
    }

    public void setScale(double s) {
        super.setScale(s);
        if (getSignalMast() != null) {
            displayState(mastState());
        }
    }

    /**
     * What to do on click? 0 means sequence through aspects; 1 means alternate
     * the "lit" aspect; 2 means alternate the
     * {@link jmri.SignalAppearanceMap#HELD} aspect.
     */
    protected int clickMode = 0;

    public void setClickMode(int mode) {
        clickMode = mode;
    }

    public int getClickMode() {
        return clickMode;
    }

    /**
     * How to handle lit vs not lit?
     * <P>
     * False means ignore (always show R/Y/G/etc appearance on screen); True
     * means show {@link jmri.SignalAppearanceMap#DARK} if lit is set false.
     */
    protected boolean litMode = false;

    public void setLitMode(boolean mode) {
        litMode = mode;
    }

    public boolean getLitMode() {
        return litMode;
    }

    public void dispose() {
        getSignalMast().removePropertyChangeListener(this);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SignalMastIcon.class.getName());
}
