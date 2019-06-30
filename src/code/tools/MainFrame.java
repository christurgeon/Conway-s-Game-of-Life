/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {

  /**
   *  This method passes set up information to main window.
   *  It sets the name, the close operation, the opening
   *  position and the initial size.
   *  @param None
   */
  public MainFrame() {
    setTitle("Conway's Game of Life");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Toolkit kit = Toolkit.getDefaultToolkit();
    Dimension screenSize = kit.getScreenSize();
    int screenHeight = screenSize.height;
    int screenWidth = screenSize.width;
    setSize(screenWidth / 2, screenHeight / 2);
    setLocationByPlatform(true);
    setLocationRelativeTo(null);
  }
}
