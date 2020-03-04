package gorillaGroup;

public class Dog extends whatSoundmake implements Animal  {
	
	protected String dog="DOG";
	
	@Override
	public String whatAnimalAreYou() {
		return " I am a Dog.";
	}
	
	@Override
	public String getSound(){
		return sound(dog);
	}
	
	

}
