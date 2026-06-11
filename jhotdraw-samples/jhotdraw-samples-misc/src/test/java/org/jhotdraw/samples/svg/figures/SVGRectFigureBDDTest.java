package org.jhotdraw.samples.svg.figures;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

import java.awt.geom.Point2D;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

/**
 * User Story:
 *   As a graphic designer using JHotDraw,
 *   I want to use a rectangle tool,
 *   So that I can create and refine precise rectangular shapes in my drawing.
 */
public class SVGRectFigureBDDTest
        extends ScenarioTest<
            SVGRectFigureBDDTest.GivenARectangleFigure,
            SVGRectFigureBDDTest.WhenTheDesignerInteracts,
            SVGRectFigureBDDTest.ThenTheFigure> {

    public static class GivenARectangleFigure extends Stage<GivenARectangleFigure> {

        @ProvidedScenarioState
        SVGRectFigure rect;

        public GivenARectangleFigure a_new_default_rectangle() {
            rect = new SVGRectFigure();
            return self();
        }

        public GivenARectangleFigure a_rectangle_at_$_$_with_size_$_x_$(
                double x, double y, double w, double h) {
            rect = new SVGRectFigure(x, y, w, h);
            return self();
        }
    }

    public static class WhenTheDesignerInteracts extends Stage<WhenTheDesignerInteracts> {

        @ExpectedScenarioState
        SVGRectFigure rect;

        public WhenTheDesignerInteracts the_designer_drags_from_$_$_to_$_$(
                double x1, double y1, double x2, double y2) {
            rect.setBounds(new Point2D.Double(x1, y1), new Point2D.Double(x2, y2));
            return self();
        }

        public WhenTheDesignerInteracts the_designer_sets_corner_radii_to_$_x_$(
                double arcW, double arcH) {
            rect.setArc(arcW, arcH);
            return self();
        }
    }

    public static class ThenTheFigure extends Stage<ThenTheFigure> {

        @ExpectedScenarioState
        SVGRectFigure rect;

        public ThenTheFigure has_position_$_$(double x, double y) {
            assertThat(rect.getX()).as("x").isEqualTo(x, offset(1e-9));
            assertThat(rect.getY()).as("y").isEqualTo(y, offset(1e-9));
            return self();
        }

        public ThenTheFigure has_size_$_x_$(double w, double h) {
            assertThat(rect.getWidth()).as("width").isEqualTo(w, offset(1e-9));
            assertThat(rect.getHeight()).as("height").isEqualTo(h, offset(1e-9));
            return self();
        }

        public ThenTheFigure has_positive_dimensions() {
            assertThat(rect.getWidth()).as("width > 0").isGreaterThan(0);
            assertThat(rect.getHeight()).as("height > 0").isGreaterThan(0);
            return self();
        }

        public ThenTheFigure has_corner_radii_$_x_$(double arcW, double arcH) {
            assertThat(rect.getArcWidth()).as("arcWidth").isEqualTo(arcW, offset(1e-9));
            assertThat(rect.getArcHeight()).as("arcHeight").isEqualTo(arcH, offset(1e-9));
            return self();
        }
    }

    // Scenarios

    /**
     * Given a new default rectangle
     * When the designer drags from (10, 20) to (110, 70)
     * Then the figure has position (10, 20) and size 100 x 50
     */

    @Test
    public void creating_a_rectangle_by_dragging() {
        given().a_new_default_rectangle();
        when().the_designer_drags_from_$_$_to_$_$(10, 20, 110, 70);
        then().has_position_$_$(10, 20)
              .and().has_size_$_x_$(100, 50);
    }

    /**
     * Given  a new default rectangle
     * When the designer drags from (110, 70) to (10, 20)
     * Then the figure has positive dimensions
     */

    @Test
    public void dragging_in_reverse_still_produces_a_valid_rectangle() {
        given().a_new_default_rectangle();
        when().the_designer_drags_from_$_$_to_$_$(110, 70, 10, 20);
        then().has_positive_dimensions();
    }

    /**
     * Given a rectangle at (0, 0) with size 200 x 100
     * When the designer sets corner radii to 20 x 15
     * Then the figure has corner radii 20 x 15
     */

    @Test
    public void rounding_the_corners_of_a_rectangle() {
        given().a_rectangle_at_$_$_with_size_$_x_$(0, 0, 200, 100);
        when().the_designer_sets_corner_radii_to_$_x_$(20, 15);
        then().has_corner_radii_$_x_$(20, 15);
    }
}
