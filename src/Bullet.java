import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Bullet extends GameObject
{
    private double speed = 5;

    private BufferedImage bulletImage;
    private double angleX, angleY;
    private int angle;

    public BufferedImage getBulletImage()
    {
        return bulletImage;
    }

    public Bullet(double x, double y, BufferedImage image, int angle)
    {
        super(x, y);

        //Convert the angles to radians for sine and cosine operations. Added the plus 280 to make sure the bullets are
        // coming out of the barrel instead of from the top of the tank image since the tank image is initially is facing left.
        double radians = (angle*Math.PI + 280)/180.0;

        //Increment x and y by the radians.
        this.x = x + 36*Math.sin(radians);
        this.y = y - 36*Math.cos(radians);

        //Set angleX and angleY to the radians multiplied by the speed of the bullet.
        angleX = Math.sin(radians)* speed;
        angleY = Math.cos(radians)* speed;

        this.angle = angle;
        this.bulletImage = image;
    }

    public void tick()
    {
        //After each tick, translate the bullet by summing angleX and minusing angleY from x and y respectively.
        this.x += angleX;
        this.y -= angleY;
    }

    public void render(Graphics g)
    {
        //Rotate the bullet in the direction of where it was fired from.
        AffineTransform rotation = AffineTransform.getTranslateInstance(this.x, this.y);
        rotation.rotate(Math.toRadians(angle), this.bulletImage.getWidth() / 2.0, this.bulletImage.getHeight() / 2.0);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.bulletImage, rotation, null);
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public int getAngle()
    {
        return angle;
    }
}
