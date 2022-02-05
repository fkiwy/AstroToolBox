package astro.tool.box.shape;

import java.awt.Graphics;

public interface Drawable {

    static float STROKE_WIDTH = 2;

    void draw(Graphics graphics);

}
