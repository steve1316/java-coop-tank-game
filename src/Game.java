import javax.imageio.ImageIO;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Dimension;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;

public class Game extends Canvas implements Runnable
{
    private boolean running = false;
    private Thread thread;
    private final String TITLE = "Tank Wars";
    static final int WIDTH = 1280;
    static final int HEIGHT = 760;
    private int tankLeftSpeed = 7;
    private int tankRightSpeed = 7;
    private boolean init = false;
    private boolean isPlayer1Shooting = false;
    private boolean isPlayer2Shooting = false;
    public static BufferedImage background, destroyableWall, wall, speedPowerImg, healthPackImg, heart, gameOver, tankImage, bulletImage, spikes, mainMenu, controls, pauseImage;
    ArrayList<DestructWall> destroyableWallList;
    ArrayList<Spikes> spikesList;
    private Tank tankLeft;
    private Tank tankRight;
    private boolean isPlayer1Dead = false;
    private boolean isPlayer2Dead = false;
    private boolean speedBoostPickedUp;
    private boolean healthPickedUp;

    int player1RespawnX, player1RespawnY, player2RespawnX, player2RespawnY;

    BulletHandler tankLeftBulletHandler;
    BulletHandler tankRightBulletHandler;
    PowerUp speedPowerUp;
    PowerUp hpPowerUp;
    BufferedImage miniDestroyableWall;
    BufferedImage miniWall;
    BufferedImage miniHeart;
    BufferedImage miniSpikes;

    public static boolean paused = false;
    private Menu menu;
    private Controls myControls;

    int scaleDown;
    boolean imageLoadCheck = false;

    int timerSpeedRespawn = 0;
    int timerHealthRespawn = 0;

    public enum STATE
    {
        Menu, Game, Controls, GameOver, Exit
    }

    public STATE gameState = STATE.Menu;

    public void speedRespawn()
    {
        speedBoostPickedUp = false;
        timerSpeedRespawn = 0;
    }

    public void healthRespawn()
    {
        healthPickedUp = false;
        timerHealthRespawn = 0;
    }

    public static void main(String args[])
    {
        Game game = new Game();

        game.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        game.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        game.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        JFrame frame = new JFrame(game.TITLE);

        new Window(WIDTH, HEIGHT, frame, game);

        game.start();
    }

    public Game()
    {
        if (!init)
        {
            try
            {
                //UI element images
                gameOver = ImageIO.read(new File("resources/gameOver.png"));
                mainMenu = ImageIO.read(new File("resources/mainMenu.png"));
                pauseImage = ImageIO.read(new File("resources/pauseImage.png"));
                controls = ImageIO.read(new File("resources/controls.png"));
                heart = ImageIO.read(new File("resources/hearts.png"));
                destroyableWall = ImageIO.read(new File("resources/Wall1.gif"));
                speedPowerImg = ImageIO.read(new File("resources/run.png"));
                healthPackImg = ImageIO.read(new File("resources/health-normal.png"));
                spikes = ImageIO.read(new File("resources/spikes2.png"));
                destroyableWallList = new ArrayList<>();
                spikesList = new ArrayList<>();

                //Background and Wall images
                background = ImageIO.read(new File("resources/Background.png"));
                wall = ImageIO.read(new File("resources/Wall2.png"));

                //Tank and bullet image
                tankImage = ImageIO.read(new File("Resources/tank1.png"));
                bulletImage = ImageIO.read(new File("Resources/shell.png"));
                imageLoadCheck = true;
            }
            catch (IOException e)
            {
                System.out.println("Failed trying to find images in resources to load into Game");
            }

            //If jar did not read from resources folder, read from root.
            if(!imageLoadCheck)
            {
                try
                {
                    //UI element images
                    gameOver = ImageIO.read(new File("gameOver.png"));
                    mainMenu = ImageIO.read(new File("mainMenu.png"));
                    pauseImage = ImageIO.read(new File("pauseImage.png"));
                    controls = ImageIO.read(new File("controls.png"));
                    heart = ImageIO.read(new File("hearts.png"));
                    destroyableWall = ImageIO.read(new File("Wall1.gif"));
                    speedPowerImg = ImageIO.read(new File("run.png"));
                    healthPackImg = ImageIO.read(new File("health-normal.png"));
                    spikes = ImageIO.read(new File("spikes2.png"));
                    destroyableWallList = new ArrayList<>();
                    spikesList = new ArrayList<>();

                    //Background and Wall images
                    background = ImageIO.read(new File("Background.png"));
                    wall = ImageIO.read(new File("Wall2.png"));

                    //Tank and bullet image
                    tankImage = ImageIO.read(new File("tank1.png"));
                    bulletImage = ImageIO.read(new File("shell.png"));
                } catch (IOException e)
                {
                    System.out.println("Failed trying to find images in root to load into Game");
                }
            }

            //Use scaleDown to scale images down to fit in the MiniMap.
            scaleDown = 4;
            miniDestroyableWall = scaleDownBuffImg(destroyableWall, scaleDown);
            miniWall = scaleDownBuffImg(wall, scaleDown);
            miniHeart = scaleDownBuffImg(heart, 3);
            miniSpikes = scaleDownBuffImg(spikes, scaleDown);

            //Add the KeyListener to Game which will redirect to the key input methods in here.
            this.setFocusable(true);
            this.addKeyListener(new KeyInput(this));

            //Load in the map text file and render the powerups onto the generated map.
            loadMapAndPowerUps();

            //Create the 2 ProjectileControllers that will handle each Projectile fired.
            tankLeftBulletHandler = new BulletHandler(this);
            tankRightBulletHandler = new BulletHandler(this);

            //Create Menu and add controls to it.
            menu = new Menu(this);
            this.addMouseListener(menu);
            myControls = new Controls(this);
            this.addMouseListener(myControls);

            init = true;
        }
    }

