package org.jhotdraw.action.edit;

import java.awt.event.*;
import javax.swing.*;
import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;

/**
 * Undoes the last user action.
 */
public class UndoAction extends AbstractUndoRedoAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.undo";

    public UndoAction(Application app, View view) {
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