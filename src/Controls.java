import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Graphics;

public class Controls extends MouseAdapter
{
    private Game game;

    public Controls(Game game)
    {
        this.game = game;
    }

    public void mousePressed(MouseEvent mEvent)
    {
        int mouseX = mEvent.getX();
        int mouseY = mEvent.getY();

        if(game.gameState == Game.STATE.Controls)
        {
            //Go to the main menu when clicking on the button.
            if(mouseOverMenu(mouseX, mouseY,0, 0, 190, 80))
            {
                game.gameState = Game.STATE.Menu;
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

    }
}