    private synchronized void start()
    {
        if (running)
        {
            return;
        }
        else
        {
            thread = new Thread(this);
            running = true;
            thread.start();
        }
    }

    public void run()
    {
        Long lasTime = System.nanoTime();
        final double amountOfTicks = 60;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        int frames = 0;
        long timer = System.currentTimeMillis();

        while (running)
        {
            long now = System.nanoTime();
            delta += (now - lasTime) / ns;
            lasTime = now;
            if (delta >= 1)
            {
                tick();
                if(speedBoostPickedUp)
                {
                    timerSpeedRespawn += delta;
                    //Respawn the speed powerup after around 5 seconds.
                    if(timerSpeedRespawn > 250)
                    {
                        speedRespawn();
                    }
                }
                if(healthPickedUp)
                {
                    timerHealthRespawn += delta;
                    //Respawn the health powerup after around 5 seconds.
                    if(timerHealthRespawn > 250)
                    {
                        healthRespawn();
                    }
                }
                delta--;
            }

            render();
            frames++;

            if (System.currentTimeMillis() - timer > 1000)
            {
                timer += 1000;
                System.out.println("Fps " + frames);
                frames = 0;
            }
        }

        stop();
    }

    private synchronized void stop()
    {
        if (!running)
        {
            return;
        }
        else
        {
            running = false;

            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            System.exit(0);
        }
    }

    private void tick()
    {
        if (init)
        {
            if(gameState == STATE.Game)
            {
                if(!paused)
                {
                    //Go through each Tank and their respective ProjectileControllers
                    tankLeft.tick();
                    tankLeftBulletHandler.tick();

                    tankRight.tick();
                    tankRightBulletHandler.tick();

                    checkCollision();

                    //Check to see if Tanks are alive.
                    if(!isPlayer1Dead && !isPlayer2Dead)
                    {
                        checkAlive();
                    }

                    this.setFocusable(true);
                }
            }

            else if(gameState == STATE.Exit)
            {
                running = false;
                stop();
                System.exit(0);
            }
        }
    }

