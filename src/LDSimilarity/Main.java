import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		
		try {
			File file = new File("source.txt");
			Scanner sc = new Scanner(file);
			String source= sc.next();
			
			file = new File("target.txt");
			sc = new Scanner(file);
			String target = sc.next();
			
			System.out.printf("The two strings are %5.2f%% similar%n", 100*similarity(source,target) );
			
			sc.close();
		}catch( FileNotFoundException ex ) {
			System.out.println("Could not read one of the files");
		}
		
		System.out.println("Developed by KamikazeGV");
	}
	
	public static int algorithm1(String source, String target) {
		return algorithm1Rec(source, source.length()-1, target, target.length()-1);
	}
	
	public static int algorithm1Rec(String source, int i, String target, int j) {		//Simply uses the formula for Levenshtein distance
		
		//Termination conditions
		if(i==-1) {
			return j+1;
		}
		else if(j==-1) {
			return i+1;
		}
		else {
			Integer[] terms= new Integer[3];		//Used to find the minimum of 3 terms
			
			terms[0]=algorithm1Rec(source, i-1, target, j)+1;
			terms[1]=algorithm1Rec(source, i, target, j-1)+1;
			if( source.charAt(i) == target.charAt(j) ) {
				terms[2]=algorithm1Rec(source, i-1, target, j-1);
			}
			else{
				terms[2]=algorithm1Rec(source, i-1, target, j-1)+1;
			}
			
			return Collections.min( Arrays.asList(terms) );
		}
	}
	
	public static int algorithm2(String source, String target) {
		int[][] matrix=new int[source.length()+1][target.length()+1];	//This is where the resulting matrix will end up
		
		//Initialize the matrix
		for(int[] row: matrix) {
			Arrays.fill(row, -1);
		}
		
		algorithm2Rec(source, source.length()-1, target, target.length()-1, matrix);
		return matrix[source.length()][target.length()];
	}
	
	public static void algorithm2Rec( String source, int i, String target, int j, int[][] matrix) {	// Uses dynamic programming to avoid recomputing certain 
																																									// "sub" Levenshtein distances. This is called Wagner–Fischer
		if(i==-1) {																																			// algorithm
			matrix[i+1][j+1]= j+1;
		}
		else if(j==-1) {
			matrix[i+1][j+1]= i+1;
		}
		else {
			Integer[] terms= new Integer[3];
			
			if( matrix[i][j+1] == -1) {
				algorithm2Rec( source, i-1,target, j, matrix);
			}
			terms[0]=matrix[i][j+1]+1;
			
			if( matrix[i+1][j] == -1) {
				algorithm2Rec( source, i,target, j-1, matrix);
			}
			terms[1]=matrix[i+1][j]+1;
			
			
			if( source.charAt(i) == target.charAt(j) ) {
				if( matrix[i][j] == -1) {
					algorithm2Rec( source, i-1,target, j-1, matrix);
				}
				terms[2]=matrix[i][j];
			}
			else{
				if( matrix[i][j] == -1) {
					algorithm2Rec( source, i-1,target, j-1, matrix);
				}
				terms[2]=matrix[i][j]+1;
			}
			
			matrix[i+1][j+1]=Collections.min( Arrays.asList(terms) );
		}
	}
	
	public static int algorithm3(String source, String target) {					//Since algorithm2 does NOT use tail recursion we fill
		int[][] matrix=new int[source.length()+1][target.length()+1];		//our stack space rather quickly. This is an attempt to 
																															//fix this. We fill the matrix with the "sub" Levenshtein 
																															//distances consecutively, starting from the first column 
																															//and the first row, then filling what remains of the second
																															//column and the second row and so on.
		
		//Set the first column and the first row 
		for(int row=0; row<matrix.length; row++) {
			matrix[row][0]=row;
		}
		for(int col=1; col<matrix[0].length; col++) {
			matrix[0][col]=col;
		}
		
		//Consecutively, set each element of each layer 
		Integer[] terms= new Integer[3];
		for(int layer=1; layer< Math.min(matrix.length, matrix[0].length); layer++) {
			
			//We first set the rows
			for(int row=layer; row<matrix.length; row++) {
				terms[0]=matrix[row-1][layer]+1;
				terms[1]=matrix[row][layer-1]+1;
				if( source.charAt(row-1) == target.charAt(layer-1) ) {
					terms[2]=matrix[row-1][layer-1];
				}
				else {
					terms[2]=matrix[row-1][layer-1]+1;
				}
				
				matrix[row][layer]=Collections.min( Arrays.asList(terms) );
			}
			
			//We then set the columns
			for(int col=layer+1; col<matrix[0].length; col++) {
				terms[0]=matrix[layer-1][col]+1;
				terms[1]=matrix[layer][col-1]+1;
				if( source.charAt(layer-1) == target.charAt(col-1) ) {
					terms[2]=matrix[layer-1][col-1];
				}
				else {
					terms[2]=matrix[layer-1][col-1]+1;
				}
				
				matrix[layer][col]=Collections.min( Arrays.asList(terms) );
			}	
		}
		
		return matrix[matrix.length-1][matrix[0].length-1];
	}
	
	public static int algorithm4(String source, String target) {						//If we are only interested in calculating Levenshtein distance we do not
		ArrayList<Integer> verticalPrev=new ArrayList<Integer>();				//need to store the whole matrix. Doing so would result in filling our heap
		ArrayList<Integer> horizontalPrev=new ArrayList<Integer>();			//space rather quickly as the sizes of our strings go larger. In this variant,
		ArrayList<Integer> verticalCur=new ArrayList<Integer>();					//we only store the previous calculated column, the previous calculated row
		ArrayList<Integer> horizontalCur=new ArrayList<Integer>();			//the current column(that is the column we are calculating in a given step) and 
																																//the current row. This variant is more that a little tricky to implement corre-
																																//ctly.
		
		for(int row=0; row<source.length()+1; row++) {
			verticalPrev.add(row);
		}
		for(int col=1; col<target.length()+1; col++) {
			horizontalPrev.add(col);
		}
		
		Integer[] terms= new Integer[3];
		for(int layer=1; layer< Math.min(source.length()+1, target.length()+1); layer++) {
			System.out.println( layer+"/"+Math.min(source.length()+1, target.length()+1) );		//Only on this version of the program, so we know at what stage our program is
			//Setting the verticalCur																											
			verticalCur= new ArrayList<Integer>();
			terms[0]=horizontalPrev.get(0)+1;				//We have to do something special to calculate the first element of a given
			terms[1]=verticalPrev.get(1)+1;					//verticalCur
			if( source.charAt(layer-1) == target.charAt(layer-1) ) {
				terms[2]=verticalPrev.get(0);
			}
			else {
				terms[2]=verticalPrev.get(0)+1;
			}
			verticalCur.add( Collections.min( Arrays.asList(terms) ) );
			for(int row=layer+1; row<source.length()+1; row++) {
				terms[0]=verticalCur.get( verticalCur.size() -1)+1;
				terms[1]=verticalPrev.get( verticalCur.size()+1 )+1;
				if( source.charAt(row-1) == target.charAt(layer-1) ) {
					terms[2]=verticalPrev.get( verticalCur.size() );
				}
				else {
					terms[2]=verticalPrev.get( verticalCur.size() )+1;
				}
				
				verticalCur.add( Collections.min( Arrays.asList(terms) ) );
			}
			
			//Setting the horizontalCur
			horizontalCur= new ArrayList<Integer>();
			if( layer< target.length() ) {
				terms[0]=horizontalPrev.get(1)+1;
				terms[1]=verticalCur.get(0)+1;
				if( source.charAt(layer-1) == target.charAt(layer) ) {
					terms[2]=horizontalPrev.get(0);
				}
				else {
					terms[2]=horizontalPrev.get(0)+1;
				}
				horizontalCur.add( Collections.min( Arrays.asList(terms) ) );
				for(int col=layer+2; col<target.length()+1; col++) {
					terms[0]=horizontalPrev.get( horizontalCur.size()+1 )+1;
					terms[1]=horizontalCur.get( horizontalCur.size() -1)+1;
					if( source.charAt(layer-1) == target.charAt(col-1) ) {
						terms[2]=horizontalPrev.get( horizontalCur.size() );
					}
					else {
						terms[2]=horizontalPrev.get( horizontalCur.size() )+1;
					}
					
					horizontalCur.add( Collections.min( Arrays.asList(terms) ) );
				}
			}
			else {
				//Do nothing! - Leave horizontalCur empty
			}
			
			verticalPrev=verticalCur;
			horizontalPrev=horizontalCur;
		}
		
		if( horizontalPrev.isEmpty() ) {
			return verticalPrev.get( verticalPrev.size() - 1);
		}
		else {
			return horizontalPrev.get( horizontalPrev.size() - 1);
		}
	}
	
	public static double similarity(String source, String target) {		//A simple function to calculate the how similar two strings are.
																														//Since, in the worst case, the minimum distance of two strings
																														//is simply the length of the bigger one (we can get to the bigger
																														//string performing lengthOfSmallerString replacements and then
																														//lengthOfBiggerString-lengthOfSmallerString), a reasonable measure
																														//of difference would simply be: levDis/lengthOfBiggerString. By sub-
																														//tracting this from 1 we get a measure of similarity.
		
		double difference= (double) algorithm4(source, target)/Math.max(source.length(), target.length() );		//We can choose the algorithm of our liking
		return  1-difference ;
	}
	
}
