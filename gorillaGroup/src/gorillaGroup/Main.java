package gorillaGroup;

/*
 * Abstract
 * Interface
 * 
 * Animal-> Cats, Dogs
 * Cats -> Do they Meow
 * Dogs -> Do they Bark
 * 
 * */


public class Main {
	
	public static void main(String[] args){
		
		Animal animal= new Dog();
		System.out.println(animal.whatAnimalAreYou());
		System.out.println(animal.getSound());

		
		animal= new Cat();
		System.out.println(animal.whatAnimalAreYou());
		System.out.println(animal.getSound());
		
		Dog dog= new Dog();
		
		System.out.println(dog.dog);
	}
}