    private void checkAlive()
    {
        //If Player 1 is currently dead...
        if(tankLeft.getHp() <= 0)
        {
            //Reduce Player 1's life by 1.
            tankLeft.setLife(tankLeft.getLife() - 1);

            //If Player 1's lives are no more, reset its position, clear bullets, and go to Game Over.
            if (tankLeft.getLife() <= 0)
            {
                tankLeft.setVelX(0);
                tankLeft.setVelY(0);
                isPlayer1Dead = true;
                gameState = STATE.GameOver;
                tankLeftBulletHandler.clearBullets();
                tankRightBulletHandler.clearBullets();
            }

            //Reset its health and speed and respawn at its initial starting coordinates.
            else
            {
                tankLeft.setHp(60);
                tankLeftSpeed = 7;
                tankLeft.setTankSpeed(tankLeftSpeed);
                tankLeft.setX(player1RespawnX);
                tankLeft.setY(player1RespawnY);
            }
        }

        //If Player 2 is currently dead...
        if(tankRight.getHp() <= 0)
        {
            //Reduce Player 2's life by 1.
            tankRight.setLife(tankRight.getLife() - 1);

            //If Player 1's lives are no more, reset its position, clear bullets, and go to Game Over.
            if (tankRight.getLife() <= 0)
            {
                tankRight.setVelX(0);
                tankRight.setVelY(0);
                isPlayer2Dead = true;
                gameState = STATE.GameOver;
                tankLeftBulletHandler.clearBullets();
                tankRightBulletHandler.clearBullets();
            }

            //Reset its health and speed and respawn at its initial starting coordinates.
            else
            {
                tankRight.setHp(60);
                tankRightSpeed = 7;
                tankRight.setTankSpeed(tankRightSpeed);
                tankRight.setX(player2RespawnX);
                tankRight.setY(player2RespawnY);
            }
        }
    }

    public void render()
    {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null)
        {
            createBufferStrategy(3);
            return;
        }

        //Draw everything onto this Graphics g.
        Graphics g = bs.getDrawGraphics();
        Graphics2D g2d = (Graphics2D) g;
        g.setColor(Color.black);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        if(gameState == STATE.Menu)
        {
            g.drawImage(mainMenu, 0, 0, mainMenu.getWidth(), mainMenu.getHeight(), this);
            menu.render(g);
        }

        if(gameState == STATE.Controls)
        {
            g.drawImage(controls, 0, 0, controls.getWidth(), controls.getHeight() - 40, this);
        }

        if(gameState == STATE.Game)
        {
            //Draw the background first and then the breakable walls.
            drawBackground(g);
            drawBreakWalls(g);
            drawSpikes(g);

            //Render Player 1 and any Projectiles remaining.
            tankLeft.render(g);
            tankLeftBulletHandler.render(g);

            //Render Player 2 and any Projectiles remaining.
            tankRight.render(g);
            tankRightBulletHandler.render(g);

            //Begin rendering the UI for the tanks.
            if (!isPlayer1Dead && !isPlayer2Dead)
            {
                drawMiniMap(g);

                //If the powerups haven't been picked up yet, render them.
                if (!speedBoostPickedUp)
                {
                    speedPowerUp.render(g);
                }
                if (!healthPickedUp)
                {
                    hpPowerUp.render(g);
                }

                //Player 1 Health Bar
                g.setColor(Color.red);
                g.fillRect((int) tankLeft.getX(), (int) tankLeft.getY() + 70, 60, 10);
                g.setColor(Color.green);
                g.fillRect((int) tankLeft.getX(), (int) tankLeft.getY() + 70, (int) tankLeft.getHp(), 10);

                //Player 2 Health Bar
                g.setColor(Color.red);
                g.fillRect((int) tankRight.getX(), (int) tankRight.getY() + 70, 60, 10);
                g.setColor(Color.green);
                g.fillRect((int) tankRight.getX(), (int) tankRight.getY() + 70, (int) tankRight.getHp(), 10);

                //Render the hearts below Player 1 as its lives remaining.
                switch (tankLeft.getLife())
                {
                    case 3:
                        g.drawImage(heart, (int) tankLeft.getX(), (int) tankLeft.getY() + 80, this);
                        g.drawImage(heart, (int) tankLeft.getX() + 15, (int) tankLeft.getY() + 80, this);
                        g.drawImage(heart, (int) tankLeft.getX() + 30, (int) tankLeft.getY() + 80, this);
                        break;
                    case 2:
                        g.drawImage(heart, (int) tankLeft.getX(), (int) tankLeft.getY() + 80, this);
                        g.drawImage(heart, (int) tankLeft.getX() + 15, (int) tankLeft.getY() + 80, this);
                        break;
                    case 1:
                        g.drawImage(heart, (int) tankLeft.getX(), (int) tankLeft.getY() + 80, this);
                        break;
                    case 0:
                        break;
                    default:
                        break;
                }

                //Render the hearts below Player 2 as its lives remaining.
                switch (tankRight.getLife())
                {
                    case 3:
                        g.drawImage(heart, (int) tankRight.getX(), (int) tankRight.getY() + 80, this);
                        g.drawImage(heart, (int) tankRight.getX() + 15, (int) tankRight.getY() + 80, this);
                        g.drawImage(heart, (int) tankRight.getX() + 30, (int) tankRight.getY() + 80, this);
                        break;
                    case 2:
                        g.drawImage(heart, (int) tankRight.getX(), (int) tankRight.getY() + 80, this);
                        g.drawImage(heart, (int) tankRight.getX() + 15, (int) tankRight.getY() + 80, this);
                        break;
                    case 1:
                        g.drawImage(heart, (int) tankRight.getX(), (int) tankRight.getY() + 80, this);
                        break;
                    case 0:
                        break;
                    default:
                        break;
                }
            }
        }

