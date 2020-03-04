

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class test {

	public static void combinationUtil(int arr[], int data[], int start, int end, int index, int r,int item, int frequency) throws IOException { 
		if (index == r) 
		{ 
			File file = new File("result.txt");
			FileWriter fr = new FileWriter(file, true);
			BufferedWriter br = new BufferedWriter(fr);
			

			br.write('{');
			System.out.print( "{");
			for (int j=0; j<r; j++) {
				System.out.print(data[j]+" "); 
				//br.write((char)(data[j]+97)+ " ");
				br.write(data[j]+ " ");
			}
			//br.write((char)(item+97) +" } - "+ frequency);
			br.write(/*item +*/" }  "/*+ frequency*/);
			
			System.out.println(item + "} "+frequency); 
			br.newLine();
			
			br.close();
			fr.close();
			return; 
		} 
		// replace index with all possible elements. The condition 
		// "end-i+1 >= r-index" makes sure that including one element 
		// at index will make a combination with remaining elements 
		// at remaining positions 
		for (int i=start; i<=end && end-i+1 >= r-index; i++) 
		{ 
			data[index] = arr[i]; 
			combinationUtil(arr, data, i+1, end, index+1, r,item,  frequency); 
		} 
	} 
	
	public static void printCombination(int arr[], int n, int r, int item, int frequency) throws IOException { 
        // A temporary array to store all combination one by one 
        int data[]=new int[r]; 
  
        // Print all combination using temprary array 'data[]' 
        System.out.println();
        combinationUtil(arr, data, 0, n-1, 0, r, item,  frequency); 
    } 
	
	public static void main(String args[]) throws IOException {
		File file= new File("result.txt");
		file.delete();
		int[] array= {1,2,3,4,5,6};
		
		//printCombination(array,array.length,3, 0,0);

		for(int i=1;i<array.length;i++) {
			printCombination(array,array.length,i, 0,0);

		}
	}
}
