package org.jhotdraw.action.edit;

import java.awt.event.*;
import javax.swing.*;
import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;

/**
 * Redoes the last user action on the active view.
 */
public class RedoAction extends AbstractUndoRedoAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.redo";

    public RedoAction(Application app, View view) {
        super(app, view);
        labels.configureAction(this, ID);
    }

    @Override
    protected String getID() {
        return ID;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Action realAction = getRealAction();
        if (realAction != null && realAction != this) {
            realAction.actionPerformed(e);
        }
    }
}