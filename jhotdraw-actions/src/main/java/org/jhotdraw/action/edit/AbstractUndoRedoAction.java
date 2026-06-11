package org.jhotdraw.action.edit;

import java.beans.*;
import javax.swing.*;
import org.jhotdraw.action.AbstractViewAction;
import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;
import org.jhotdraw.util.*;

/**
 * Abstract base class for UndoAction and RedoAction.
 * Eliminates duplicated code by extracting shared logic
 * into this parent class.
 */
public abstract class AbstractUndoRedoAction extends AbstractViewAction {

    private static final long serialVersionUID = 1L;
    protected ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.action.Labels");

    private final transient PropertyChangeListener actionPropertyListener = evt -> {
        String name = evt.getPropertyName();
        if (Action.NAME.equals(name)) {
            putValue(Action.NAME, evt.getNewValue());
        } else if ("enabled".equals(name)) {
            updateEnabledState();
        }
    };

    protected AbstractUndoRedoAction(Application app, View view) {
        super(app, view);
    }

    protected void updateEnabledState() {
        boolean isEnabled = false;
        Action realAction = getRealAction();
        if (realAction != null && realAction != this) {
            isEnabled = realAction.isEnabled();
        }
        setEnabled(isEnabled);
    }

    @Override
    protected void updateView(View oldValue, View newValue) {
        super.updateView(oldValue, newValue);
        if (newValue != null
                && newValue.getActionMap().get(getID()) != null
                && newValue.getActionMap().get(getID()) != this) {
            putValue(Action.NAME, newValue.getActionMap().get(getID())
                    .getValue(Action.NAME));
            updateEnabledState();
        }
    }

    @Override
    protected void installViewListeners(View p) {
        super.installViewListeners(p);
        Action actionInView = p.getActionMap().get(getID());
        if (actionInView != null && actionInView != this) {
            actionInView.addPropertyChangeListener(actionPropertyListener);
        }
    }

    @Override
    protected void uninstallViewListeners(View p) {
        super.uninstallViewListeners(p);
        Action actionInView = p.getActionMap().get(getID());
        if (actionInView != null && actionInView != this) {
            actionInView.removePropertyChangeListener(actionPropertyListener);
        }
    }

    protected Action getRealAction() {
        return (getActiveView() == null) ? null : getActiveView().getActionMap().get(getID());
    }

    protected abstract String getID();
}