import java.awt.*;        // Uses AWT's Layout Managers
import java.awt.event.*;  // Uses AWT's Event Handlers
import javax.swing.*;     // Uses Swing's Container/Components
import javax.swing.border.*;
import java.util.Random;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import javax.swing.Timer;
import java.text.SimpleDateFormat;

 
/**
 * The Sudoku game.
 * To solve the number puzzle, each row, each column, and each of the
 * nine sub-grids shall contain all of the digits from 1 to 9
 */
public class Sudoku extends JFrame {
    // Name-constants for the game properties
    public static final int GRID_SIZE = 9;    // Size of the board
    public static final int SUBGRID_SIZE = 3; // Size of the sub-grid
 
    // Name-constants for UI control (sizes, colors and fonts)
    public static final int CELL_SIZE = 60;   // Cell width/height in pixels
    public static final int CANVAS_WIDTH  = CELL_SIZE * GRID_SIZE;
    public static final int CANVAS_HEIGHT = CELL_SIZE * GRID_SIZE;
                                             // Board width/height in pixels
    public static final Color OPEN_CELL_BGCOLOR = Color.YELLOW;
    public static final Color OPEN_CELL_TEXT_YES = new Color(0, 255, 0);  // RGB
    public static final Color OPEN_CELL_TEXT_NO = Color.RED;
    public static final Color CLOSED_CELL_BGCOLOR = Color.WHITE; // RGB
    public static final Color CLOSED_CELL_TEXT = Color.BLACK;
    public static final Color CONFLICT = new Color(0, 167,234);  // RGB
    public static final Color CONFLICT_CELL_TEXT = Color.WHITE;
    public static final Font FONT_NUMBERS = new Font("Monospaced", Font.BOLD, 20);
    public static final Border BLACKLINE = BorderFactory.createLineBorder(Color.BLACK);
 
    // The game board composes of 9x9 JTextFields,
    // each containing String "1" to "9", or empty String
    private JTextField[][] tfCells = new JTextField[GRID_SIZE][GRID_SIZE];
    // status bar
    private JPanel status = new JPanel(new BorderLayout());       // status panel in the south
    private JLabel statusBar = new JLabel();
    // progress bar
    private JProgressBar pbar = new JProgressBar();
    // timer
    private JPanel timerPanel = new JPanel(new FlowLayout());
    private final ClockListener clock = new ClockListener();
    private final Timer timer = new Timer(10, clock);
    private final JTextField tf = new JTextField(9);
    private static final String stop = "Stop";
    private static final String start = "Start";
    private static final String pause = "Pause";
    private static final String resume= "Resume";
    public int minutes;
    public int seconds;
    public int milliseconds;
    // other variables
    public int count = 5;
    public String difficulty;
    public int num;
 
    // Puzzle to be solved and the mask (which can be used to control the
    //  difficulty level).
    // Hardcoded here. Extra credit for automatic puzzle generation
    //  with various difficulty levels.
    private int[][] puzzle =
      {{5, 3, 4, 6, 7, 8, 9, 1, 2},
       {6, 7, 2, 1, 9, 5, 3, 4, 8},
       {1, 9, 8, 3, 4, 2, 5, 6, 7},
       {8, 5, 9, 7, 6, 1, 4, 2, 3},
       {4, 2, 6, 8, 5, 3, 7, 9, 1},
       {7, 1, 3, 9, 2, 4, 8, 5, 6},
       {9, 6, 1, 5, 3, 7, 2, 8, 4},
       {2, 8, 7, 4, 1, 9, 6, 3, 5},
       {3, 4, 5, 2, 8, 6, 1, 7, 9}};
    // For testing, open only 2 cells.
    private boolean[][] masks =
      {{false, false, false, false, false, true, false, false, false},
       {false, false, false, false, false, false, false, false, true},
       {false, false, false, false, false, false, false, false, false},
       {false, false, false, false, false, false, false, false, false},
       {false, false, true, false, false, false, false, false, false},
       {false, false, false, false, false, false, false, false, false},
       {false, false, false, true, false, false, false, false, false},
       {false, false, false, false, false, false, true, false, false},
       {false, false, false, false, false, false, false, false, false}};


