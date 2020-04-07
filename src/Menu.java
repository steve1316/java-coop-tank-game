import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;

public class Menu extends MouseAdapter
{
    private Game game;

    public Menu(Game game)
    {
        this.game = game;
    }

    public void mousePressed(MouseEvent mEvent)
    {
        int mouseX = mEvent.getX();
        int mouseY = mEvent.getY();

        if(game.gameState == Game.STATE.Menu)
        {
            //Go to the game when clicking on "(S)tart".
            if(mouseOverMenu(mouseX, mouseY,Game.WIDTH / 2 - 110, 250, 200, 64))
            {
                game.gameState = Game.STATE.Game;

            }

            //Go to the controls when clicking on "(C)ontrols".
            if(mouseOverMenu(mouseX, mouseY,Game.WIDTH / 2 - 110, 350, 200, 64))
            {
                game.gameState = Game.STATE.Controls;
            }

            //Exit the game when clicking on "(Q)uit".
            if(mouseOverMenu(mouseX, mouseY,Game.WIDTH / 2 - 110, 450, 200, 64))
            {
                game.gameState = Game.STATE.Exit;
            }
        }
    }

    public void mouseReleased(MouseEvent mEvent)
    {

    }

    private boolean mouseOverMenu(int mouseX, int mouseY, int x, int y, int width, int height)
    {
        if(mouseX > x && mouseX < x + width)
        {
            if(mouseY > y && mouseY < y + height)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public void tick()
    {

    }

    public void render(Graphics graphics)
    {
        Font font = new Font("arial", 1, 50);
        Font font2 = new Font("arial", 1, 30);
        graphics.setFont(font);

        graphics.setColor(Color.black);
        graphics.setFont(font2);
        graphics.drawRect(Game.WIDTH / 2 - 110,250,200,64);
        graphics.drawString("(S)tart", Game.WIDTH / 2 - 55, 290);

        graphics.setColor(Color.black);
        graphics.drawRect(Game.WIDTH / 2 - 110,350,200,64);
        graphics.drawString("(C)ontrols", Game.WIDTH / 2 - 80, 390);

        graphics.setColor(Color.black);
        graphics.drawRect(Game.WIDTH / 2 - 110,450,200,64);
        graphics.drawString("(Q)uit", Game.WIDTH / 2 - 50, 490);
    }
}