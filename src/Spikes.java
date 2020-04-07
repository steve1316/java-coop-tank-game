import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class Spikes extends GameObject
{
    private BufferedImage spikes;

    public Spikes(double x, double y, BufferedImage spikes)
    {
        super (x,y);
        this.spikes = spikes;
    }

    public void render(Graphics g)
    {
        g.drawImage(spikes, (int) x, (int) y, null);
    }

    public void renderMiniMap(Graphics g, int scaleDown, BufferedImage after)
    {
        g.drawImage(after,(int)x/scaleDown ,(int) y/scaleDown , null);
    }
}
