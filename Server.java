// 2460681S, Wesley Scott

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors; // implementation of threading

public class Server{
    public static void main(String[] args) throws Exception {
        try (var listener = new ServerSocket(9876)) {
            System.out.println("---------------------------------");
            System.out.println("| netDraughts server is running |");
            System.out.println("---------------------------------");
            var pool = Executors.newFixedThreadPool(2); // create thread pool size of 2
            while (true) {
                Game game = new Game(); // new instance of data model

                // create new player instances in the game on their own thread
                pool.execute(game.new Player(listener.accept(), 'r'));
                System.out.println("Player 'r' connected...\n");
                pool.execute(game.new Player(listener.accept(), 'b'));
                System.out.println("Player 'b' connected...\n");
                game.setupBoard(); // initialise game board with pieces
            }
        }
    }
}

class Game{
    // Board cells numbered 0-8, top to bottom, left to right; null if empty
    private Player[][] board = new Player[8][8];
    Player currentPlayer;

    // populates the board with player pieces
    public void setupBoard(){
        int i;
        int j;
        for(i = 0; i < 8; i++){
            for(j = 0; j < 8; j++){
                board[j][i] = null;
            }
        }
        for(i = 1; i < 8; i += 2) {
            board[1][i] = currentPlayer.opponent;
            board[5][i] = currentPlayer;
            board[7][i] = currentPlayer;
        }
        for(i = 0; i < 8; i += 2) {
            board[0][i] = currentPlayer.opponent;
            board[2][i] = currentPlayer.opponent;
            board[6][i] = currentPlayer;
        }
    }

//    public boolean hasWinner() {
//
//    }

