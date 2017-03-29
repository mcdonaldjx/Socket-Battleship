/* Programmers: Jared McDonald
 *Class: CSC 435, Spring 2016
 *Instructor: Dr. Cook
 * Program Purpose:
 * a. Program takes in command line arguments of a hostname and port number.
 * b. Program connects to a socket created by a EchoServer using the hostname and port number
 * c. Program sends out a READY signal to the socket
 * d. Program waits for a READY signal from the socket (sent by a BattleshipHost object)
 * e. Program asks player to place ships on board
 * f. Program either:
 * 	i. Sends a move to the other player over the socket
 *  	ii. Recieves a move from the other player over the socket
 * g. Program sends/recieves a hit or miss over the socket
 * h. Program ends when either all ships have been destroyed or the battleship has been destroyed
 */
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class BattleshipClient { //Also known as EchoClient
	static String ships[] = {"Battleship (6 spaces)","Carrier (5 spaces)","Destroyer (4 spaces)","Submarine (3 spaces)","Patrol (2 spaces)"};
	public static char board[][] = new char[10][10];
	public static char fired[][] = new char[10][10];
	static int shipsplaced = 0;
	static Scanner sc = new Scanner(System.in);
	static int battleshiphealth = 6;
	static int carrierhealth = 5;
	static int destroyerhealth = 4;
	static int submarinehealth = 3;
	static int patrolhealth = 2;
	static int read = 0;
	static int row = 0;
	static String ship = null;
	static int col = 0;
	static int length = 0;
	static char mode = ' ';

	public static void main(String[] args) throws Exception {
		if(args.length != 2){
			System.err.println("Necessary command line arguments: <hostname> <portNumber>");
			System.exit(1);
		}
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);
		while(true){
			//SendMode
			try{
				Socket echoSocket = new Socket(hostName,portNumber);
				PrintWriter outSend = new PrintWriter(echoSocket.getOutputStream(), true);
				BufferedReader inSend = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				String userInput;
				outSend.println("READY");
				System.out.println("Waiting for READY signal...");
				while((userInput= inSend.readLine()) != null){		
					if(userInput.contains("READY")){
						System.out.println("READY Recieved");
						break;
					}
					else{
						outSend.println("READY");
					}
				}
				fillBoard();
				placement();
				shipsplaced = 5;
				System.out.println("Waiting to Recieve message...");
				while((userInput = inSend.readLine()) != null){
				
				//POINT B READING
					//At the point the server response is captured within userInput
					System.out.println("\tRecieved "+userInput);
					if(userInput.contains("MOVE")){
						shot(userInput,outSend);
					}
					else if(userInput.contains("BATTLESHIP")){ //Win condition
						System.out.println("You Win!");
						System.exit(0);
					}
					
					System.out.print("Type Message FORMAT( MOVE {A-J} {1-10} >>> ");
					printFired();
					userInput = stdIn.readLine();
					if(transmit() == 1){
						System.out.println("Sent "+userInput+" successfully.");
						updateFired(userInput);
						outSend.println(userInput);
						//Listen for Hit or miss
						userInput = inSend.readLine();
						System.out.println(userInput);
					}
					else{
						System.out.println("Tried to send "+userInput+" but it failed.");
						TimeUnit.SECONDS.sleep(10);
						outSend.println("TIMEOUT - EXPECTING A MOVE");

					}	
				//POINT A SENDING
					//User Input is withing the userInput variable at this point
					//The below function is the variable that is sent to the server
					
					System.out.println("Waiting to Recieve message...");
				}
				echoSocket.close();
			}
			catch(UnknownHostException e){
				System.err.println("Don't know about host "+hostName);
				System.exit(1);
			}
			catch(IOException e){
				System.err.println("Couldn't get I/O for the connection to "+hostName);
				System.exit(1);
			}
		}
	}
	public static int transmit(){
		int chance = 1 + (int)(Math.random() * ((10 - 1) + 1));
		if(chance > 1){
			return 1;
		}
		else{
			return 0;
		}
	}
	public static void updateFired(String str){ //RECIEVED HIT
		str = str.substring(5).trim();		
		char ch = str.charAt(0);
		String temp = str.substring(str.indexOf(" ")).trim();
		int col = Integer.parseInt(temp)-1;
		int row = (int)ch-65;
		fired[row][col] = '!';
	}
	public static void shot(String str, PrintWriter out){
		str = str.substring(4).trim();		
		char ch = str.charAt(0);
		String temp = str.substring(str.indexOf(" ")).trim();
		System.out.println("Shot receieved "+str);
		int col = Integer.parseInt(temp)-1;
		int row = (int)ch-65;
		char tile = board[row][col];
		if(tile != '~' && tile != '*' && tile != '!'){
			hit(tile,out);
			board[row][col] = '!';
		}
		else if(tile == '!'){
			System.out.println("Shot missed!");
			out.println("MISS.");
		}
		else{
			System.out.println("Shot missed!");
			out.println("MISS.");
			miss(row,col);
		}
	}
	public static void hit(char c,PrintWriter out){ //
		if(c == 'B'){
			battleshiphealth -= 1;
			if(battleshiphealth == 0){
				out.println("YOU SUNK MY BATTLESHIP. YOU WIN! GAME OVER.");
				System.out.println("YOU LOSE. BATTLESHIP SUNKEN.");
				System.exit(0);
			}
			else{
				System.out.println("Battleship hit!");
				out.println("HIT.");
			}
		}
		else if(c == 'C'){
			carrierhealth -= 1;
			if(carrierhealth == 0){
				shipsplaced--;
				out.println("YOU SUNK MY CARRIER. I HAVE "+shipsplaced+" SHIPS LEFT.");
			}
			else{
				System.out.println("Carrier hit!");
				out.println("HIT.");
			}
		}
		else if(c == 'D'){
			destroyerhealth -= 1;
			if(destroyerhealth == 0){
				shipsplaced--;
				out.println("YOU SUNK MY DESTROYER. I HAVE "+shipsplaced+" SHIPS LEFT.");
			}
			else{
				System.out.println("Destroyer hit!");
				out.println("HIT.");
			}
		}
		else if(c == 'S'){
			submarinehealth -= 1;
			if(submarinehealth == 0){
				shipsplaced--;
				out.println("YOU SUNK MY SUBMARINE. I HAVE "+shipsplaced+" SHIPS LEFT.");
			}
			else{
				System.out.println("Submarine hit!");
				out.println("HIT.");
			}
		}
		else if(c == 'P'){
			patrolhealth -= 1;
			if(patrolhealth == 0){
				shipsplaced--;
				out.println("YOU SUNK MY PATROL. I HAVE "+shipsplaced+" SHIPS LEFT.");
			}
			else{
				System.out.println("Patrol hit!");
				out.println("HIT.");
			}
		}
	}
	public static void printFired(){
		System.out.println("Tiles fired upon:");
		System.out.println("* = MISS, ~ = OPEN WATER, ! = HIT");
		for(int i = 0;i < 10;i++){
			System.out.print((char)(i+65)+" ");
			for(int j = 0;j < 10;j++){
				System.out.print(fired[i][j]+" ");
			}
			System.out.println();
		}
	}
	public static void placement(){
		while(shipsplaced != 5){
			System.out.println("You still have ships to place on the board");
			System.out.println("Press the number next to the ship you want to place.");
			for(int i = 0;i < 5;i++){
				if(ships[i] != null){
					System.out.println(i+". "+ships[i]);
				}
			}
			System.out.println("Ship  #: ");		
			read = sc.nextInt();
			if((read >= 11 || read < 0) && ships[read] != null){
				System.out.println("Invalid number.");
			}
			else{
				ship = ships[read];
				System.out.println("Where do you want to put your "+ships[read]+"? You can put it anywhere there is a ~ (open water).");
				System.out.println("  0 1 2 3 4 5 6 7 8 9");
				for(int i = 0;i < 10;i++){
					System.out.print(i+" ");
					for(int j = 0;j < 10;j++){
						System.out.print(board[i][j]+" ");
					}
					System.out.println();
				}
				boolean validspot = false;
				while(validspot == false){
					System.out.println("Which row do you want the head of the ship located? ");
					row = sc.nextInt();
					System.out.println("Row "+row+". Which column do you want the head of the ship located? ");
					col = sc.nextInt();
					System.out.println("Row "+row+", Column "+col+". Horizontally (enter H) or Vertically (enter V).");
					sc.nextLine();
					String temp = sc.nextLine();
					mode = temp.charAt(0);
					if((0 <= row && row <= 9) && (0 <= col && col <= 9) && (mode == 'h'||mode == 'v'||mode == 'H'||mode == 'V')){
						validspot = true;
					}
					else{
						System.err.println("Please enter a number 0-9 for the rows and columns and H or V for orientaion.");
					}
				}
				if(ship.contains("6")){
					length = 6;
				}
				else if(ship.contains("5")){
					length = 5;
				}
				else if(ship.contains("4")){
					length = 4;
				}
				else if(ship.contains("3")){
					length = 3;
				}
				else if(ship.contains("2")){
					length = 2;
				}
				if(TilesEmpty(row,col,length,mode) == true){
					addShip(row,col,ship,length,mode);
					System.out.println("Added "+ship+".");
					printBoard();
					shipsplaced++;
					ships[read] = null;
				}
				else{
					System.err.println("You cannot put a ship there in that position. Pick somewhere else.");
				}
			}
		}
	}
		public static void addShip(int row, int col, String ship, int length, char mode) {
			char letter = ship.charAt(0);
			if(mode == 'V'|| mode == 'v'){ //Vertical
				for(int i = row; i < (row+length);i++){
					board[i][col] = letter; 
				}
			}
			else if(mode == 'H' || mode == 'h'){ //Horizontal
				for(int i = col; i <(col+length);i++){
					board[row][i] = letter; 
				}
			}
		}
		public static boolean TilesEmpty(int row, int col, int length, char mode) {
			if(row+length > 9 ||col+length >9){
				return false;
			}
			else{
				if(mode == 'H'|| mode == 'h'){
					int max = row + length;
					for(int i = row;i < max;i++){
						if(board[i][col] != '~'){
							System.err.println("One or more tiles in the selection is occupied.");
							return false;
						}
						if(i == max-1){
							return true;
						}
					}
					return true;
				}
				else{
					int max = col + length;
					for(int i = col;i < max;i++){
						if(board[row][i] != '~'){
							System.err.println("One or more tiles in the selection is occupied.");
							return false;
						}
						if(i == max-1){
							return true;
						}
					}
					return true;
				}
			}
		}
	public static void fillBoard(){
		for(int i = 0;i < 10;i++){
			for(int j = 0;j < 10;j++){
				board[i][j] = '~';
				fired[i][j] = '~';
			}
		}
	}
	public static void miss(int r,int c){
		board[r][c] = '*';
		fired[r][c] = '*';
	}
	public static void printBoards(){
		System.out.println("Tiles fired upon:");
		System.out.println("* = MISS, ~ = OPEN WATER, ! = HIT");
		for(int i = 0;i < 10;i++){
			System.out.print(i+" ");
			for(int j = 0;j < 10;j++){
				System.out.print(fired[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println("Your board:");
		System.out.println("  0 1 2 3 4 5 6 7 8 9");
		for(int i = 0;i < 10;i++){
			System.out.print(i+" ");
			for(int j = 0;j < 10;j++){
				System.out.print(board[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println("* = MISS, ~ = OPEN WATER, ! = HIT");
		System.out.println("B = Battleship, C = Carrier, D = Destroyer");
		System.out.println("S = Submarine, P = Patrol\n");
	}
	public static void printBoard(){
		System.out.println("Your board:");
		System.out.println("  0 1 2 3 4 5 6 7 8 9");
		for(int i = 0;i < 10;i++){
			System.out.print(i+" ");
			for(int j = 0;j < 10;j++){
				System.out.print(board[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println("* = MISS, ~ = OPEN WATER, ! = HIT");
		System.out.println("B = Battleship, C = Carrier, D = Destroyer");
		System.out.println("S = Submarine, P = Patrol\n");
	}
}
