import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class EchoServer {
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

    public static void main(String args[]) throws Exception{
    	if(args.length != 1){
    		System.err.println("Usage: java EchoServer <port number>");
    		System.exit(1);
    	}
    	int portNumber = Integer.parseInt(args[0]);
	PrintWriter out = null;
    	//Recieve Mode
    	try{
    		ServerSocket serverSocket = new ServerSocket(portNumber);
    		Socket clientSocket = serverSocket.accept();
    		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    		out = new PrintWriter(clientSocket.getOutputStream(), true);
		String inputLine;
		out.println("READY");	
		System.out.println("Waiting for READY signal....");
		while((inputLine = in.readLine()) != null){
			
			if(inputLine.contains("READY")){
				System.out.println("READY Recieved");
				break;
			}
			else{
				out.println("READY");
			}
		}
		fillBoard();
		placement();
		shipsplaced = 5;
    		System.out.print("Type Message FORMAT( MOVE {A-J} {1-10} >>> ");
		printFired();
    		while((inputLine = stdIn.readLine()) != null){
			if(transmit() == 1){
				out.println(inputLine);
				System.out.println("Sent "+inputLine+" successfully.");
				//Listen for hit/miss
				inputLine = in.readLine();
				System.out.println("\t"+inputLine);
			}
			else{
				System.out.println("Tried to send "+inputLine+" but it failed.");
				TimeUnit.SECONDS.sleep(10);
				out.println("TIMEOUT - EXPECTING A MOVE");
			}
			
			System.out.println("Waiting to Recieve message...");
			inputLine = in.readLine();
    			System.out.println("\t"+inputLine);
			if(inputLine.contains("BATTLESHIP")){ //Win condition
				System.out.println("You Win!");
				System.exit(0);
			}
			else if(inputLine.contains("MOVE ")){
				shot(inputLine,out);
			}
    			System.out.print("Type Message FORMAT( MOVE {A-J} {1-10} >>> ");
			printFired();
    			
    		}
    	}
    	catch(IOException e){
    		System.out.println("Exception caught when trying to listen on port "+portNumber+" or listening for a connection");
    		System.out.println(e.getMessage());
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
	public static void fillBoard(){
		for(int i = 0;i < 10;i++){
			for(int j = 0;j < 10;j++){
				board[i][j] = '~';
				fired[i][j] = '~';
			}
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

	public static void printBoards(){
		System.out.println("Tiles fired upon:");
		System.out.println("* = MISS, ~ = OPEN WATER, ! = HIT");
		for(int i = 0;i < 10;i++){
			System.out.print((char)(i+65)+" ");
			for(int j = 0;j < 10;j++){
				System.out.print(fired[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println("Your board:");
		System.out.println("  1 2 3 4 5 6 7 8 9 10");
		for(int i = 0;i < 10;i++){
			System.out.print((char)(i+65)+" ");
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

	public static void hit(char c,PrintWriter out){ //
		if(c == 'B'){
			battleshiphealth--;
			if(battleshiphealth == 0){
				out.println("YOU SUNK MY BATTLESHIP. YOU WIN! GAME OVER.");
				System.out.println("YOU LOSE. BATTLESHIP SUNKEN.");
				System.exit(0);
			}
			else{
				out.println("HIT.");
			}
		}
		else if(c == 'C'){
			carrierhealth--;
			if(carrierhealth == 0){
				shipsplaced--;
				out.println("YOU SUNK MY CARRIER. I HAVE "+shipsplaced+" SHIPS LEFT.");
			}
			else{
				out.println("HIT.");
			}
		}
		else if(c == 'D'){
			destroyerhealth--;
			if(destroyerhealth == 0){
				shipsplaced--;
				out.println("YOU SUNK MY DESTROYER. I HAVE "+shipsplaced+" SHIPS LEFT.");
			}
			else{
				out.println("HIT.");
			}
		}
		else if(c == 'S'){
			submarinehealth--;
			if(submarinehealth == 0){
				shipsplaced--;
				out.println("YOU SUNK MY SUBMARINE. I HAVE "+shipsplaced+" SHIPS LEFT.");
			}
			else{
				out.println("HIT.");
			}
		}
		else if(c == 'P'){
			patrolhealth--;
			if(patrolhealth == 0){
				shipsplaced--;
				out.println("YOU SUNK MY PATROL. I HAVE "+shipsplaced+" SHIPS LEFT.");
			}
			else{
				out.println("HIT.");
			}
		}
	}
	public static void miss(int r,int c){
		board[r][c] = '*';
		fired[r][c] = '*';
	}

}