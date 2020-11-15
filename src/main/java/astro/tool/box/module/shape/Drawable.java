package astro.tool.box.module.shape;

import java.awt.Graphics;

public interface Drawable {

    static int FONT_SIZE = 10;

    static float STROKE_WIDTH = 2;

    void draw(Graphics graphics);

}
