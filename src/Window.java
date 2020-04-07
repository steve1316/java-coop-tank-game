import javax.swing.JPanel;
import javax.swing.JFrame;

//This class will create the Window that will house the JFrame
public class Window extends JPanel
{
    public Window(int width, int height, JFrame frame, Game game)
    {
        frame.add(game);
        frame.pack();
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setFocusable(false);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.setFocusable(false);
    }
}