    /**
      * Constructor to setup the game and the UI Components
    */
    public Sudoku() {
      JFrame overall = new JFrame();
      overall.setLayout(new BorderLayout());

      // container
      Container cp = getContentPane();
      cp.setLayout(new GridLayout(GRID_SIZE, GRID_SIZE));  // 9x9 GridLayout
      overall.add(cp, BorderLayout.CENTER);

      // status panel
      overall.add(status, BorderLayout.SOUTH);
      TitledBorder title = BorderFactory.createTitledBorder(BLACKLINE, "status");
      title.setTitleJustification(TitledBorder.CENTER);
      status.setBorder(title);
      status.setPreferredSize(new Dimension(CANVAS_WIDTH, 50));

      // status bar
      statusBar.setHorizontalAlignment(SwingConstants.CENTER);
      statusBar.setText("Fill the cells in yellow!");
      status.add(statusBar, BorderLayout.NORTH);

      // progress bar
      status.add(pbar, BorderLayout.SOUTH);
      pbar.setValue(0);

      // timer panel
      tf.setHorizontalAlignment(JTextField.CENTER);
      tf.setEditable(false);
      timerPanel.add(tf);
      timer.setInitialDelay(0);
      final JToggleButton btn2 = new JToggleButton(pause);
      final JToggleButton btn = new JToggleButton(start);
      btn.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (btn.isSelected()) {
                timeinit();
                timer.start();
                btn.setText(stop);
            } else {
                timer.stop();
                btn.setText(start);
            }
        }
    });

      btn2.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          if (btn2.isSelected()) {
                timer.stop();
                btn2.setText(resume);
            } else {
                timer.start();
                btn2.setText(pause);
            }
        }
      });

      timerPanel.add(btn);
      timerPanel.add(btn2);
      timerPanel.setPreferredSize(new Dimension(100, 100));
      overall.add(timerPanel, BorderLayout.EAST);


      // menu
      JMenuBar menuBar = new JMenuBar();
      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic(KeyEvent.VK_F);      // shortcut: Alt + F
      JMenu optionsMenu = new JMenu("Options");
      optionsMenu.setMnemonic(KeyEvent.VK_O);   // shortcut: Alt + O
      JMenu helpMenu = new JMenu("Help");
      helpMenu.setMnemonic(KeyEvent.VK_H);      // shortcut: Alt + H

      JMenuItem restart = new JMenuItem("New Game");
      restart.setMnemonic(KeyEvent.VK_N);
      JMenuItem reset = new JMenuItem("Reset Game");
      reset.setMnemonic(KeyEvent.VK_R);
      JMenuItem exit = new JMenuItem("Exit");
      exit.setMnemonic(KeyEvent.VK_E);

      JMenuItem easy = new JMenuItem("Easy");
      easy.setMnemonic(KeyEvent.VK_E);
      JMenuItem intermediate = new JMenuItem("Medium");
      intermediate.setMnemonic(KeyEvent.VK_M);
      JMenuItem difficult = new JMenuItem("Difficult");
      difficult.setMnemonic(KeyEvent.VK_D);

      JMenuItem reveal = new JMenuItem("Reveal a Cell");
      reveal.setMnemonic(KeyEvent.VK_R);

      // Set Shortcut
      restart.setAccelerator(KeyStroke.getKeyStroke('N', CTRL_DOWN_MASK));
      reset.setAccelerator(KeyStroke.getKeyStroke('S', CTRL_DOWN_MASK));
      exit.setAccelerator(KeyStroke.getKeyStroke('Z', CTRL_DOWN_MASK));
      easy.setAccelerator(KeyStroke.getKeyStroke('E', CTRL_DOWN_MASK));
      intermediate.setAccelerator(KeyStroke.getKeyStroke('M', CTRL_DOWN_MASK));
      difficult.setAccelerator(KeyStroke.getKeyStroke('D', CTRL_DOWN_MASK));
      reveal.setAccelerator(KeyStroke.getKeyStroke('R', CTRL_DOWN_MASK));


      fileMenu.add(restart);
      fileMenu.add(reset);
      fileMenu.add(exit);

      optionsMenu.add(easy);
      optionsMenu.add(intermediate);
      optionsMenu.add(difficult);

      helpMenu.add(reveal);

      menuBar.add(fileMenu);
      menuBar.add(optionsMenu);
      menuBar.add(helpMenu);

      overall.add(menuBar);
      overall.setJMenuBar(menuBar);
      
      // register MenuListener
      restart.addActionListener(new MenuListener());
      reset.addActionListener(new MenuListener());
      exit.addActionListener(new MenuListener());
      easy.addActionListener(new MenuListener());
      intermediate.addActionListener(new MenuListener());
      difficult.addActionListener(new MenuListener());
      reveal.addActionListener(new MenuListener());


      // Allocate a common listener as the ActionEvent listener for all the
      //  JTextFields
      // [TODO 3]
      InputListener listener = new InputListener();
 
      // Construct 9x9 JTextFields and add to the content-pane
      for (int row = 0; row < GRID_SIZE; ++row) {
         for (int col = 0; col < GRID_SIZE; ++col) {
            tfCells[row][col] = new JTextField(); // Allocate element of array
            cp.add(tfCells[row][col]);            // ContentPane adds JTextField
            if (masks[row][col]) {
               tfCells[row][col].setText("");     // set to empty string
               tfCells[row][col].setEditable(true);
               tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
 
               // Add ActionEvent listener to process the input
               // [TODO 4]
               tfCells[row][col].addActionListener(listener);
            } else {
               tfCells[row][col].setText(puzzle[row][col] + "");
               tfCells[row][col].setEditable(false);
               tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
               tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
            }
            // Beautify all the cells
            tfCells[row][col].setHorizontalAlignment(JTextField.CENTER);
            tfCells[row][col].setFont(FONT_NUMBERS);

            // Set border
            tfCells[row][col].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            if (row == 3 || row == 6) {
              tfCells[row][col].setBorder(BorderFactory.createMatteBorder(3, 1, 1, 1, Color.BLACK));
            }
            if (col == 3 || col == 6) {
              tfCells[row][col].setBorder(BorderFactory.createMatteBorder(1, 3, 1, 1, Color.BLACK));
            }
            if ((row == 3 || row == 6) && (col == 3 || col == 6)) {
              tfCells[row][col].setBorder(BorderFactory.createMatteBorder(3, 3, 1, 1, Color.BLACK));
            }
         }
      }
 
      // Set the size of the content-pane and pack all the components
      //  under this container.
      cp.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
      pack();
 
      // set overall frame
      overall.setSize(new Dimension(CANVAS_WIDTH+130, CANVAS_HEIGHT+130));
      overall.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Handle window closing
      overall.setTitle("Sudoku");
      overall.setVisible(true);
    }
 
    /** The entry main() entry method */
    public static void main(String[] args) {
      // set lookandfeel
      String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
      try { 
        UIManager.setLookAndFeel(lookAndFeel); 
      }
      catch (Exception e) { 
        System.out.println("Look and Feel not set"); 
      }

      new Sudoku01();
    }

    // Define the Listener Inner Class
    // [TODO 2]
    // Inner class to be used as ActionEvent listener for ALL JTextFields
    private class InputListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         // All the 9*9 JTextFileds invoke this handler. We need to determine
         // which JTextField (which row and column) is the source for this invocation.
         int rowSelected = -1;
         int colSelected = -1;
 
         // Get the source object that fired the event
         JTextField source = (JTextField)e.getSource();
         // Scan JTextFileds for all rows and columns, and match with the source object
         boolean found = false;
         for (int row = 0; row < GRID_SIZE && !found; ++row) {
            for (int col = 0; col < GRID_SIZE && !found; ++col) {
               if (tfCells[row][col] == source) {
                  rowSelected = row;
                  colSelected = col;
                  found = true;  // break the inner/outer loops
               }
            }
         }
 
         /*
          * [TODO 5]
          * 1. Get the input String via tfCells[rowSelected][colSelected].getText()
          * 2. Convert the String to int via Integer.parseInt().
          * 3. Assume that the solution is unique. Compare the input number with
          *    the number in the puzzle[rowSelected][colSelected].  If they are the same,
          *    set the background to green (Color.GREEN); otherwise, set to red (Color.RED).
          */
          String inStr = tfCells[rowSelected][colSelected].getText();
          int userInput = Integer.parseInt(inStr);
          if (userInput == puzzle[rowSelected][colSelected]) {
            tfCells[rowSelected][colSelected].setBackground(OPEN_CELL_TEXT_YES);
            // Clear other cell's colors
            for(int row=0; row<GRID_SIZE; row++) {
              for(int col=0; col<GRID_SIZE; col++) {
                Color c = tfCells[row][col].getBackground();
                if(c==CONFLICT) {
                  tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
                  tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
                }
              }
            }
          } else {
            tfCells[rowSelected][colSelected].setBackground(OPEN_CELL_TEXT_NO);

            for(int col=0; col<GRID_SIZE; col++) {
              // Clear previous conflict cells' colors
              for(int row=0; row<GRID_SIZE; row++) {
                Color c = tfCells[row][col].getBackground();
                if(c==CONFLICT) {
                  tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
                  tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
                }
              }
            }

            // Show the conflict cells
            // same row
            for(int col=0; col<GRID_SIZE; col++) {
              Color c = tfCells[rowSelected][col].getBackground();
              if(puzzle[rowSelected][col] == userInput && c == CLOSED_CELL_BGCOLOR) {
                tfCells[rowSelected][col].setBackground(CONFLICT);
                tfCells[rowSelected][col].setForeground(CONFLICT_CELL_TEXT);
              } 
            }

            // same col
            for(int row=0; row<GRID_SIZE; row++) {
              Color c = tfCells[row][colSelected].getBackground();
              if(puzzle[row][colSelected] == userInput && c == CLOSED_CELL_BGCOLOR) {
                tfCells[row][colSelected].setBackground(CONFLICT);
                tfCells[row][colSelected].setForeground(CONFLICT_CELL_TEXT);
              } 
            }

            // same sub-grid
            int subGridRow = rowSelected/3;
            int subGridCol = colSelected/3;
            for(int row=subGridRow*3; row <= subGridRow*3+2; row++) {
              for(int col=subGridCol*3; col <= subGridCol*3+2; col++) {
                  Color c = tfCells[row][col].getBackground();
                  if(puzzle[row][col] == userInput && c == CLOSED_CELL_BGCOLOR) {
                  tfCells[row][col].setBackground(CONFLICT);
                  tfCells[row][col].setForeground(CONFLICT_CELL_TEXT);
                  }
              }
            }
          }          

         /* [TODO 6]
          * Check if the player has solved the puzzle after this move.
          * You could update the masks[][] on correct guess, and check the masks[][] if
          * any input cell pending.
          */
         if (userInput != puzzle[rowSelected][colSelected]) {
            masks[rowSelected][colSelected] = true;
         } else if (userInput == puzzle[rowSelected][colSelected]) {
            masks[rowSelected][colSelected] = false;
         }

         boolean finish = true;
         count = 0;
         for (int row = 0; row < GRID_SIZE; ++row) {
            for (int col = 0; col < GRID_SIZE; ++col) {
              if(masks[row][col]!=false) {
                finish = false;
                count++;
              }
            }
         }

         // update status bar
         statusBar.setText("You have " + count + " cells to fill in");
         // update progress bar
         updateProgress(difficulty, count);

         if (finish) {
            statusBar.setText("You have finished this sudoku!");
            JOptionPane.showMessageDialog(null, "Congratulations! : )");
         }
      }
    }

    private class MenuListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent evt) {
        String menuLabel = evt.getActionCommand();
        if (menuLabel.equals("New Game")) {
            initMasks();
            if (difficulty == "intermediate") {
              randomMasks(30);
              statusBar.setText("You have 30 cells to fill in");
              pbar.setValue(0);
            } else if (difficulty == "difficult") {
              randomMasks(50);
              statusBar.setText("You have 50 cells to fill in");
              pbar.setValue(0);
            } else {
              randomMasks(5);
              statusBar.setText("You have 5 cells to fill in");
              pbar.setValue(0);
            }
        } else if (menuLabel.equals("Reset Game")) {
            for(int row=0; row<GRID_SIZE; row++) {
              for(int col=0; col<GRID_SIZE; col++) {
                Color c = tfCells[row][col].getBackground();
                if(masks[row][col] && c!=CLOSED_CELL_BGCOLOR || c==OPEN_CELL_TEXT_YES) {
                  tfCells[row][col].setText("");
                  tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
                } else {
                  tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
                  tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
                }
              }
            }
            if(difficulty == "intermediate") {statusBar.setText("You have 30 cells to fill in");}
            else if(difficulty == "difficult") {statusBar.setText("You have 50 cells to fill in");}
            else {statusBar.setText("You have 5 cells to fill in");}
            pbar.setValue(0);
        } else if (menuLabel.equals("Exit")) {System.exit(0);}
          else if (menuLabel.equals("Easy")) {
            difficulty = "easy";
            initMasks();
            randomMasks(5);
            statusBar.setText("You have 5 cells to fill in");
            pbar.setValue(0);
        } else if (menuLabel.equals("Medium")) {
            difficulty = "intermediate";
            initMasks();
            randomMasks(30);
            statusBar.setText("You have 30 cells to fill in");
            pbar.setValue(0);
        } else if (menuLabel.equals("Difficult")) {
            difficulty = "difficult";
            initMasks();
            randomMasks(50);
            statusBar.setText("You have 50 cells to fill in");
            pbar.setValue(0);
        } else if (menuLabel.equals("Reveal a Cell")) {
            boolean flag = true;
            for(int row=0; row<GRID_SIZE; row++) {
              for(int col=0; col<GRID_SIZE; col++) {
                if(masks[row][col] && flag) {
                  tfCells[row][col].setText(puzzle[row][col] + "");
                  tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
                  tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
                  tfCells[row][col].setEditable(false);
                  masks[row][col] = false;
                  flag = false;
                }
              }
            }
            count--;
            // update status bar
            statusBar.setText("You have " + count + " cells to fill in");
            // update progress bar
            updateProgress(difficulty, count);
          }
        }
      }

    private class ClockListener implements ActionListener{
      @Override
      public void actionPerformed(ActionEvent e) {
        SimpleDateFormat date = new SimpleDateFormat("mm.ss.SS");

        if (milliseconds == 100) 
        {
            milliseconds = 00;
            seconds++;
        }
        if (seconds == 60) {
            seconds = 00;
            minutes++;
        }

        tf.setText(String.format("%02d:%02d:%02d", minutes, seconds, milliseconds));
        milliseconds++;
      }
    }

    // Set all the masks false
    public void initMasks() {
        for(int row=0; row<GRID_SIZE; row++) {
          for(int col=0; col<GRID_SIZE; col++) {
            masks[row][col] = false;
          }
        }
    }

    public void randomMasks(int num) {
        Random rand = new Random();
        int[] arr_row = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        int[] arr_col = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        int i = 0;
        while (i<num) {
          int r = rand.nextInt(9);
          int c = rand.nextInt(9);
          if (!masks[arr_row[r]][arr_col[c]]) {
            masks[arr_row[r]][arr_col[c]] = true;
            i++;
          } else {
            r = rand.nextInt(9);
            c = rand.nextInt(9);
          }
        }
  
            int n = rand.nextInt(10);
            n += 1;
            for (int row = 0; row < GRID_SIZE; ++row) {
              for (int col = 0; col < GRID_SIZE; ++col) {
                puzzle[row][col] += n;
                puzzle[row][col] = puzzle[row][col] % 9 + 1;
                if (masks[row][col]) {
                  tfCells[row][col].setText("");
                  tfCells[row][col].setEditable(true);
                  tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
                  tfCells[row][col].addActionListener(new InputListener());
                } else {
                  tfCells[row][col].setText(puzzle[row][col] + "");
                  tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
                  tfCells[row][col].setEditable(false);
                  tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
                }
              }
            }
    }

    public void updateProgress(String difficulty, int count) {
      int total = 0;
      if (difficulty == "intermediate") {
        total = 30;
      } else if (difficulty == "difficult") {
        total = 50;
      } else {
        total = 5;
      }
      pbar.setMaximum(total);
      pbar.setValue(total - count);
    }

    public void timeinit(){
          minutes = 0;
          seconds = 0;
          milliseconds = 0;
    }
}
