import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class BulletHandler
{
    private LinkedList<Bullet> bulletList = new LinkedList<>();
    Bullet tempBullet;
    Game game;
    private int dmg = 15;

    public BulletHandler(Game game) {
        this.game = game;
    }

    public void tick()
    {
        for (int i = 0; i < bulletList.size(); i++)
        {
            tempBullet = bulletList.get(i);

            //Remove bullet if it goes out of bounds in the miniMap.
            if (tempBullet.getX() < 292.5 && tempBullet.getY() < 167.5)
            {
                removeBullet(tempBullet);
            }

            if (tempBullet.getX() < 30)
            {
                removeBullet(tempBullet);
            }

            else if (tempBullet.getY() < 30)
            {
                removeBullet(tempBullet);

            }

            else if (tempBullet.getX() > 1210)
            {
                removeBullet(tempBullet);
            }

            else if (tempBullet.getY() > 670)
            {
                removeBullet(tempBullet);
            }

            tempBullet.tick();
        }
    }

    public void render(Graphics g)
    {
        for (int i = 0; i < bulletList.size(); i++)
        {
            tempBullet = bulletList.get(i);
            tempBullet.render(g);
        }
    }

    public void renderMiniMap(Graphics g, int scaleDown)
    {
        int scaleDownImage = scaleDown + 1;

        for (int i = 0; i < bulletList.size(); i++)
        {
            if(tempBullet != null && tempBullet.getBulletImage() != null)
            {
                BufferedImage before = tempBullet.getBulletImage();
                int w = tempBullet.getBulletImage().getWidth();
                int h = tempBullet.getBulletImage().getHeight();
                BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                AffineTransform at = new AffineTransform();
                //at.scale((double) 1 / scaleDownImage, (double) 1 / scaleDownImage);wa
                at.scale((double) 1 / 6, (double) 1 / 6);
                AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                after = scaleOp.filter(before, after);
                g.drawImage(after,(int) bulletList.get(i).x/scaleDown ,(int) bulletList.get(i).y/scaleDown , null);
            }
        }
    }


    public void addBullet(Bullet bullet)
    {
        bulletList.add(bullet);
    }

    public void removeBullet(Bullet bullet)
    {
        bulletList.remove(bullet);
    }

    public void clearBullets()
    {
        bulletList.clear();
    }

    public LinkedList<Bullet> checkBulletCollision()
    {
        return bulletList;
    }

    public int getDmg()
    {
        return dmg;
    }
}
