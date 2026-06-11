package org.jhotdraw.samples.svg.figures;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.awt.geom.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

// Unit tests for the core domain logic of SVGRectFigure

@RunWith(MockitoJUnitRunner.class)
public class SVGRectFigureTest {

    /** Standard 100x50 rectangle at (10,20) used as the shared fixture. */
    private SVGRectFigure rect;

    @Mock
    private PropertyChangeListener mockListener;

    @Before
    public void setUp() {
        rect = new SVGRectFigure(10, 20, 100, 50);
        rect.addPropertyChangeListener(mockListener);
    }

    //region setBounds

    @Test
    public void testSetBounds_normalPoints_updatesGeometry() {
        rect.setBounds(new Point2D.Double(5, 15), new Point2D.Double(55, 65));

        assertEquals(5.0,  rect.getX(),      1e-9);
        assertEquals(15.0, rect.getY(),      1e-9);
        assertEquals(50.0, rect.getWidth(),  1e-9);
        assertEquals(50.0, rect.getHeight(), 1e-9);
    }

    @Test
    public void testSetBounds_reversedPoints_producesPositiveDimensions() {
        rect.setBounds(new Point2D.Double(100, 80), new Point2D.Double(20, 30));

        assertTrue(rect.getWidth()  > 0);
        assertTrue(rect.getHeight() > 0);
        assertEquals(20.0, rect.getX(), 1e-9);
        assertEquals(30.0, rect.getY(), 1e-9);
    }

    @Test
    public void testSetBounds_coincidentPoints_enforcesMinimumSize() {
        Point2D.Double p = new Point2D.Double(50, 50);
        rect.setBounds(p, p);

        assertEquals(0.1, rect.getWidth(),  1e-9);
        assertEquals(0.1, rect.getHeight(), 1e-9);
    }

    //endregion
    //region getBounds

    @Test
    public void testGetBounds_reflectsConstructorValues() {
        Rectangle2D.Double b = rect.getBounds();

        assertEquals(10.0,  b.x,      1e-9);
        assertEquals(20.0,  b.y,      1e-9);
        assertEquals(100.0, b.width,  1e-9);
        assertEquals(50.0,  b.height, 1e-9);
    }

    @Test
    public void testGetBounds_reflectsLatestSetBounds() {
        rect.setBounds(new Point2D.Double(0, 0), new Point2D.Double(200, 80));

        assertEquals(200.0, rect.getBounds().width,  1e-9);
        assertEquals(80.0,  rect.getBounds().height, 1e-9);
    }

    //endregion
    //region isEmpty

    @Test
    public void testIsEmpty_normalRect_returnsFalse() {
        assertFalse(rect.isEmpty());
    }

    @Test
    public void testIsEmpty_zeroSizeDefaultRect_returnsTrue() {
        assertTrue(new SVGRectFigure().isEmpty());
    }

    @Test
    public void testIsEmpty_minimumEnforcedSize_returnsFalse() {
        rect.setBounds(new Point2D.Double(0, 0), new Point2D.Double(0, 0));
        assertFalse(rect.isEmpty());
    }

    //endregion
    //region setArcWidth, setArcHeight, setArc

    @Test
    public void testSetArc_storesBothRadii() {
        rect.setArc(20.0, 12.0);

        assertEquals(20.0, rect.getArcWidth(),  1e-9);
        assertEquals(12.0, rect.getArcHeight(), 1e-9);
    }

    @Test
    public void testSetArcWidth_zero_sharpCorners() {
        rect.setArc(20, 10);
        rect.setArcWidth(0.0);

        assertEquals(0.0, rect.getArcWidth(), 1e-9);
    }

    @Test
    public void testSetArcWidth_equalToRectWidth_stored() {
        rect.setArcWidth(rect.getWidth());

        assertEquals(100.0, rect.getArcWidth(), 1e-9);
    }

    @Test
    public void testSetArcWidth_firesCorrectPropertyChangeEvent() {
        rect.setArcWidth(10.0);

        ArgumentCaptor<PropertyChangeEvent> captor =
                ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(mockListener, atLeastOnce()).propertyChange(captor.capture());

        PropertyChangeEvent event = captor.getAllValues().stream()
                .filter(e -> SVGRectFigure.ARC_WIDTH_PROPERTY.equals(e.getPropertyName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No arcWidth event fired"));

        assertEquals(0.0,  (Double) event.getOldValue(), 1e-9);
        assertEquals(10.0, (Double) event.getNewValue(), 1e-9);
    }

    @Test
    public void testSetArcHeight_firesPropertyChangeEvent() {
        rect.setArcHeight(8.0);

        ArgumentCaptor<PropertyChangeEvent> captor =
                ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(mockListener, atLeastOnce()).propertyChange(captor.capture());

        assertTrue(captor.getAllValues().stream()
                .anyMatch(e -> SVGRectFigure.ARC_HEIGHT_PROPERTY.equals(e.getPropertyName())));
    }

    //endregion
    //region clone

    @Test
    public void testClone_equalGeometry() {
        rect.setArc(8, 6);
        SVGRectFigure copy = rect.clone();

        assertEquals(rect.getX(),         copy.getX(),         1e-9);
        assertEquals(rect.getY(),         copy.getY(),         1e-9);
        assertEquals(rect.getWidth(),     copy.getWidth(),     1e-9);
        assertEquals(rect.getHeight(),    copy.getHeight(),    1e-9);
        assertEquals(rect.getArcWidth(),  copy.getArcWidth(),  1e-9);
        assertEquals(rect.getArcHeight(), copy.getArcHeight(), 1e-9);
    }

    @Test
    public void testClone_mutationIndependence() {
        SVGRectFigure copy = rect.clone();
        copy.setArc(99, 99);
        copy.setBounds(new Point2D.Double(0, 0), new Point2D.Double(999, 999));

        assertEquals(10.0,  rect.getX(),       1e-9);
        assertEquals(100.0, rect.getWidth(),    1e-9);
        assertEquals(0.0,   rect.getArcWidth(), 1e-9);
    }

    //endregion
    //region Invariants (Java assert and JUnit assertion)

    @Test
    public void testInvariant_widthNeverNegative() {
        rect.setBounds(new Point2D.Double(100, 100), new Point2D.Double(50, 50));
        double w = rect.getBounds().width;
        assert w >= 0 : "Invariant: width < 0 (" + w + ")";
        assertTrue(w >= 0);
    }

    @Test
    public void testInvariant_heightNeverNegative() {
        rect.setBounds(new Point2D.Double(100, 100), new Point2D.Double(50, 50));
        double h = rect.getBounds().height;
        assert h >= 0 : "Invariant: height < 0 (" + h + ")";
        assertTrue(h >= 0);
    }

    @Test
    public void testInvariant_isEmptyConsistentWithBounds() {
        Rectangle2D.Double b = rect.getBounds();
        if (b.width > 0 && b.height > 0) {
            assert !rect.isEmpty() : "Invariant: positive dimensions but isEmpty() is true";
            assertFalse(rect.isEmpty());
        }
    }

    //endregion
}
