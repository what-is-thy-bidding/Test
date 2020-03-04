package gorillaGroup;

public class Cat extends whatSoundmake implements Animal {
	
	protected String cat="CAT";

	
	@Override
	public String whatAnimalAreYou() {
		return " I am a Cat.";
	}
	
	@Override
	public String getSound(){
		return sound(cat);
	}
}
