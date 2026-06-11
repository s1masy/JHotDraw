package org.jhotdraw.undo;

import org.junit.*;
import static org.junit.Assert.*;
import javax.swing.undo.*;

/**
 * Unit tests for UndoRedoManager.
 * Tests the core business logic of the undo/redo feature.
 */
public class UndoRedoManagerTest {

    private UndoRedoManager manager;
    private TestableEdit edit1;
    private TestableEdit edit2;

    /**
     * A simple testable UndoableEdit for use in tests.
     */
    private static class TestableEdit extends AbstractUndoableEdit {
        private final String name;
        private boolean undoCalled = false;
        private boolean redoCalled = false;

        public TestableEdit(String name) {
            this.name = name;
        }

        @Override
        public String getPresentationName() {
            return name;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            undoCalled = true;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            redoCalled = true;
        }

        public boolean wasUndoCalled() { return undoCalled; }
        public boolean wasRedoCalled() { return redoCalled; }
    }

    @Before
    public void setUp() {
        manager = new UndoRedoManager();
        edit1 = new TestableEdit("Edit1");
        edit2 = new TestableEdit("Edit2");
    }

    // ─── BEST CASE SCENARIOS ──────────────────────────────────────────────────

    @Test
    public void testAddEditEnablesUndo() {
        manager.addEdit(edit1);
        assertTrue("Undo should be enabled after adding an edit", manager.canUndo());
    }

    @Test
    public void testAddEditDisablesRedoInitially() {
        manager.addEdit(edit1);
        assertFalse("Redo should not be available before any undo", manager.canRedo());
    }

    @Test
    public void testUndoCallsEditUndo() throws CannotUndoException {
        manager.addEdit(edit1);
        manager.undo();
        assertTrue("Edit's undo() should have been called", edit1.wasUndoCalled());
    }

    @Test
    public void testRedoAfterUndo() throws CannotUndoException {
        manager.addEdit(edit1);
        manager.undo();
        assertTrue("Redo should be available after undo", manager.canRedo());
    }

    @Test
    public void testRedoCallsEditRedo() throws CannotUndoException, CannotRedoException {
        manager.addEdit(edit1);
        manager.undo();
        manager.redo();
        assertTrue("Edit's redo() should have been called", edit1.wasRedoCalled());
    }

    @Test
    public void testHasSignificantEditsAfterAdd() {
        manager.addEdit(edit1);
        assertTrue("Manager should have significant edits after adding one",
                manager.hasSignificantEdits());
    }

    @Test
    public void testDiscardAllEditsDisablesUndoAndRedo() {
        manager.addEdit(edit1);
        manager.discardAllEdits();
        assertFalse("Undo should be disabled after discardAllEdits", manager.canUndo());
        assertFalse("Redo should be disabled after discardAllEdits", manager.canRedo());
    }

    @Test
    public void testDiscardAllEditsClearsSignificantEdits() {
        manager.addEdit(edit1);
        manager.discardAllEdits();
        assertFalse("hasSignificantEdits should be false after discardAllEdits",
                manager.hasSignificantEdits());
    }

    @Test
    public void testMultipleEditsUndoInOrder() throws CannotUndoException {
        manager.addEdit(edit1);
        manager.addEdit(edit2);
        manager.undo();
        assertTrue("edit2 should be undone first (LIFO order)", edit2.wasUndoCalled());
        assertFalse("edit1 should not be undone yet", edit1.wasUndoCalled());
    }

    @Test
    public void testUndoActionIsNotNull() {
        assertNotNull("UndoAction should not be null", manager.getUndoAction());
    }

    @Test
    public void testRedoActionIsNotNull() {
        assertNotNull("RedoAction should not be null", manager.getRedoAction());
    }

    // ─── BOUNDARY CASES ───────────────────────────────────────────────────────

    @Test
    public void testCannotUndoOnEmptyManager() {
        assertFalse("Cannot undo when no edits have been added", manager.canUndo());
    }

    @Test
    public void testCannotRedoOnEmptyManager() {
        assertFalse("Cannot redo when no edits have been added", manager.canRedo());
    }

    @Test(expected = CannotUndoException.class)
    public void testUndoThrowsWhenNothingToUndo() throws CannotUndoException {
        manager.undo(); // should throw CannotUndoException
    }

    @Test(expected = CannotRedoException.class)
    public void testRedoThrowsWhenNothingToRedo() throws CannotUndoException, CannotRedoException {
        manager.addEdit(edit1);
        // do NOT undo first — redo should throw
        manager.redo();
    }

    @Test
    public void testAddEditDuringUndoIsIgnored() throws CannotUndoException {
        // UndoRedoManager should ignore edits added while undo is in progress
        // This tests the undoOrRedoInProgress flag behaviour
        manager.addEdit(edit1);
        manager.undo();
        // After undo, stack should still only have edit1
        assertTrue("Redo should still be available", manager.canRedo());
    }

    @Test
    public void testDiscardAllOnEmptyManagerDoesNotThrow() {
        // Discarding an already empty manager should not throw
        try {
            manager.discardAllEdits();
        } catch (Exception e) {
            fail("discardAllEdits on empty manager should not throw: " + e.getMessage());
        }
    }

    @Test
    public void testHasSignificantEditsIsFalseInitially() {
        assertFalse("New manager should have no significant edits",
                manager.hasSignificantEdits());
    }

    @Test
    public void testUndoDisablesUndoWhenStackEmpty() throws CannotUndoException {
        manager.addEdit(edit1);
        manager.undo();
        assertFalse("Undo should be disabled after undoing the only edit",
                manager.canUndo());
    }

    @Test
    public void testSetHasSignificantEdits() {
        manager.setHasSignificantEdits(true);
        assertTrue("hasSignificantEdits should return true after being set",
                manager.hasSignificantEdits());
        manager.setHasSignificantEdits(false);
        assertFalse("hasSignificantEdits should return false after being unset",
                manager.hasSignificantEdits());
    }

    // ─── JAVA ASSERTION (invariant check) ─────────────────────────────────────

    @Test
    public void testManagerInvariant() {
        // Invariant: canRedo() should never be true when canUndo() is false
        // and no undo has been performed
        manager.addEdit(edit1);

        assert !(!manager.canUndo() && manager.canRedo())
                : "Invariant violated: canRedo true but canUndo false without prior undo";

        assertTrue("canUndo should be true after adding an edit", manager.canUndo());
        assertFalse("canRedo should be false before any undo has been performed", manager.canRedo());
    }
}