package jmri.util;

/**
 * Simple TransferHandler that exports a string value of a cell in a JTable.
 * <P>
 *
 * @author Pete Cressman Copyright 2010
 * @version $Revision$
 */
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnDTableExportHandler extends TransferHandler {

    /**
     *
     */
    private static final long serialVersionUID = 2134105274438741231L;

    public int getSourceActions(JComponent c) {
        return COPY;
    }

    public Transferable createTransferable(JComponent c) {
        JTable table = (JTable) c;
        int col = table.getSelectedColumn();
        int row = table.getSelectedRow();
        if (col < 0 || row < 0) {
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("TransferHandler.createTransferable: from ("
                    + row + ", " + col + ") for \""
                    + table.getModel().getValueAt(row, col) + "\"");
        }
        Object obj = table.getModel().getValueAt(row, col);
        if (obj instanceof String) {
            return new StringSelection((String) obj);
        } else if (obj != null) {
            return new StringSelection(obj.getClass().getName());
        } else {
            return null;
        }
    }

    public void exportDone(JComponent c, Transferable t, int action) {
        if (log.isDebugEnabled()) {
            log.debug("TransferHandler.exportDone ");
        }
    }
    private final static Logger log = LoggerFactory.getLogger(DnDTableExportHandler.class.getName());
}
