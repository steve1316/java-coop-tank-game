import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Tank extends GameObject
{
    private double velX = 0;
    private double velY = 0;
    private int life = 3;
    private int up, down, left, right, shoot, respawn;
    private BufferedImage tankImg;
    private double hp = 60;
    private boolean isDeadtankLeft = false;
    private boolean isDeadtankRight = false;
    private int tankSpeed = 7;

    private int angle;
    private final int R = 2;
    private final int ROTATIONSPEED = 3;
    private boolean UpPressed;
    private boolean DownPressed;
    private boolean RightPressed;
    private boolean LeftPressed;
    private Game game;

    private void moveBackwards()
    {
        velX = (int) Math.round(R * Math.cos(Math.toRadians(angle))) * tankSpeed/3;
        velY = (int) Math.round(R * Math.sin(Math.toRadians(angle))) * tankSpeed/3;
        oldX = x;
        oldY = y;
        x -= velX;
        y -= velY;
        checkBorder();
    }

    private void moveForwards()
    {
        velX = (int) Math.round(R * Math.cos(Math.toRadians(angle))) * tankSpeed/3;
        velY = (int) Math.round(R * Math.sin(Math.toRadians(angle))) * tankSpeed/3;
        oldX = x;
        oldY = y;
        x += velX;
        y += velY;
        checkBorder();
    }

    //Check the tank's position to make sure it doesn't go beyond the boundary walls.
    private void checkBorder()
    {
        if (x < 30)
        {
            oldX = x;
            x = 30;
        }
        if (x >= game.WIDTH - 110)
        {
            oldX = x;
            x = game.WIDTH - 110;
        }
        if (y < 40)
        {
            oldY = y;
            y = 40;
        }
        if (y >= game.HEIGHT - 135)
        {
            oldY = y;
            y = game.HEIGHT - 135;
        }
    }

    public Tank(double x, double y, int up, int down, int left, int right, int shoot, int respawn, int angle, Game game, BufferedImage img) {
        super(x, y);
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.shoot = shoot;
        this.respawn = respawn;
        this.angle = angle;
        this.game = game;
        this.tankImg = img;
        this.oldX = x;
        this.oldY = y;
    }

    public void tick()
    {
        if (this.UpPressed) {
            this.moveForwards();
        }
        if (this.DownPressed) {
            this.moveBackwards();
        }

        if (this.LeftPressed) {
            this.rotateLeft();
        }
        if (this.RightPressed) {
            this.rotateRight();
        }
    }

    public void render(Graphics g)
    {
        AffineTransform rotation = AffineTransform.getTranslateInstance(x, y);
        rotation.rotate(Math.toRadians(angle), this.tankImg.getWidth() / 2.0, this.tankImg.getHeight() / 2.0);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.tankImg, rotation, null);
    }

    public void renderMinimap(Graphics g, int scaleDown)
    {
        BufferedImage before = tankImg;
        int w = before.getWidth();
        int h = before.getHeight();
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale((double) 1 / scaleDown, (double) 1 / scaleDown);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(before, after);
        g.drawImage(after, (int) x/scaleDown, (int) y /scaleDown, null);
    }


    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setVelX(double velX) {
        this.velX = velX;
    }

    public void setVelY(double velY) {
        this.velY = velY;
    }

    public int getUp() {
        return up;
    }

    public int getDown() {
        return down;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getShoot() {
        return shoot;
    }

    public int getRespawn() {
        return respawn;
    }

    public double getHp() {
        return hp;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public void setLeftDead(boolean isDeadtankLeft)
    {
        this.isDeadtankLeft = isDeadtankLeft;
    }

    public boolean getLeftDead()
    {
        return isDeadtankLeft;
    }

    public void setRightDead(boolean isDeadtankRight)
    {
        this.isDeadtankRight = isDeadtankRight;
    }

    public boolean getRightDead()
    {
        return isDeadtankRight;
    }

    public void setTankSpeed(int speed)
    {
        this.tankSpeed = speed;
    }

    public int getTankSpeed()
    {
        return tankSpeed;
    }

    public double getOldX()
    {
        return oldX;
    }

    public void setOldX(double oldX)
    {
        this.oldX = oldX;
    }

    public double getOldY()
    {
        return oldY;
    }

    public void setOldY(double oldY)
    {
        this.oldY = oldY;
    }

    private double oldX, oldY;

    public int getAngle()
    {
        return angle;
    }

    void toggleUpPressed() {
        this.UpPressed = true;
    }

    void toggleDownPressed() {
        this.DownPressed = true;
    }

    void toggleRightPressed() {
        this.RightPressed = true;
    }

    void toggleLeftPressed() {
        this.LeftPressed = true;
    }

    void unToggleUpPressed() {
        this.UpPressed = false;
    }

    void unToggleDownPressed() {
        this.DownPressed = false;
    }

    void unToggleRightPressed() {
        this.RightPressed = false;
    }

    void unToggleLeftPressed() {
        this.LeftPressed = false;
    }

    private void rotateLeft() {
        this.angle -= this.ROTATIONSPEED;
    }

    private void rotateRight() {
        this.angle += this.ROTATIONSPEED;
    }
}
