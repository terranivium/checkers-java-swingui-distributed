// 2460681S, Wesley Scott

import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Scanner;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Client {
    private JFrame frame = new JFrame("| netDraughts - client ver. 1.0 |");
    private JLabel messageLabel = new JLabel(" ");

    private Square[][] board = new Square[8][8];
    private Square moveFrom;
    private Square moveTo;
    private Square moveJump;
    private int fromCol;
    private int fromRow;

    private Socket socket;
    private Scanner in;
    private PrintWriter out;

    public Client(String serverAddress) throws Exception{
        socket = new Socket(serverAddress, 9876);
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);

        messageLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(messageLabel, BorderLayout.SOUTH);

        // draw client's view of game board
        var boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);
        boardPanel.setBounds(0,0,600,600);
        boardPanel.setLayout(new GridLayout(8, 8, 2, 2));
        for (var i = 0; i < board[0].length; i++){
            final int k = i;
            for (var j = 0; j < board[0].length; j++){
                final int l = j;
                if ((i % 2 == 0 && j % 2 == 0) || (i % 2 == 1 && j % 2 == 1)){
                    board[i][j] = new Square();
                    board[i][j].setColour(false);
                } else {
                    board[i][j] = new Square();
                    board[i][j].setColour(true);
                }
                boardPanel.add(board[k][l]);
            }
            frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
        }
        // mouse control interface
        boardPanel.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e) {
                    fromCol = e.getX() / 75;
                    fromRow = e.getY() / 72;
                    moveFrom = board[fromRow][fromCol];
            }
            public void mouseReleased(MouseEvent e){
                    int col = e.getX() / 75;
                    int row = e.getY() / 72;
                    moveTo = board[row][col];
                    moveJump = board[(fromRow+row)/2][(fromCol+col)/2];
                    out.println("MOVE " + fromRow + fromCol + row + col);
            }
        });
    }

    // populate board view
    public void setup(){
        int i;
        for(i = 1; i < 8; i += 2){
            board[1][i].setText('b');
            board[5][i].setText('r');
            board[7][i].setText('r');
        }
        for(i = 0; i < 8; i += 2){
            board[0][i].setText('b');
            board[2][i].setText('b');
            board[6][i].setText('r');
        }
    }

    // client runtime
    public void play() throws Exception{
        try{
            var response = in.nextLine();
            var mark = response.charAt(8);
            var opponentMark = mark == 'r' ? 'b' : 'r';
            frame.setTitle("| netDraughts: Player " + mark + " |");

            setup();

            while (in.hasNextLine()) { // reads encoded messages from server
                response = in.nextLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Valid move, please wait");
                    moveFrom.setText(' ');
                    moveJump.setText(' ');
                    moveTo.setText(mark);
                    moveFrom.repaint();
                    moveJump.repaint();
                    moveTo.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    var opMoveFrom1 = Integer.parseInt(response.substring(15,16));
                    var opMoveFrom2 = Integer.parseInt(response.substring(16,17));
                    var opMoveTo1 = Integer.parseInt(response.substring(17,18));
                    var opMoveTo2 = Integer.parseInt(response.substring(18,19));
                    board[opMoveFrom1][opMoveFrom2].setText(' ');
                    board[(opMoveFrom1+opMoveTo1)/2][(opMoveFrom2+opMoveTo2)/2].setText(' ');
                    board[opMoveTo1][opMoveTo2].setText(opponentMark);
                    board[opMoveFrom1][opMoveFrom2].repaint();
                    board[(opMoveFrom1+opMoveTo1)/2][(opMoveFrom2+opMoveTo2)/2].repaint();
                    board[opMoveTo1][opMoveTo2].repaint();
                    messageLabel.setText("Opponent moved - your turn");
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                } else if (response.startsWith("VICTORY")) {
                    JOptionPane.showMessageDialog(frame, "You Win!");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    JOptionPane.showMessageDialog(frame, "You lost...");
                    break;
                } else if (response.startsWith("TIE")) {
                    JOptionPane.showMessageDialog(frame, "Game has ended in a tie...");
                    break;
                } else if (response.startsWith("OTHER_PLAYER_LEFT")) {
                    messageLabel.setText("Other player left");
                }
            }
            out.println("QUIT");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
            frame.dispose();
        }
    }

    static class Square extends JPanel{ // game board square
        JLabel label = new JLabel();

        public Square(){
            setLayout(new GridBagLayout());
            label.setFont(new Font("Terminal", Font.BOLD, 38));
            add(label);
        }

        // used to alternate square colours to draw board
        public void setColour(boolean squareColour){
            if(squareColour){
                setBackground(Color.white);
            } else setBackground(Color.black);
        }

        public void setText(char text){
            label.setForeground(text == 'r' ? Color.RED : Color.BLUE);
            label.setText(text + "");
        }
    }

    public static void main(String[] args) throws Exception{
        Client client = new Client("127.0.0.1"); // IP hardcoded to local play for submission purposes
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setSize(600, 600);
        client.frame.setVisible(true);
        client.frame.setResizable(false);
        client.play();
    }
}