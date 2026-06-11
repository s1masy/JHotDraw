package org.jhotdraw.undo;

import com.tngtech.jgiven.annotation.*;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;
import javax.swing.undo.*;
import static org.junit.Assert.*;

public class UndoRedoScenarioTest extends
        ScenarioTest<UndoRedoScenarioTest.GivenStage,
                UndoRedoScenarioTest.WhenStage,
                UndoRedoScenarioTest.ThenStage> {

    // ─── TESTABLE EDIT ────────────────────────────────────────────────────────

    static class TestableEdit extends AbstractUndoableEdit {
        private final String name;
        private boolean undoCalled = false;
        private boolean redoCalled = false;

        public TestableEdit(String name) {
            this.name = name;
        }

        @Override
        public String getPresentationName() { return name; }

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

    static class EditPair {
        final TestableEdit first;
        final TestableEdit second;

        EditPair(TestableEdit first, TestableEdit second) {
            this.first = first;
            this.second = second;
        }
    }

    // ─── GIVEN ───────────────────────────────────────────────────────────────

    public static class GivenStage extends com.tngtech.jgiven.Stage<GivenStage> {

        @ProvidedScenarioState
        private UndoRedoManager manager;

        @ProvidedScenarioState
        private TestableEdit edit;

        @ProvidedScenarioState
        private EditPair editPair;

        @BeforeScenario
        public void setUp() {
            manager = new UndoRedoManager();
        }

        public GivenStage I_have_not_drawn_anything() {
            edit = new TestableEdit("Draw Shape");
            return self();
        }

        public GivenStage I_have_drawn_a_shape_on_the_canvas() {
            edit = new TestableEdit("Draw Shape");
            manager.addEdit(edit);
            return self();
        }

        public GivenStage I_have_drawn_two_shapes_on_the_canvas() {
            TestableEdit e1 = new TestableEdit("Draw Shape 1");
            TestableEdit e2 = new TestableEdit("Draw Shape 2");
            editPair = new EditPair(e1, e2);
            manager.addEdit(e1);
            manager.addEdit(e2);
            return self();
        }

        public GivenStage I_have_drawn_a_shape_and_undone_the_action()
                throws CannotUndoException {
            edit = new TestableEdit("Draw Shape");
            manager.addEdit(edit);
            manager.undo();
            return self();
        }

        public GivenStage I_have_not_performed_any_undo() {
            return I_have_drawn_a_shape_on_the_canvas();
        }
    }

    // ─── WHEN ─────────────────────────────────────────────────────────────────

    public static class WhenStage extends com.tngtech.jgiven.Stage<WhenStage> {

        @ExpectedScenarioState
        private UndoRedoManager manager;

        public WhenStage I_press_Ctrl_Z() throws CannotUndoException {
            manager.undo();
            return self();
        }

        public WhenStage I_press_Ctrl_Z_twice() throws CannotUndoException {
            manager.undo();
            manager.undo();
            return self();
        }

        public WhenStage I_press_Ctrl_Y() throws CannotRedoException {
            manager.redo();
            return self();
        }

        public WhenStage I_try_to_undo() {
            return self();
        }

        public WhenStage I_try_to_redo() {
            return self();
        }

        public WhenStage I_draw_a_new_shape() {
            TestableEdit newEdit = new TestableEdit("New Shape");
            manager.addEdit(newEdit);
            return self();
        }
    }

    // ─── THEN ─────────────────────────────────────────────────────────────────

    public static class ThenStage extends com.tngtech.jgiven.Stage<ThenStage> {

        @ExpectedScenarioState
        private UndoRedoManager manager;

        @ExpectedScenarioState
        private TestableEdit edit;

        @ExpectedScenarioState
        private EditPair editPair;

        public ThenStage the_shape_should_be_removed_from_the_canvas() {
            assertTrue("edit undo should have been called", edit.wasUndoCalled());
            return self();
        }

        public ThenStage the_redo_action_should_become_available() {
            assertTrue("redo should be available after undo", manager.canRedo());
            return self();
        }

        public ThenStage the_shape_should_reappear_on_the_canvas() {
            assertTrue("edit redo should have been called", edit.wasRedoCalled());
            return self();
        }

        public ThenStage the_undo_action_should_become_available() {
            assertTrue("undo should be available after redo", manager.canUndo());
            return self();
        }

        public ThenStage the_undo_action_should_be_disabled() {
            assertFalse("undo should be disabled", manager.canUndo());
            return self();
        }

        public ThenStage the_redo_action_should_be_disabled() {
            assertFalse("redo should be disabled", manager.canRedo());
            return self();
        }

        public ThenStage both_shapes_should_be_removed_in_reverse_order() {
            assertTrue("second shape should be undone first",
                    editPair.second.wasUndoCalled());
            assertTrue("first shape should be undone second",
                    editPair.first.wasUndoCalled());
            return self();
        }

        public ThenStage undo_should_be_disabled() {
            assertFalse("undo should be disabled after undoing only edit",
                    manager.canUndo());
            return self();
        }

        public ThenStage redo_should_be_enabled() {
            assertTrue("redo should be enabled after undo", manager.canRedo());
            return self();
        }
    }

    // ─── SCENARIOS ────────────────────────────────────────────────────────────

    @Test
    public void undo_a_drawing_action() throws Exception {
        given().I_have_drawn_a_shape_on_the_canvas();
        when().I_press_Ctrl_Z();
        then().the_shape_should_be_removed_from_the_canvas()
                .and().the_redo_action_should_become_available();
    }

    @Test
    public void redo_an_undone_action() throws Exception {
        given().I_have_drawn_a_shape_and_undone_the_action();
        when().I_press_Ctrl_Y();
        then().the_shape_should_reappear_on_the_canvas()
                .and().the_undo_action_should_become_available();
    }

    @Test
    public void cannot_undo_with_empty_stack() {
        given().I_have_not_drawn_anything();
        when().I_try_to_undo();
        then().the_undo_action_should_be_disabled();
    }

    @Test
    public void multiple_undos_in_reverse_order() throws Exception {
        given().I_have_drawn_two_shapes_on_the_canvas();
        when().I_press_Ctrl_Z_twice();
        then().both_shapes_should_be_removed_in_reverse_order();
    }

    @Test
    public void redo_is_cleared_after_new_action() throws Exception {
        given().I_have_drawn_a_shape_and_undone_the_action();
        when().I_draw_a_new_shape();
        then().the_redo_action_should_be_disabled();
    }

    @Test
    public void undo_disables_itself_when_stack_is_empty() throws Exception {
        given().I_have_drawn_a_shape_on_the_canvas();
        when().I_press_Ctrl_Z();
        then().undo_should_be_disabled()
                .and().redo_should_be_enabled();
    }

    @Test
    public void cannot_redo_with_empty_redo_stack() {
        given().I_have_not_performed_any_undo();
        when().I_try_to_redo();
        then().the_redo_action_should_be_disabled();
    }
}