    // Move logic handled here, checks for legal/illegal movements
    public synchronized void move(int moveFrom1, int moveFrom2, int moveTo1, int moveTo2, Player player) {
        if (player != currentPlayer) {
            throw new IllegalStateException("Not your turn...");
        } else if (player.opponent == null) {
            throw new IllegalStateException("You don't have an opponent...");
        } else if (board[moveTo1][moveTo2] != null) {
            throw new IllegalStateException("Board square already occupied...");
        } else if (board[moveFrom1][moveFrom2] == null) {
            throw new IllegalStateException("Board square has no piece...");
        } else if (board[moveFrom1][moveFrom2] != currentPlayer) {
            throw new IllegalStateException("Cannot move opponents pieces...");
        }

        // force player to jump mechanic
        // force r player to jump
        if(currentPlayer.mark == 'b') {
            for (int i = 0; i <= 5; i++) {
                for (int j = 0; j <= 7; j++) {
                    if (board[i][j] == currentPlayer) {
                        if (j >= 6) { // far right
                            // check left
                            if (board[i + 1][j - 1] == currentPlayer.opponent) {
                                if (board[i + 2][j - 2] == null) {
                                    if (board[i + 2][j] == null) {
                                        if (moveFrom1 == i && (moveTo2 == (j - 2) || moveTo2 == j)) {
                                            break;
                                        } else throw new IllegalStateException("Jump must be made... r1 " + j);
                                    }
                                    if (moveFrom1 == i && moveTo2 == (j - 2)) {
                                        break;
                                    } else throw new IllegalStateException("Jump must be made... r2 " + j);
                                }
                            }
                        } else if (j <= 1) { // far left
                            // check right
                            if (board[i + 1][j + 1] == currentPlayer.opponent) {
                                if (board[i + 2][j + 2] == null) {
                                    if (board[i + 2][j] == null) {
                                        if (moveFrom1 == i && (moveTo2 == (j + 2) || moveTo2 == j)) {
                                            break;
                                        } else throw new IllegalStateException("Jump must be made... r3 " + j);
                                    }
                                    if (moveFrom1 == i && moveTo2 == (j + 2)) {
                                        break;
                                    } else throw new IllegalStateException("Jump must be made... r4 " + j);
                                }
                            }
                        } else {
                            // // edge case where 2 jumps are possible
                            // if(board[i+1][j+1] == currentPlayer.opponent && board[i + 1][j - 1] == currentPlayer.opponent){
                            //     if(board[i+2][j+2] == null && board[i+2][j-2] == null){
                            //         if(moveTo1 != (i+2)){
                            //             throw new IllegalStateException("Jump must be made... r3 " + j);
                            //         }
                            //     }
                            // check right
                            if (board[i + 1][j + 1] == currentPlayer.opponent) {
                                if (board[i + 2][j + 2] == null) {
                                    if (board[i + 2][j] == null) {
                                        if (moveFrom1 == i && (moveTo2 == (j + 2) || moveTo2 == j)) {
                                            break;
                                        } else throw new IllegalStateException("Jump must be made... r5 " + j);
                                    }
                                    if (moveFrom1 == i && moveTo2 == (j + 2)) {
                                        break;
                                    } else throw new IllegalStateException("Jump must be made... r6 " + j);
                                }
                                // check left
                            } else if (board[i + 1][j - 1] == currentPlayer.opponent) {
                                if (board[i + 2][j - 2] == null) {
                                    if (board[i + 2][j] == null) {
                                        if (moveFrom1 == i && (moveTo2 == (j - 2) || moveTo2 == j)) {
                                            break;
                                        } else throw new IllegalStateException("Jump must be made... r7 " + j);
                                    }
                                    if (moveFrom1 == i && moveTo2 == (j - 2)) {
                                        break;
                                    } else throw new IllegalStateException("Jump must be made... r8 " + j);

                                }
                            }
                        }
                    }
                }
            }
         // force b player to jump
        } else if(currentPlayer.mark == 'r') {
            for (int i = 2; i <= 7; i++) {
                for (int j = 0; j <= 7; j++) {
                    if (board[i][j] == currentPlayer) {
                        if (j >= 6) { // far right
                            // check left
                            if (board[i - 1][j - 1] == currentPlayer.opponent) {
                                if (board[i - 2][j - 2] == null) {
                                	if(board[i-2][j] == null){
                                		if (moveFrom1 == i && (moveTo2 == (j-2) || moveTo2 == j)){
                                			break;
                                		} else throw new IllegalStateException("Jump must be made... b1 " + j);
                                	}
                                    if (moveFrom1 == i && moveTo2 == (j-2)) {
                                        break;
                                    } else throw new IllegalStateException("Jump must be made... b2 " + j);
                                }
                            }
                        } else if (j <= 1) { // far left
                            // check right
                            if (board[i - 1][j + 1] == currentPlayer.opponent) {
                                if (board[i - 2][j + 2] == null) {
                                    if(board[i - 2][j] == null){
                                		if (moveFrom1 == i && (moveTo2 == (j+2) || moveTo2 == j)){
                                			break;
                                		} else throw new IllegalStateException("Jump must be made... b3 " + j);
                                	}
                                    if (moveFrom1 == i && moveTo2 == (j+2)) {
                                        break;
                                    }  else throw new IllegalStateException("Jump must be made... b4 " + j);
                                }
                            }
                        } else {
                            // edge case where 2 jumps are possible
                            // if(board[i - 1][j + 1] == currentPlayer.opponent && board[i - 1][j - 1] == currentPlayer.opponent){
                            //     if (board[i - 2][j + 2] == null && board[i - 2][j - 2] == null) {
                            //         if(moveTo1 == (i-2) && (moveTo2 == (j+2) || moveTo2 == (j-2))){
                            //            break;
                            //         } else throw new IllegalStateException("Jump must be made... b3 " + j);
                            //     }
                            //     // check right
                            if (board[i - 1][j + 1] == currentPlayer.opponent) {
                                if (board[i - 2][j + 2] == null) {
                                	if(board[i - 2][j] == null){
                                		if (moveFrom1 == i && (moveTo2 == (j+2) || moveTo2 == j)){
                                			break;
                                		} else throw new IllegalStateException("Jump must be made... b5 " + j);
                                	}
                                    if (moveFrom1 == i && moveTo2 == (j+2)) {
                                        break;
                                    } else throw new IllegalStateException("Jump must be made... b6 " + j);
                                }
                                // check left
                            } else if (board[i - 1][j - 1] == currentPlayer.opponent) {
                                if (board[i - 2][j - 2] == null) {
                                	if(board[i-2][j] == null){
                                		if (moveFrom1 == i && (moveTo2 == (j-2) || moveTo2 == j)){
                                			break;
                                		} else throw new IllegalStateException("Jump must be made... b7 " + j);
                                	}
                                    if (moveFrom1 == i && moveTo2 == (j-2)) {
                                        break;
                                    } else throw new IllegalStateException("Jump must be made... b8 " + j);
                                }
                            }
                        }
                    }
                }
            }
        }

        // check for legal move made
        // standard one square move
        if ((currentPlayer.mark == 'b' &&
                    (moveTo1 - moveFrom1) == 1 &&
                    Math.abs(moveTo2 - moveFrom2) == 1) ||
                (currentPlayer.mark == 'r' &&
                        (moveTo1 - moveFrom1) == -1 &&
                        Math.abs(moveTo2 - moveFrom2) == 1)) {
            board[moveFrom1][moveFrom2] = null;
            board[moveTo1][moveTo2] = currentPlayer;
            currentPlayer = currentPlayer.opponent;
        // jump move
        } else if((currentPlayer.mark == 'b' &&
                    (moveTo1 - moveFrom1) == 2 &&
                    Math.abs(moveTo2 - moveFrom2) == 2 &&
                    board[(moveTo1+moveFrom1)/2][Math.abs(moveTo2+moveFrom2)/2].mark == 'r') ||
                (currentPlayer.mark == 'r' &&
                        (moveTo1 - moveFrom1) == -2 &&
                        Math.abs(moveTo2 - moveFrom2) == 2 &&
                        board[(moveTo1+moveFrom1)/2][Math.abs(moveTo2+moveFrom2)/2].mark == 'b')){
            board[moveFrom1][moveFrom2] = null;
            board[(moveTo1+moveFrom1)/2][(moveTo2+moveFrom2)/2] = null;
            board[moveTo1][moveTo2] = currentPlayer;
            // multi-jump status must come before turn swap
            currentPlayer = currentPlayer.opponent;
        } else{
            throw new IllegalStateException(
                    "Invalid move " + currentPlayer.mark +
                            " From: " + moveFrom1 + "" + moveFrom2 +
                            " To: " + moveTo1 + "" + moveTo2);
        }
    }

