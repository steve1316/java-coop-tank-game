import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class PowerUp extends GameObject
{
    BufferedImage powerUpImage;

    public PowerUp(double x, double y, BufferedImage power)
    {
        super(x, y);
        this.powerUpImage = power;
    }

    public void render(Graphics g)
    {
        g.drawImage(powerUpImage, (int) x, (int) y, null);
    }

    public void renderMiniMap(Graphics g, int scaleDown)
    {
        BufferedImage before = powerUpImage;
        int w = before.getWidth();
        int h = before.getHeight();
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale((double) 1 / scaleDown, (double) 1 / scaleDown);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(before, after);
        g.drawImage(after, (int) x/scaleDown, (int) y /scaleDown, null);
    }
}
