import java.awt.*;
import java.awt.image.BufferedImage;

public class DestructWall extends GameObject
{
    private BufferedImage wall;

    public DestructWall(double x, double y, BufferedImage wall)
    {
        super (x,y);
        this.wall = wall;
    }

    public void render(Graphics g)
    {
        g.drawImage(wall, (int) x, (int) y, null);
    }
    public void renderMiniMap(Graphics g, int scaleDown, BufferedImage after)
    {
        g.drawImage(after,(int)x/scaleDown ,(int) y/scaleDown , null);
    }
}