    class Player implements Runnable{
        char mark;
        Player opponent;
        int checkers;
        Socket socket;
        Scanner input;
        PrintWriter output;

        public Player(Socket socket, char mark) {
            this.socket = socket; // connection to server
            this.mark = mark; // either 'r' for red or 'b' for black
            try{
                setup(); // assign current and opponent player
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void run(){ // client thread run
            try {
                processCommands();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (opponent != null && opponent.output != null) {
                    opponent.output.println("OTHER_PLAYER_LEFT");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                	e.printStackTrace();
                }
            }
        }

        private void setup() throws IOException {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("WELCOME " + mark);
            if (mark == 'r') {
                currentPlayer = this;
                currentPlayer.checkers = 12;
                output.println("MESSAGE Waiting for opponent");
            } else {
                opponent = currentPlayer;
                opponent.checkers = 12;
                opponent.opponent = this;
                opponent.output.println("MESSAGE Your turn");
            }
        }

        private void processCommands(){ // reads encoded messages from client
            while (input.hasNextLine()){
                var command = input.nextLine();
                if (command.startsWith("QUIT")) {
                    return;
                } else if (command.startsWith("MOVE")) {
                    processMoveCommand(
                            Integer.parseInt(command.substring(5, 6)),
                            Integer.parseInt(command.substring(6, 7)),
                            Integer.parseInt(command.substring(7, 8)),
                            Integer.parseInt(command.substring(8, 9))
                    );
                }
            }
        }

        private void processMoveCommand(int moveFrom1, int moveFrom2, int moveTo1, int moveTo2){
            try {
                move(moveFrom1, moveFrom2, moveTo1, moveTo2, this);
                output.println("VALID_MOVE");
                opponent.output.println("OPPONENT_MOVED " + moveFrom1 + moveFrom2 + moveTo1 + moveTo2);
                // win condition
//                if (hasWinner()) {
//                    output.println("VICTORY");
//                    opponent.output.println("DEFEAT");
                // tie condition
//                } else if (boardFilledUp()) { no possible moves
//                    output.println("TIE");
//                    opponent.output.println("TIE");
//                }
            } catch (IllegalStateException e) {
                output.println("MESSAGE " + e.getMessage());
            }
        }
    }
}