package apps.startup;

import apps.StartupModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Randall Wood
 */
class StartupModelCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    protected void setValue(Object value) {
        if (value != null) {
            setText(((StartupModel) value).getName());
        } else {
            // null is rendered as an empty cell.
            setText("");
        }
    }
}
