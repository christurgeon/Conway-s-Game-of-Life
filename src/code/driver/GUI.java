/**
 *  @author Chris Turgeon
 *  @version 1.0
 */

package code.driver;
import code.tools.*;
import java.io.*;
import java.util.*;
import java.lang.Math;
import java.lang.String;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.nio.file.*;


// ============================================================================
// ============================================================================


public class GUI {

  private GameOfLife game;
  private ArrayList<ArrayList<CellPanel>> grid;
  private ArrayList<int[][]> prevGrids;
  private int rowCount;
  private int colCount;

  private MainFrame frame;
  private JPanel cellPanel;
  private StatisticsPanel statsPanel;
  private Color currentColor;

  private String outFileName = "out";
  private String outDirectory = ".";
  private static final String[] GUI_COLORS = { "Green", "Red", "Blue", "Orange", "Yellow" };

  /**
   *  The constructor for the Game Of Life graphical interface.
   *  @param rowCount - the number of rows in the grid
   *  @param colCount - the number of columns in the grid
   */
  public GUI(int rowCount, int colCount) {
    this.frame = new MainFrame();
    this.frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent) {
        writeConfig();
      }
    });
    this.rowCount = rowCount;
    this.colCount = colCount;
    this.prevGrids = new ArrayList<int[][]>();
    this.currentColor = Color.GREEN;
    this.grid = new ArrayList<ArrayList<CellPanel>>();
    for (int r = 0; r < rowCount; r++) {
      this.grid.add(new ArrayList<CellPanel>());
      for (int c = 0; c < colCount; c++) {
        CellPanel cell = new CellPanel(Color.WHITE);
        this.grid.get(r).add(cell);
      }
    }
  }


  /**
   *  Driver method to create the GUI and display it to the user.
   *  @param None
   *  @return None
   */
  public void createAndShowGUI() {

    // Load the config file
    File configFile = new File("_CONFIG_.txt");
    if (configFile.exists()) {
      loadConfig();
    } else {
      statsPanel = new StatisticsPanel(0, 0);
    }

    JPanel containerPanel = new JPanel();
    containerPanel.setLayout(new BorderLayout());
    this.cellPanel = new JPanel();
    cellPanel.setLayout(new GridLayout(this.rowCount, this.colCount));

    // Add CellPanels to the JPanel
    for (int r = 0; r < rowCount; r++) {
      for (int c = 0; c < colCount; c++) {
        cellPanel.add(this.grid.get(r).get(c));
      }
    }

    // Create a JComboBox to take in user selected colors
    JComboBox<String> colorSelector = new JComboBox<String>(GUI_COLORS);
    colorSelector.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (rowCount == 2 || colCount == 2) {
          JOptionPane.showMessageDialog(null, "You must load in a game before selecting a color");
          return;
        }
        String selectedColor = (String) colorSelector.getSelectedItem();
        if (selectedColor.equals("Green"))  updateColor(Color.GREEN);
        if (selectedColor.equals("Red"))    updateColor(Color.RED);
        if (selectedColor.equals("Orange")) updateColor(Color.ORANGE);
        if (selectedColor.equals("Yellow")) updateColor(Color.YELLOW);
        if (selectedColor.equals("Blue"))   updateColor(Color.BLUE);
      }
    });

    TickControl tickControl = new TickControl(Color.RED, Color.GREEN);
    GameMenuBar menu = new GameMenuBar();
    menu.addObject(colorSelector);

    // Add statistics and control objects to bottom panel
    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 7));
    bottomPanel.add(statsPanel);
    bottomPanel.add(tickControl);

    // Add to frame and display
    cellPanel.setPreferredSize(new Dimension(600, 600));
    containerPanel.add(menu, BorderLayout.NORTH);
    containerPanel.add(cellPanel, BorderLayout.CENTER);
    containerPanel.add(bottomPanel, BorderLayout.SOUTH);
    this.frame.getContentPane().add(containerPanel);
    this.frame.pack();
    this.frame.setVisible(true);
  }


  /**
   *  Update the color of the cells.
   *  @param color the new color of the cells
   *  @return None
   */
  public void updateColor(Color color) {
    this.currentColor = color;
    for (int r = 0; r < rowCount; r++) {
      for (int c = 0; c < colCount; c++) {
        if (this.game.getGrid()[r][c] == 1) {
          this.grid.get(r).get(c).birth(color);
        }
      }
    }
  }


  /**
   *  This method updates the GUI display with a new grid
   *  from a different tick.
   *  @param newGrid - a 2D array of ints used to load the new grid
   *  @param newTick - the tick corresponding to the grid
   *  @param fade - if true, apply a fade to successive alive cells
   *  @return None
   */
  public void updateAndShowGUI(int[][] newGrid, int newTick, boolean fade) {
    if (newTick == statsPanel.getTick() && newTick > 0) return;
    // Update the new colors of the cells and keep track of dead/alive count
    int numAlive = 0;
    int numDead = 0;
    for (int r = 0; r < rowCount; r++) {
      for (int c = 0; c < colCount; c++) {
        int newState = newGrid[r][c];
        CellPanel cell = this.grid.get(r).get(c);
        boolean aliveAgain = (newState == 1) && (cell.isAlive());
        if (aliveAgain && fade) {
          cell.darken();
          numAlive++;
        } else if (aliveAgain && !fade) {
          cell.birth(this.currentColor);
          numAlive++;
        } else if (newState == 1 && !cell.isAlive()) {
          cell.birth(this.currentColor);
          numAlive++;
        } else {
          cell.kill();
          numDead++;
        }
      }
    }
    // Update the current grid with a new one and update stats
    this.statsPanel.update(numAlive, numDead, newTick);
    this.frame.revalidate();
    this.frame.repaint();
  }


  /**
   *  This method is called when an entirely new grid needs to be uploaded
   *  into the game interface. It assumes that newGrid holds only 0s and 1s
   *  and it assumes that it has rowCount*colCount total entries.
   *  @param newGrid - 2D array of integers which becomes the new grid
   *  @return None
   */
  public void configureNewGrid(int[][] newGrid) {
    // Reset current cell panel
    this.cellPanel.removeAll();
    cellPanel.setLayout(new GridLayout(this.rowCount, this.colCount));

    // Update the new colors of the cells and keep track of dead/alive count
    int numAlive = 0;
    int numDead = 0;
    this.grid = new ArrayList<ArrayList<CellPanel>>();
    for (int r = 0; r < rowCount; r++) {
      this.grid.add(new ArrayList<CellPanel>());
      for (int c = 0; c < colCount; c++) {
        if (newGrid[r][c] == 1) {
          CellPanel cell = new CellPanel(this.currentColor);
          this.grid.get(r).add(cell);
          this.cellPanel.add(cell);
          numAlive++;
        } else {
          CellPanel cell = new CellPanel(Color.WHITE);
          this.grid.get(r).add(cell);
          this.cellPanel.add(cell);
          numDead++;
        }
      }
    }
    this.statsPanel.update(numAlive, numDead, 0);
    this.frame.revalidate();
    this.frame.repaint();
  }


  /**
   *  This method updates the GUI with the next tick of
   *  the game. It also stores the previous grid.
   *  @param display - a boolean value telling the GUI to update or not
   *  @return None
   */
  public void nextTick(boolean display) {
    if (this.rowCount == 2) {
      JOptionPane.showMessageDialog(null, "You must load in a new game before stepping through it");
      return;
    }
    int nextTick = this.statsPanel.getTick() + 1;
    int[][] copyGrid = new int[rowCount][colCount];
    int[][] gameGrid = this.game.getGrid();
    for (int r = 0; r < rowCount; r++) {
      for (int c = 0; c < colCount; c++) {
        copyGrid[r][c] = gameGrid[r][c];
      }
    }
    this.game.replaceGrid(copyGrid); // Load the newest grid into the GameOfLife object
    this.prevGrids.add(copyGrid);    // Save the previous grid in prevGrids
    this.game.play(1);               // Update the grid for one round
    if (display) {
      this.updateAndShowGUI(this.game.getGrid(), nextTick, true);
    }
  }


  /**
   *  This method updates the GUI when the user goes back a tick.
   *  It also deletes previous grids as it goes backwards.
   *  @param display - a boolean value telling the GUI to update or not
   *  @return None
   */
  public void previousTick(boolean display) {
    int prevTick = this.statsPanel.getTick() - 1;
    int lastGrid = this.prevGrids.size() - 1;
    int[][] prevGrid = this.prevGrids.get(lastGrid);
    this.game.replaceGrid(prevGrid);
    this.prevGrids.remove(lastGrid);
    for (int r = 0; r < rowCount; r++) {
      for (int c = 0; c < colCount; c++) {
        this.grid.get(r).get(c).resetOpacity();
      }
    }
    if (display) {
      this.updateAndShowGUI(prevGrid, prevTick, false);
    }
  }


  /**
   *  This method checks to see if any of the files in
   *  directory with file name between tick start and end exist.
   *  @param directory - the location where output files will be placed
   *  @param start - the starting tick appended to the filename
   *  @param end - the ending tick appended to the filename
   *  @return true if files will be overwritten, false if not
   */
  public boolean overwriteFiles(String directory, int start, int end) {
    String fileName;
    File file;
    for (int i = start; i <= end; i++) {
      fileName = directory + "/" + outFileName + Integer.toString(i) + ".txt";
      file = new File(fileName);
      if (file.exists()) {
        return true;
      }
    }
    return false;
  }


  /**
   *  This method takes in a tick and jumps to that tick.
   *  @param tick - the tick to go to
   *  @return None
   */
  public void processAndLoadTick(int tick) {
    if (this.rowCount == 2) {
      JOptionPane.showMessageDialog(null, "You must first load in a game");
      return;
    }
    int currentTick = this.statsPanel.getTick();
    if (tick > currentTick) {
      while (this.statsPanel.getTick() < tick - 1) {
        nextTick(false);
        this.statsPanel.updateTick(true);
      }
      nextTick(true);
    } else if (tick < currentTick) {
      while (this.statsPanel.getTick() > tick + 1) {
        previousTick(false);
        this.statsPanel.updateTick(false);
      }
      previousTick(true);
    } else {
      return;
    }
  }


  /**
   *  This method sees if a configuration file exists, and if it
   *  does then it uses those settings to load into the game.
   *  @param None
   *  @return None
   */
  public void loadConfig() {
    BufferedReader br = null;
    try {
      Path currentRelativePath = Paths.get("");
      String configLoc = currentRelativePath.toAbsolutePath().toString() + "/_CONFIG_.txt";
      File file = new File(configLoc);
      br = new BufferedReader(new FileReader(file));
      this.outFileName = br.readLine();
      this.outDirectory = br.readLine();
      int tick = Integer.valueOf( br.readLine() );
      String color = br.readLine();
      this.currentColor = getColorObject(color);
      this.rowCount = Integer.valueOf( br.readLine() );
      this.colCount = Integer.valueOf( br.readLine() );

      // Build the starting grid
      int r = 0;
      int c = 0;
      int numAlive = 0;
      int numDead = 0;
      String buffer;
      int[][] inputGrid = new int[rowCount][colCount];
      while ((buffer = br.readLine()) != null) {
        char[] input = buffer.toCharArray();
        c = 0;
        for (char state : input) {
          if (state == '0') {
            inputGrid[r][c] = 0;
            numDead++;
            c++;
          } else if (state == '1') {
            inputGrid[r][c] = 1;
            numAlive++;
            c++;
          }
        }
        r++;
      }
      // Initialize the GameOfLife object
      this.game = new GameOfLife(r, c);
      this.game.replaceGrid(inputGrid);
      this.statsPanel = new StatisticsPanel(numAlive, numDead);

      // Reinitialize the grid
      this.grid = new ArrayList<ArrayList<CellPanel>>();
      for (int i = 0; i < rowCount; i++) {
        this.grid.add(new ArrayList<CellPanel>());
        for (int j = 0; j < colCount; j++) {
          if (inputGrid[i][j] == 1) {
            this.grid.get(i).add(new CellPanel(this.currentColor));
          } else {
            this.grid.get(i).add(new CellPanel(Color.WHITE));
          }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
      System.out.println("No config file found");
    } finally {
      try { br.close(); }
      catch (Exception e) { System.out.println("Couldn't close reader"); }
    }
  }


  /**
   *  This method writes out to a configuration file so that
   *  settings can be saved when the app is opened again.
   */
  public void writeConfig() {
    if (this.rowCount == 2) return;
    // Write out output file name, output directory name, tick, color, row, col, starting grid
    PrintWriter writer = null;
    try {
      writer = new PrintWriter("_CONFIG_.txt", "UTF-8");
      writer.println(this.outFileName);
      writer.println(this.outDirectory);
      writer.println(this.statsPanel.getTick());
      writer.println(getColorName(this.currentColor));
      writer.println(this.rowCount);
      writer.println(this.colCount);

      int[][] outGrid;
      if (this.statsPanel.getTick() == 0)
        outGrid = this.game.getGrid();
      else
        outGrid = this.prevGrids.get(0);
      for (int r = 0; r < rowCount; r++) {
        for (int c = 0; c < colCount; c++) {
          writer.printf("%d ", outGrid[r][c]);
        }
        writer.println();
      }
    } catch(Exception e) {
      System.out.println("Something went wrong while trying to write to config file...");
    } finally {
      if (writer != null)
        writer.close();
    }
  }


  /**
   *  Get the name of a color as a string.
   *  @param c - a Color object
   *  @return the color of c as a string
   */
  private String getColorName(Color c) {
    if (c.equals(Color.GREEN)) {
      return "Green";
    } else if (c.equals(Color.RED)) {
      return "Red";
    } else if (c.equals(Color.ORANGE)) {
      return "Orange";
    } else if (c.equals(Color.BLUE)) {
      return "Blue";
    } else {
      return "Yellow";
    }
  }


  /**
   *  Get a Color based on its name.
   *  @param c - the name of a color as a string
   *  @return a Color object representing that string
   */
  private Color getColorObject(String c) {
    Color color = null;
    switch (c) {
      case "Yellow":
      color = Color.YELLOW; break;
      case "Green":
        color = Color.GREEN; break;
      case "Orange":
        color = Color.ORANGE; break;
      case "Red":
        color = Color.RED; break;
      case "Blue":
        color = Color.BLUE; break;
      }
    return color;
  }


// ============================================================================


  class GameMenuBar extends JMenuBar {

    /**
     *  Constructor for the toolbar and menu.
     *  @param None
     */
    public GameMenuBar() {

      ToolMenu tools = new ToolMenu();
      JMenu game = new JMenu("Game");
      game.setMnemonic(KeyEvent.VK_A);
      game.getAccessibleContext().setAccessibleDescription("Game Related Commands");
      JMenuItem config = new JMenuItem("Configuration");
      JMenuItem goToTick = new JMenuItem("Go To");
      JMenuItem newGame = new JMenuItem("Load Game", KeyEvent.VK_T);
      JMenuItem saveGameAll = new JMenuItem("Save All", KeyEvent.VK_T);
      JMenuItem saveGameRange = new JMenuItem("Save Range", KeyEvent.VK_T);

      // Allow a user to jump to any tick that he or she desires < 200
      goToTick.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GoTo tickSelector = new GoTo();
          String[] options = { "Submit", "Cancel" };
          int optionType = JOptionPane.DEFAULT_OPTION;
          int messageType = JOptionPane.PLAIN_MESSAGE;
          int reply = JOptionPane.showOptionDialog(null, tickSelector, "",
                      optionType, messageType, null, options, options[0]);
          if (reply == -1 || reply == 1) return;
          int tick = tickSelector.getTick("What tick do you want to go to?  ");
          if (tick == -1)
            JOptionPane.showMessageDialog(null, "Invalid input entered: must be int in range [0, 200]");
          else if (tick == -2)
            return;
          else
            processAndLoadTick(tick);
        }
      });

      // Allow user to specify file to load in
      newGame.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JFileChooser fc = new JFileChooser();
          fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
          int status = fc.showOpenDialog(null);
          if (status == JFileChooser.APPROVE_OPTION && fc.getSelectedFile().getName().contains(".txt")) {
            File selectedFile = fc.getSelectedFile();
            try {
              int numRows = 0;
              int numCols = 0;
              Scanner sc = new Scanner(selectedFile);
              if (sc.hasNext()) { // Extract the number of rows
                String rowCount = sc.next();
                rowCount = rowCount.substring(0, rowCount.length()-1);
                numRows = Integer.parseInt(rowCount);
              } if (sc.hasNext()) { // Extract the number of columns
                String colCount = sc.next();
                numCols = Integer.parseInt(colCount);
              } if (numRows < 3 || numCols < 3) { // Check for size of grid
                JOptionPane.showMessageDialog(null, "Grid dimensions within file are invalid. Width and height must be > 3.");
              } else {
                // Create new GameOfLife object, update values and grid
                GUI.this.game = new GameOfLife(numRows, numCols);
                GUI.this.game.populate(sc);
                GUI.this.rowCount = numRows;
                GUI.this.colCount = numCols;
                GUI.this.statsPanel.update(0, 0, 0);
                configureNewGrid(GUI.this.game.getGrid());
              }
            }
            // Catch Java exceptions and display errors
            catch(FileNotFoundException e1) {
              JOptionPane.showMessageDialog(null, "The selected file path was not able to be found");
            } catch(Exception e2) {
              e2.printStackTrace();
              JOptionPane.showMessageDialog(null, "An error occurred while trying to parse the file, make sure that it matches format in README.md");
            }
          } else {
            JOptionPane.showMessageDialog(null, "No file selected or non .txt file selected");
          }
        }
      });


      // SAVE ALL feature to save all ticks up to current
      saveGameAll.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (GUI.this.rowCount == 2 || GUI.this.colCount == 2) {
            JOptionPane.showMessageDialog(null, "Please load in a grid first");
            return;
          }
          int rc = -1;
          int currentTick = GUI.this.statsPanel.getTick();
          boolean writeOverFile = overwriteFiles(GUI.this.outDirectory, 0, currentTick);
          if (writeOverFile) {
            String[] options = { "YES", "NO" };
            rc = JOptionPane.showOptionDialog(null, "Do you want to overwrite files?",
                                        "Click a button", JOptionPane.DEFAULT_OPTION,
                                        JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
          }
          if (rc == 0 || !writeOverFile) {
            try {
              int i;
              String location = GUI.this.outDirectory + "/" + GUI.this.outFileName;
              for (i = 0; i < currentTick; i++) {
                int[][] outGrid = GUI.this.prevGrids.get(i);
                GUI.this.game.print(outGrid, i, location);
              }
              int[][] currentGrid = GUI.this.game.getGrid();
              GUI.this.game.print(currentGrid, i, location);
            } catch(IOException exception) {
                JOptionPane.showMessageDialog(null, "ERROR: Could not write to an output file");
            }
          }
        }
      });


      // Save a files based on a tick range
      saveGameRange.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (GUI.this.rowCount == 2 || GUI.this.colCount == 2) {
            JOptionPane.showMessageDialog(null, "Please load in a grid first");
            return;
          }
          // Create the box and pass it required information
          RangeDialogBox inputBox = new RangeDialogBox();
          String[] options = { "Submit", "Cancel" };
          int optionType = JOptionPane.DEFAULT_OPTION;
          int messageType = JOptionPane.PLAIN_MESSAGE;
          int reply = JOptionPane.showOptionDialog(null, inputBox, "Save Files By Tick Range",
                      optionType, messageType, null, options, options[0]);

          // Return if the 'cancel' or close button is hit
          if (reply == 1 || reply == -1) { return; }

          // Process the input
          try {
            String startInput =  inputBox.getText("Starting Tick ");
            String endInput = inputBox.getText(" Ending Tick ");
            int startRange = Integer.valueOf(startInput);
            int endRange = Integer.valueOf(endInput);
            if (startRange < 0 || endRange < 0) {
              JOptionPane.showMessageDialog(null, "Values must be nonnegative");
            } else if (startRange > endRange) {
              JOptionPane.showMessageDialog(null, "Ending tick must be greater than or equal to staring tick");
            } else if (endRange > 200) {
              JOptionPane.showMessageDialog(null, "Ending tick is too large, must be < 200");
            } else {
              // See if user wants to overwrite files
              int rc = -1;
              int currentTick = GUI.this.statsPanel.getTick();
              boolean writeOverFile = overwriteFiles(GUI.this.outDirectory, startRange, endRange);
              if (writeOverFile) {
                String[] opts = { "YES", "NO" };
                rc = JOptionPane.showOptionDialog(null, "Do you want to overwrite files?",
                                            "Click a button", JOptionPane.DEFAULT_OPTION,
                                            JOptionPane.INFORMATION_MESSAGE, null, opts, opts[0]);
              }
              if (rc == 1) return;
              String outFile = GUI.this.outFileName;
              // Move current tick to users desired end range
              if (endRange > currentTick) {
                boolean display = false;
                while (currentTick <= endRange) {
                  nextTick(display);
                  currentTick++;
                }
              }
              // Print grids within the range to the outfile
              String location = GUI.this.outDirectory + "/" + GUI.this.outFileName;
              for (int i = startRange; i <= endRange; i++) {
                int[][] outGrid = GUI.this.prevGrids.get(i);
                GUI.this.game.print(outGrid, i, location);
              }
              System.out.printf("Saving %d files to %s\n", endRange-startRange+1, GUI.this.outDirectory);
            }
          } catch(Exception exception) {
            JOptionPane.showMessageDialog(null, "Invalid input entered");
          }
        }
      });


      // Configuration panel set up and event on action
      config.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Config configurationPanel = new Config();
          String[] options = { "Submit", "Cancel" };
          int optionType = JOptionPane.DEFAULT_OPTION;
          int messageType = JOptionPane.PLAIN_MESSAGE;
          int reply = JOptionPane.showOptionDialog(null, configurationPanel, "Set Configurations",
                      optionType, messageType, null, options, options[0]);

          // Return if the 'cancel' or close button is hit
          if (reply == 1 || reply == -1) { return; }
          String directory = configurationPanel.getOutDirectory();
          String filePattern = configurationPanel.getText("Output File Pattern ");
          if (!directory.equals("."))
            GUI.this.outDirectory = directory;

          // Parse the file name input and see if it is valid
          boolean noInput = filePattern.equals("");
          if (!noInput && (filePattern != null) && !(filePattern.matches("^[a-zA-Z]*$"))) {
            JOptionPane.showMessageDialog(null, "Invalid file name entered, use only letters");
          } else if (noInput) {
            GUI.this.outFileName = GUI.this.outFileName;
          } else {
            GUI.this.outFileName = filePattern;
          }
        }
      });
      game.add(newGame);
      game.add(saveGameAll);
      game.add(saveGameRange);
      this.add(game);
      this.add(tools);
      this.add(config);
      this.add(goToTick);
    }


    /**
     *  A method to add a color selector to the toolbar.
     *  @param box - the JComboBox color selector
     *  @return None
     */
    public void addObject(JComboBox<String> box) {
      try {
        this.add(box);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }


// ============================================================================


  class TickControl extends JPanel {

    private JButton back;
    private JButton next;

    /**
     *  The constructor for TickControl takes in two colors and assigns
     *  them to the back and next buttons which are added to a JPanel.
     *  @param backColor - Sets the 'Back' button to backColor
     *  @param nextColor - Sets the 'Next' button to nextColor
     */
    public TickControl(Color backColor, Color nextColor) {
      this.back = new JButton("Back");
      this.next = new JButton("Next");
      this.back.setBackground(backColor);
      this.next.setBackground(nextColor);
      this.setLayout(new FlowLayout());
      add(back); add(next);

      // User selected the next button
      next.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          nextTick(true);
        }
      });

      // User selected the back button
      back.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int currentTick = GUI.this.statsPanel.getTick();
          if (currentTick == 0 && GUI.this.rowCount == 2) {
            JOptionPane.showMessageDialog(null, "You must load in a new game before stepping through it");
          } else if (GUI.this.statsPanel.getTick() == 0) {
            JOptionPane.showMessageDialog(null, "Can't go back from tick 0");
          } else {
            previousTick(true);
          }
        }
      });
    }
  }


// ============================================================================


  /**
   *  GUI Main Method
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(() -> {
      GUI gui = new GUI(2, 2);
      gui.createAndShowGUI();
    });
    System.out.println("App exited.");
  }
}
