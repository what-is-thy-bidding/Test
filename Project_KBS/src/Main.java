import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
/*
 * Read CSV file 
 * */
public class Main {
	
	
	public static HashMap<String, ArrayList<String>> Columns=null;
	
	public static String remove_spaces(String query) {
		
		StringBuilder sb= new StringBuilder();
		
	    for(int i = 0;i < query.length();i++) {
	        if(query.charAt(i) != ' ') 
	        	sb.append(query.charAt(i));
	    }
	    return 	sb.toString();

	}
	
	public static void printTable(HashMap<String,ArrayList<String>> Map){
		/*for(int i=0;i<table.size();i++){
        	for(int j=0;j<table.get(i).size();j++){	
        		System.out.print( table.get(i).get(j)+ " ");
        	}
        	System.out.println();
        }*/
		
		for(String key:Map.keySet()){
			System.out.print(key + " ");
			ArrayList<String> temp= new ArrayList<String>();
			temp=Map.get(key);
			
			for(String S:temp){
				System.out.print(S+" ");
			}
			System.out.println();
		}
		
		
		
	}
	
	public static void getDatabase(){
		String fileName= "test.csv";
	    File file= new File(fileName);

	    // this gives you a 2-dimensional array of strings
	    Scanner inputStream;

	    try{
	        inputStream = new Scanner(file);

        	int index=0;
        	ArrayList<String> col= new ArrayList<String>();
        	
	        while(inputStream.hasNext()){
	            
	        	String line= inputStream.next();
	            String[] values = line.split(",");
	            
	            if(index==0){
	    			//System.out.println("Code has reached here");

	            	index=values.length;
	            	
	            	ArrayList<String> check= new ArrayList<String>();
	            	
	            	Columns=new HashMap<String, ArrayList<String>>();// Attributes
	            	
	            	
	            	for(String temp:values){
	            		Columns.put(temp, check);
	            		col.add(temp);
	            	}
	            	
	            	
	            }else{
	            	

	            	for(int i=0;i<values.length;i++){
	            		
	            		String key= col.get(i); //-------------------------------------------------------------
	            		System.out.println(" Key "+key + " "+ values[i]);
	            		
	            		ArrayList<String> temp= Columns.get(key);
	            		temp.add(values[i]);
	            		
	            		Columns.put(key, temp);
	            	}
	            	
	            }
	            
	        }

	        inputStream.close();
	        
	        printTable(Columns);
	        
	        
	        
	    }catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }

	    // the following code lets you iterate through the 2-dimensional array
	    /*int lineNo = 1;
	    for(List<String> line: lines) {
	    int columnNo = 1;
	    	for (String value: line) {
	        	System.out.println("Line " + lineNo + " Column " + columnNo + ": " + value);
	        	columnNo++;
	    	}
	    	lineNo++;
	    }*/
	}
	
	private static void PrefixEvaluation(String substring) {
		
		System.out.println(substring );
		
		/*String[]cols=substring.split(",");
		int colsNumbers[]=new int[cols.length];
		
		for(int i=0;i<colsNumbers.length;i++){
			
			System.out.println(cols[i] + " "+ Columns.containsKey(cols[i]) + " "+ Columns.get(cols[i]));
			
			colsNumbers[i]=Columns.get(cols[i]);
		}
		
		ArrayList<ArrayList<String>> table= new ArrayList<ArrayList<String>>(colsNumbers.length);*/
		

	}
	
	public static void getRequest(String Columns){
		
	}
	
	public static void processQuery(String Query){
		
		
		
		char[] query = new char[Query.length()];
		query=Query.toCharArray();
		int index= 0;
		Stack<Character> stack= new Stack<Character>();
		System.out.println();
		while(index<query.length){
			stack.push(query[index]);
			
			if(query[index]==')'){
				String exp="", temp="";
				
				
				while(temp.compareTo("(")!=0){
					temp=stack.pop().toString();
					exp=temp+(String)exp;
				}
				
				exp=exp.substring(1, exp.length()-1);//---->remove the brackets and 
				
				
				
				
				PrefixEvaluation(exp);
				
				
				
				
				
				//exp=String.valueOf(infix(exp));
							
				stack.push('%');//-----> push the evaluated value in the stack
				
			}
			index++;
			
		}
		//check if number of opening brackets equals number of closing brackets
				int numOpenBracks=0;
				int numClosedBracks=0;			
				for(int i=0;i<query.length;i++)
				{
					if(query[i] == ')')
					{		
						numOpenBracks++;
					}
					if(query[i] =='(')
					{
						numClosedBracks++;
					}
				}
				if(numOpenBracks != numClosedBracks)
				{
					System.out.println("Syntax Error: Brackets");
				}						
				else
				{
					System.out.println(stack.pop() + " Last Else Condtion");
				}	
		
		
		
		
		
		
	}
	
	

	

	public static void main(String args[]) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("Queries.txt"));
		
			getDatabase();
			
			String query = null;
		
			query = br.readLine();
					 
			br.close();
			query="("+query+")";
			System.out.println(query);
			processQuery(query);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}






	
	
}
