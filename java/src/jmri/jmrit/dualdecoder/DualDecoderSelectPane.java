// DualDecoderSelectPane.java
package jmri.jmrit.dualdecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.*;

/**
 * Pane for selecting an active decoder from multiple ones in a loco
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class DualDecoderSelectPane extends javax.swing.JPanel implements jmri.ProgListener {

    /**
     *
     */
    private static final long serialVersionUID = 1032342693614263212L;
    boolean scanning = false;
    int next = 0;

    final int NENTRIES = 8;

    JLabel[] labels = new JLabel[NENTRIES];
    JRadioButton[] buttons = new JRadioButton[NENTRIES];

    JLabel status = new JLabel("Idle");
    JToggleButton searchButton = new JToggleButton("Search");
    jmri.jmrit.progsupport.ProgModePane modePane = new jmri.jmrit.progsupport.ProgModePane(BoxLayout.Y_AXIS);

    public DualDecoderSelectPane() {
        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        ButtonGroup g = new ButtonGroup();
        JPanel pane1 = new JPanel();
        pane1.setLayout(new GridLayout(NENTRIES, 1));
        for (int i = 0; i < NENTRIES; i++) {
            JPanel p = new JPanel();
            String name = "ID number " + i;
            if (i == NENTRIES - 1) {
                name = "   Legacy ";
            }
            p.add(labels[i] = new JLabel(name));
            JRadioButton b = new JRadioButton();
            buttons[i] = b;
            b.setActionCommand("" + i);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    select(e.getActionCommand());
                }
            });
            p.add(b);
            g.add(b);
            pane1.add(p);
        }
        add(pane1);
        add(new JSeparator(JSeparator.HORIZONTAL));

        JPanel pane2 = new JPanel();
        JButton t;
        pane2.add(searchButton);
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });
        pane2.add(t = new JButton("Reset"));
        t.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });
        add(pane2);

        JPanel pane3 = new JPanel();
        pane3.add(t = new JButton("Init DH163 + Soundtraxx"));
        t.setToolTipText("This will configure a loco contains a DH163 and a decoder not supporting the proposed protocol");
        t.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doInit();
            }
        });
        add(pane3);
        add(new JSeparator(JSeparator.HORIZONTAL));

        JPanel pane4 = new JPanel();
        pane4.add(status);
        add(pane4);
        add(new JSeparator(JSeparator.HORIZONTAL));

        add(modePane);
    }

    public void dispose() {
        modePane.dispose();
        buttons = null;
        labels = null;
        status = null;
        searchButton = null;
    }

    void reset() {
        for (int i = 0; i < NENTRIES; i++) {
            labels[i].setEnabled(true);
            buttons[i].setSelected(false);
            buttons[i].setEnabled(true);
        }
    }

    void search() {
        mode = SEARCH;
        reset();
        searchButton.setSelected(true);
        scanning = true;
        next = 0;
        select("0");
    }

    void select(String number) {
        mode = SEARCH;
        next = Integer.parseInt(number);
        // write that CV value
        state = WROTECV15;
        writeCV15(next);
    }

    void writeCV15(int value) {
        writeCV(15, value);
    }

    void writeCV16(int value) {
        writeCV(16, value);
    }

    void writeCV(int cv, int value) {
        Programmer p = modePane.getProgrammer();
        if (p == null) {
            state = IDLE;
            status.setText("No programmer connected");
        } else {
            try {
                status.setText("writing...");
                p.writeCV(cv, value, this);
            } catch (jmri.ProgrammerException ex) {
                state = IDLE;
                status.setText("" + ex);
            }
        }
    }

    void readCV16() {
        Programmer p = modePane.getProgrammer();
        if (p == null) {
            state = IDLE;
            status.setText("No programmer connected");
        } else {
            try {
                status.setText("reading...");
                state = READCV16;
                p.readCV(16, this);
            } catch (jmri.ProgrammerException ex) {
                state = IDLE;
                status.setText("" + ex);
            }
        }
    }

    // modes
    final int SEARCH = 10;
    final int INIT = 20;
    int mode = SEARCH;

    // search, select operation states
    final int IDLE = 0;
    final int WROTECV15 = 1;
    final int READCV16 = 2;
    final int FIRSTCV16 = 11;
    final int FIRSTCV15 = 12;
    final int SECONDCV16 = 13;
    int state = IDLE;

    public void programmingOpReply(int value, int retcode) {
        switch (mode) {
            case SEARCH:
                searchReply(value, retcode);
                break;
            case INIT:
                initReply(value, retcode);
                break;
            default:
                log.warn("unexpected mode: " + mode);
                break;
        }
    }

    void searchReply(int value, int retcode) {
        switch (state) {
            case IDLE:
            default:
                // shouldn't happen, reset and ignore
                log.warn("Unexpected search programming reply: " + value + " " + retcode);
                state = IDLE;
                break;
            case WROTECV15:
                //confirm OK
                readCV16();
                break;
            case READCV16:
                // was it OK?
                String result = "OK";
                if (retcode != ProgListener.OK) {
                    log.debug("Readback error: " + retcode + " " + value);
                    labels[next].setEnabled(false);
                    buttons[next].setEnabled(false);
                    result = "Could not confirm: " + modePane.getProgrammer().decodeErrorCode(retcode);
                } else if (value != next) {
                    log.debug("Readback error: " + retcode + " " + value);
                    if (scanning) {
                        labels[next].setEnabled(false);
                        buttons[next].setEnabled(false);
                    }
                    result = "Unexpected ID read: " + value;
                }
                // go on to next?
                if (scanning) {
                    next++;
                    if (next >= NENTRIES) {
                        state = IDLE;
                        next = 0;
                        scanning = false;
                        status.setText("Idle");
                        searchButton.setSelected(false);
                        break;
                    }
                    select("" + next);
                    break;
                } else {
                    status.setText(result);
                    break;
                }
        }
    }

    /**
     * Start process of initializing a Digitrax and legacy decoder. Operations
     * are:
     * <ol>
     * <li>Write 1 to CV16, which will write both decoders
     * <li>Write 7 to CV15, which will turn off Digitrax
     * <LI>Write 7 to CV16, which will be stored in the legacy decoder only
     * </ol>
     */
    void doInit() {
        mode = INIT;
        state = FIRSTCV16;
        writeCV16(1);
    }

    void initReply(int value, int retcode) {
        switch (state) {
            case IDLE:
            default:
                // shouldn't happen, reset and ignore
                log.warn("Unexpected init programming reply: " + value + " " + retcode);
                state = IDLE;
                break;
            case FIRSTCV16:
                state = FIRSTCV15;
                if (retcode != ProgListener.OK) {
                    log.debug("Readback error: " + retcode + " " + value);
                    status.setText("Write CV15=7 failed!");
                    state = IDLE;
                } else { // is OK
                    writeCV15(7);
                }
                break;
            case FIRSTCV15:
                state = SECONDCV16;
                if (retcode != ProgListener.OK) {
                    log.debug("Readback error: " + retcode + " " + value);
                    status.setText("Write CV16=7 failed!");
                    state = IDLE;
                } else { // is OK
                    writeCV16(7);
                }
                break;
            case SECONDCV16:
                if (retcode != ProgListener.OK) {
                    log.debug("Readback error: " + retcode + " " + value);
                    status.setText("Write CV16=1 failed!");
                    state = IDLE;
                } else { // is OK
                    state = IDLE;
                    status.setText("Initialized");
                }
                break;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DualDecoderSelectPane.class.getName());

}
