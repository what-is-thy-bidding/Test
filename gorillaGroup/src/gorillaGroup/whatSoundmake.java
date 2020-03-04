package gorillaGroup;

public abstract class  whatSoundmake {
	
	public String sound(String animal){
		
		if(animal.compareTo("DOG")==0){
			return "BOW-BOW";
			
		}else if(animal.compareTo("CAT")==0){
			
			return "MEOOW";
		}else{
			return "DONT KNOW";
		}
		
	}
	
	
}
