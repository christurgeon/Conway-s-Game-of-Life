/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.tools;
import javax.swing.*;
import java.awt.Color;
import javax.swing.border.LineBorder;

public class CellPanel extends JPanel {

  private Color cellColor;
  private int transparency;

  /**
   *  Constructor for an individual CellPantel. It takes in
   *  the initial color to and sets the CellPanel to that
   *  color, it also adds a border.
   *  @param color - the initial color to set the CellPanel to
   */
  public CellPanel(Color color) {
    this.cellColor = color;
    this.transparency = 255;
    this.setBackground(color);
    LineBorder border = new LineBorder(Color.BLACK, 1);
    this.setBorder(border);
  }

  /**
   *  This method is invoked to dim the color
   *  of a ButtonCell after each tick that it
   *  survives.
   *  @param None
   *  @return true - if the color could be dimmed
   *  @return false - if the cell is dead or is too
   *                  transparent to be faded again
   */
  public Boolean darken() {
    transparency = transparency - 50;
    int r = cellColor.getRed();
    int g = cellColor.getGreen();
    int b = cellColor.getBlue();
    if (transparency > 20) { // Can make color more transparent
      Color newColor = new Color(r, g, b, transparency);
      this.cellColor = newColor;
      this.setBackground(this.cellColor);
      return true;
    } else { // Color is too transparent already, keep it at 20
      Color newColor = new Color(r, g, b, 30);
      this.cellColor = newColor;
      this.setBackground(this.cellColor);
      return false;
    }
  }

  /**
   *  Change the color of the CellPanel to White.
   *  @param None
   *  @return None
   */
  public void kill() {
    this.cellColor = Color.WHITE;
    this.setBackground(cellColor);
  }

  /**
   *  Change the color of the CellPanel to Green.
   *  @param c - the color to set the cell to when it comes to life
   *  @return None
   */
  public void birth(Color c) {
    this.cellColor = c;
    this.setBackground(cellColor);
  }

  /**
   *  Accessor method for the Cell's state.
   *  @param None
   *  @return 1 if the cell is alive, 0 if dead
   */
  public int getState() {
    if (this.isAlive())
      return 1;
    else
      return 0;
  }

  /**
   *  Accessor method for the Cell's color.
   *  @param None
   *  @return the color of the cell
   */
  public Color getColor() {
    return this.cellColor;
  }

  /**
   *  This method returns a boolean stating whether the cell is alive.
   *  @param None
   *  @return true it the cell is alive
   *  @return false if the cell is dead
   */
  public boolean isAlive() {
    return !cellColor.equals(Color.WHITE);
  }

  /**
   *  This method resets the Color's alpha value
   *  @param None
   *  @return None
   */
  public void resetOpacity() {
    this.transparency = 255;
  }
}