        //If Game Over, render the game over screen instead.
        else if(gameState == STATE.GameOver)
        {
            g.drawImage(gameOver, 0, 0, gameOver.getWidth(), gameOver.getHeight(), this);
        }

        //If the game is paused, render the "GAME PAUSED" text in the middle of the screen.
        if(paused)
        {
            g.drawImage(pauseImage, (gameOver.getWidth() - 125)/ 2, (gameOver.getHeight() - 150)/ 2, this);
            Font myFont = new Font("Serif", Font.BOLD, 50);
            g.setFont(myFont);
            g.setColor(Color.black);
            g.drawString("GAME PAUSED", (gameOver.getWidth() - 350)/ 2, (gameOver.getHeight() - 175)/ 2);
        }

        g.dispose();
        bs.show();
    }

    //Call the render method for each destroyable wall in the destroyableWallList ArrayList.
    public void drawBreakWalls(Graphics g)
    {
        if(gameState == STATE.Game)
        {
            for (int i = 0; i < destroyableWallList.size(); i += 1)
            {
                destroyableWallList.get(i).render(g);
            }
        }
    }

    ////Call the render method for each spike in the spikesList ArrayList.
    public void drawSpikes(Graphics g)
    {
        if(gameState == STATE.Game)
        {
            for (int i = 0; i < spikesList.size(); i += 1)
            {
                spikesList.get(i).render(g);
            }
        }
    }

    //Reset the map by clearing the destroyableWall list and load the map and powerups again.
    public void resetMap()
    {
        destroyableWallList.clear();
        loadMapAndPowerUps();
    }

    public void loadMapAndPowerUps()
    {
        int[][] map = new int[24][40];

        try
        {
            //Read in the map text file and grab each number and store them into the map 2D array.
            String line;
            BufferedReader bReader = new BufferedReader(new FileReader("Resources/map.txt"));
            String delimiters = " ";

            for(int row = 0; row < 24; row++)
            {
                line = bReader.readLine();
                String[] tokens = line.split(delimiters);
                for(int col = 0; col < 40; col++)
                {
                    map[row][col] = Integer.parseInt(tokens[col]);
                }
            }

            bReader.close();
        }
        catch(IOException e)
        {
            System.out.println("Map text file not found in resources.");
        }

        if(!imageLoadCheck)
        {
            try
            {
                //Read in the map text file and grab each number and store them into the map 2D array.
                String line;
                BufferedReader bReader = new BufferedReader(new FileReader("map.txt"));
                String delimiters = " ";

                for(int row = 0; row < 24; row++)
                {
                    line = bReader.readLine();
                    String[] tokens = line.split(delimiters);
                    for(int col = 0; col < 40; col++)
                    {
                        map[row][col] = Integer.parseInt(tokens[col]);
                    }
                }

                bReader.close();
            }
            catch(IOException e)
            {
                System.out.println("Map text file not found in root.");
            }
        }

        //After the map text file has been read, reach through the array and create the appropriate objects at specific numbers, like the Players and the destroyable walls.
        for(int i = 0; i < 24; i++)
        {
            for(int j = 0; j < 40; j++)
            {
                int pixelX = j * 32;
                int pixelY = i * 32;

                if(map[i][j] == 2) //Player 1
                {
                    player1RespawnX = pixelX;
                    player1RespawnY = pixelY;
                    tankLeft = new Tank(pixelX, pixelY, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE, KeyEvent.VK_1, 0, this, tankImage);
                }

                else if(map[i][j] == 3) //Player 2
                {
                    player2RespawnX = pixelX;
                    player2RespawnY = pixelY;
                    tankRight = new Tank(pixelX, pixelY, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER, KeyEvent.VK_2, 0, this, tankImage);
                }

                else if(map[i][j] == 4) //Destroyable Walls
                {
                    destroyableWallList.add(new DestructWall(pixelX, pixelY, destroyableWall));
                }

                else if(map[i][j] == 5) //Speed Powerup
                {
                    speedBoostPickedUp = false;
                    speedPowerUp = new PowerUp(pixelX, pixelY, speedPowerImg);
                }

                else if(map[i][j] == 6) //Health Powerup
                {
                    healthPickedUp = false;
                    hpPowerUp = new PowerUp(pixelX, pixelY, healthPackImg);
                }

                else if(map[i][j] == 7) //Spikes
                {
                    spikesList.add(new Spikes(pixelX, pixelY, spikes));
                }
            }
        }
    }

    public void drawBackground(Graphics g)
    {
        if(gameState == STATE.Game)
        {
            //This method is in charge of rendering the ground and the boundary walls at the edges of the map.
            int tWidth = background.getWidth(this);
            int tHeight = background.getHeight(this);

            int numberX = (int) (WIDTH / tWidth);
            int numberY = (int) (HEIGHT / tHeight);

            //This for loop is in charge of rendering the ground.
            for (int i = -1; i <= numberY; i++)
            {
                for (int j = 0; j <= numberX; j++)
                {
                    g.drawImage(background, j * tWidth, i * tHeight, tWidth, tHeight, this);
                }
            }

            //Render the boundary walls at the edges of the map.
            for (int i = 0; i <= WIDTH + 320; i += wall.getHeight(this)) //Left boundary walls
            {
                g.drawImage(wall, 0, i, this);
            }

            for (int i = 0; i <= HEIGHT + 320; i += wall.getHeight(this)) //Right boundary walls
            {
                g.drawImage(wall, 1235, i, this);
            }

            for (int i = 0; i <= WIDTH + 180; i += wall.getWidth(this)) //Top boundary walls
            {
                g.drawImage(wall, i, 0, this);
            }

            for (int i = 0; i <= HEIGHT + 480; i += wall.getWidth(this)) //Bottom boundary walls
            {
                g.drawImage(wall, i, 690, this);
            }
        }
    }

    public BufferedImage scaleDownBuffImg(BufferedImage img, int scaleDown)
    {
        //This method is in charge of scaling the provided image by a factor of scaleDown.
        BufferedImage before = img;
        int w = before.getWidth();
        int h = before.getHeight();
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale((double) 1 / scaleDown, (double) 1 / scaleDown);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(before, after);
        return after;
    }

    public void drawMiniMap(Graphics g)
    {
        if(gameState == STATE.Game)
        {
            int tWidth = background.getWidth(this) / scaleDown - 4;
            int tHeight = background.getHeight(this) / scaleDown - 1;

            int numberX = 3;
            int numberY = 2;

            for (int i = 0; i <= numberY; i++)
            {
                for (int j = 0; j <= numberX; j++)
                {
                    g.drawImage(background, j * tWidth, i * tHeight, tWidth, tHeight, this);
                }
            }

            //Render the Players onto the MiniMap.
            tankLeft.renderMinimap(g, scaleDown);
            tankRight.renderMinimap(g, scaleDown);

            //Render the Bullets onto the MiniMap.
            tankRightBulletHandler.renderMiniMap(g, scaleDown);
            tankLeftBulletHandler.renderMiniMap(g, scaleDown);

            //Renders the destroyable walls onto the MiniMap.
            for (int i = 0; i < destroyableWallList.size(); i++)
            {
                destroyableWallList.get(i).renderMiniMap(g, scaleDown, miniDestroyableWall);
            }

            //Renders the spikes onto the MiniMap.
            for (int i = 0; i < spikesList.size(); i++)
            {
                spikesList.get(i).renderMiniMap(g, scaleDown, miniSpikes);
            }

            //Renders the Left Boundary walls to MiniMap
            for (int i = 0; i <= HEIGHT - 300 / scaleDown; i += miniWall.getHeight(this))
            {
                g.drawImage(miniWall, 0, i / scaleDown, this);
            }

            //Renders the Right boundary walls to MiniMap
            for (int i = 0; i <= HEIGHT - 300 / scaleDown; i += miniWall.getHeight(this))
            {
                g.drawImage(miniWall, 1185 / scaleDown, i / scaleDown, this);
            }

            //Renders the Top boundary walls to MiniMap
            for (int i = 0; i <= HEIGHT + 1600 / scaleDown; i += miniWall.getWidth(this))
            {
                g.drawImage(miniWall, i / scaleDown, 0, this);
            }

            //Renders the Bottom boundary walls to MiniMap
            for (int i = 0; i <= HEIGHT + 1600 / scaleDown; i += miniWall.getWidth(this))
            {
                g.drawImage(miniWall, i / scaleDown, 670 / scaleDown, this);
            }

            if (!speedBoostPickedUp)
            {
                speedPowerUp.renderMiniMap(g, scaleDown);
            }

            if (!healthPickedUp)
            {
                hpPowerUp.renderMiniMap(g, scaleDown);
            }
        }
    }


    public void keyPressed(KeyEvent e)
    {
        int key = e.getKeyCode();

        //Controls for the Main Menu.
        if(gameState == STATE.Menu)
        {
            if (key == KeyEvent.VK_S)
            {
                gameState = STATE.Game;
            }

            else if (key == KeyEvent.VK_C)
            {
                gameState = STATE.Controls;
            }

            else if (key == KeyEvent.VK_Q)
            {
                gameState = STATE.Exit;
            }
        }

        //Controls for the Controls Screen.
        else if(gameState == STATE.Controls)
        {
            if(key == KeyEvent.VK_ESCAPE)
            {
                gameState = STATE.Menu;
            }
        }

        //Controls for in-game.
        else if(gameState == STATE.Game)
        {
            //Reset the map by pressing R.
            if (key == KeyEvent.VK_R)
            {
                resetMap();
                tankLeftBulletHandler.clearBullets();
                tankRightBulletHandler.clearBullets();
            }

            //Pause the game by pressing P.
            if(key == KeyEvent.VK_P)
            {
                if(paused)
                {
                    paused = false;
                }
                else
                {
                    paused = true;
                }
            }

            //Go back to the Main Menu by pressing ESC.
            if(key == KeyEvent.VK_ESCAPE)
            {
                if(!paused)
                {
                    gameState = STATE.Menu;
                    resetMap();
                    tankLeftBulletHandler.clearBullets();
                    tankRightBulletHandler.clearBullets();
                }
            }

            //Controls for Player 1.
            if (!isPlayer1Dead)
            {
                if (key == tankLeft.getUp())
                {
                    tankLeft.toggleUpPressed();
                }

                else if (key == tankLeft.getLeft())
                {
                    tankLeft.toggleLeftPressed();
                }

                else if (key == tankLeft.getRight())
                {
                    tankLeft.toggleRightPressed();
                }

                else if (key == tankLeft.getDown())
                {
                    tankLeft.toggleDownPressed();
                }

                else if (key == tankLeft.getShoot() && !isPlayer1Shooting)
                {
                    if(!paused)
                    {
                        isPlayer1Shooting = true;
                        tankLeftBulletHandler.addBullet(new Bullet(tankLeft.getX() + 20, tankLeft.getY() + 20, bulletImage, tankLeft.getAngle()));
                    }
                }
            }

            //Controls for Player 2.
            if (!isPlayer2Dead)
            {
                if (key == tankRight.getUp())
                {
                    tankRight.toggleUpPressed();
                }

                else if (key == tankRight.getLeft())
                {
                    tankRight.toggleLeftPressed();
                }

                else if (key == tankRight.getRight())
                {
                    tankRight.toggleRightPressed();
                }

                else if (key == tankRight.getDown())
                {
                    tankRight.toggleDownPressed();
                }

                else if (key == tankRight.getShoot() && !isPlayer2Shooting)
                {
                    if(!paused)
                    {
                        isPlayer2Shooting = true;
                        tankRightBulletHandler.addBullet(new Bullet(tankRight.getX() + 20, tankRight.getY() + 20, bulletImage, tankRight.getAngle()));
                    }
                }
            }
        }

        //Controls for the Game Over screen.
        else if(gameState == STATE.GameOver)
        {
            //Press ESC to go back to the Main Menu.
            if (key == KeyEvent.VK_ESCAPE)
            {
                gameState = STATE.Menu;
                isPlayer1Dead = false;
                isPlayer2Dead = false;
                tankLeft.setHp(60);
                tankLeft.setLife(3);
                tankLeft.setTankSpeed(tankLeftSpeed);
                tankRight.setHp(60);
                tankRight.setLife(3);
                tankRight.setTankSpeed(tankRightSpeed);
                resetMap();
            }
            //Press ENTER to start over.
            else if (key == KeyEvent.VK_ENTER)
            {
                gameState = STATE.Game;
                isPlayer1Dead = false;
                isPlayer2Dead = false;
                tankLeft.setHp(60);
                tankLeft.setLife(3);
                tankLeft.setTankSpeed(tankLeftSpeed);
                tankRight.setHp(60);
                tankRight.setLife(3);
                tankRight.setTankSpeed(tankRightSpeed);
                resetMap();
            }
        }
    }

    public void keyTyped(KeyEvent e)
    {
        //Nothing here.
    }

    public void keyReleased(KeyEvent e)
    {
        int key = e.getKeyCode();

        //Controls for Player 1
        if (key == tankLeft.getUp())
        {
            tankLeft.unToggleUpPressed();
        }

        else if (key == tankLeft.getLeft())
        {
            tankLeft.unToggleLeftPressed();
        }

        else if (key == tankLeft.getRight())
        {
            tankLeft.unToggleRightPressed();
        }

        else if (key == tankLeft.getDown())
        {
            tankLeft.unToggleDownPressed();
        }

        else if (key == tankLeft.getShoot())
        {
            isPlayer1Shooting = false;
        }

        //Controls for Player 2.
        if (key == tankRight.getUp())
        {
            tankRight.unToggleUpPressed();
        }

        else if (key == tankRight.getLeft())
        {
            tankRight.unToggleLeftPressed();
        }

        else if (key == tankRight.getRight())
        {
            tankRight.unToggleRightPressed();
        }

        else if (key == tankRight.getDown())
        {
            tankRight.unToggleDownPressed();
        }

        else if (key == tankRight.getShoot())
        {
            isPlayer2Shooting = false;
        }
    }

    public void checkCollision()
    {
        if(gameState == STATE.Game)
        {
            //If both players are not dead...
            if(!isPlayer1Dead && !isPlayer2Dead)
            {
                //Tank colliding with Tank. Stop both tanks after colliding.
                if (tankLeft.getBounds(50, 50).intersects(tankRight.getBounds(50, 50)))
                {
                    tankLeft.setX(tankLeft.getOldX());
                    tankLeft.setY(tankLeft.getOldY());
                    tankRight.setX(tankRight.getOldX());
                    tankRight.setY(tankRight.getOldY());
                }
            }

            for (int i = 0; i < spikesList.size(); i++)
            {
                //Player 1 colliding with spikes. Subtract some health.
                if (tankLeft.getBounds(50, 50).intersects(spikesList.get(i).getBounds(30, 35)))
                {
                    tankLeft.setHp(tankLeft.getHp() - 0.05);
                }
            }

            for (int i = 0; i < spikesList.size(); i++)
            {
                //Player 2 colliding with spikes. Subtract some health.
                if (tankRight.getBounds(50, 50).intersects(spikesList.get(i).getBounds(30, 35)))
                {
                    tankRight.setHp(tankRight.getHp() - 0.05);
                }
            }

            for (int i = 0; i < destroyableWallList.size(); i++)
            {
                //Player 1 colliding with destroyable walls. Stop the tank.
                if (tankLeft.getBounds(50, 50).intersects(destroyableWallList.get(i).getBounds(30, 35)))
                {
                    tankLeft.setX(tankLeft.getOldX());
                    tankLeft.setY(tankLeft.getOldY());
                }
            }

            for (int i = 0; i < destroyableWallList.size(); i++)
            {
                //Player 2 colliding with destroyable walls. Stop the tank.
                if (tankRight.getBounds(50, 50).intersects(destroyableWallList.get(i).getBounds(30, 35)))
                {
                    tankRight.setX(tankRight.getOldX());
                    tankRight.setY(tankRight.getOldY());
                }
            }

            if (!tankLeftBulletHandler.checkBulletCollision().isEmpty())
            {
                for (int i = 0; i < destroyableWallList.size(); i++)
                {
                    for (int j = 0; j < tankLeftBulletHandler.checkBulletCollision().size(); j++)
                    {
                        //Player 1's bullets colliding with destroyable wall. Remove the bullet and the wall.
                        if (tankLeftBulletHandler.checkBulletCollision().get(j).getBounds(25, 25).intersects(destroyableWallList.get(i).getBounds(30, 35)))
                        {
                            tankLeftBulletHandler.removeBullet(tankLeftBulletHandler.checkBulletCollision().get(j));
                            destroyableWallList.remove(i);
                            break;
                        }
                    }
                }
            }

            if (!tankRightBulletHandler.checkBulletCollision().isEmpty())
            {
                for (int i = 0; i < destroyableWallList.size(); i++)
                {
                    for (int j = 0; j < tankRightBulletHandler.checkBulletCollision().size(); j++)
                    {
                        //Player 2's bullets colliding with destroyable wall. Remove the bullet and the wall.
                        if (tankRightBulletHandler.checkBulletCollision().get(j).getBounds(25, 25).intersects(destroyableWallList.get(i).getBounds(30, 35)))
                        {
                            destroyableWallList.remove(i);
                            tankRightBulletHandler.removeBullet(tankRightBulletHandler.checkBulletCollision().get(j));
                            break;
                        }
                    }
                }
            }

            if (!tankRightBulletHandler.checkBulletCollision().isEmpty())
            {
                for (int j = 0; j < tankRightBulletHandler.checkBulletCollision().size(); j++)
                {
                    //Player 1 colliding with Player 2's bullets. Remove the bullet and remove some health.
                    if (tankLeft.getBounds(50, 50).intersects(tankRightBulletHandler.checkBulletCollision().get(j).getBounds(25, 25)))
                    {
                        tankLeft.setHp(tankLeft.getHp() - tankRightBulletHandler.getDmg());
                        tankRightBulletHandler.removeBullet(tankRightBulletHandler.checkBulletCollision().get(j));
                        break;
                    }
                }
            }

            if (!tankLeftBulletHandler.checkBulletCollision().isEmpty())
            {
                for (int j = 0; j < tankLeftBulletHandler.checkBulletCollision().size(); j++)
                {
                    //Player 2 colliding with Player 1's bullets. Remove the bullet and remove some health.
                    if (tankRight.getBounds(50, 50).intersects(tankLeftBulletHandler.checkBulletCollision().get(j).getBounds(25, 25)))
                    {
                        tankRight.setHp(tankRight.getHp() - tankLeftBulletHandler.getDmg());
                        tankLeftBulletHandler.removeBullet(tankLeftBulletHandler.checkBulletCollision().get(j));
                        break;
                    }
                }

            }

            if (!tankLeftBulletHandler.checkBulletCollision().isEmpty() && !tankRightBulletHandler.checkBulletCollision().isEmpty())
            {
                for (int i = 0; i < tankLeftBulletHandler.checkBulletCollision().size(); i++)
                {
                    for (int j = 0; j < tankRightBulletHandler.checkBulletCollision().size(); j++)
                    {
                        //Bullets colliding with other bullets. Remove both of them.
                        if (tankRightBulletHandler.checkBulletCollision().get(j).getBounds(25, 25).intersects(tankLeftBulletHandler.checkBulletCollision().get(i).getBounds(25, 25)))
                        {
                            tankLeftBulletHandler.removeBullet(tankLeftBulletHandler.checkBulletCollision().get(i));
                            tankRightBulletHandler.removeBullet(tankRightBulletHandler.checkBulletCollision().get(j));
                            break;
                        }
                    }
                }
            }

            //If Players collide with powerups, activate its effects and remove it from the map.
            if (!speedBoostPickedUp)
            {
                if (tankLeft.getBounds(50, 50).intersects(speedPowerUp.getBounds(40, 40)))
                {
                    //Set the speed to 10.
                    tankLeftSpeed = 10;
                    tankLeft.setTankSpeed(tankLeftSpeed);
                    speedBoostPickedUp = true;
                }
                if (tankRight.getBounds(50, 50).intersects(speedPowerUp.getBounds(40, 40)))
                {
                    //Set the speed to 10.
                    tankRightSpeed = 10;
                    tankRight.setTankSpeed(tankRightSpeed);
                    speedBoostPickedUp = true;
                }
            }
            if (!healthPickedUp)
            {
                if (tankLeft.getBounds(50, 50).intersects(hpPowerUp.getBounds(40, 40)))
                {
                    //Restore the health to full.
                    tankLeft.setHp(60);
                    healthPickedUp = true;
                }
                if (tankRight.getBounds(50, 50).intersects(hpPowerUp.getBounds(40, 40)))
                {
                    //Restore the health to full.
                    tankRight.setHp(60);
                    healthPickedUp = true;
                }
            }
        }
    }
}
