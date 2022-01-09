import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Wordle {

    static ArrayList<String> dictionary;

    static ArrayList<Character>[] graySets;

    static ArrayList<Character>[] yellowSets;

    static char[] solution;

    public static void main(String[] args) throws FileNotFoundException {
        initialize();
        Scanner reader = new Scanner(System.in);
        int guessCount = 0;
        System.out.println("Let's play Wordle!");
        String currentResponse = "";
        String currentGuess = "roate";
        while (true) {
            guessCount += 1;
            System.out.println("Current Dictionary Size: " + dictionary.size());
            System.out.println("I guess: " + currentGuess);
            System.out.println("Please input the response (g for green, y for yellow, x for blank):");
            currentResponse = reader.nextLine();
            if ((currentResponse.equals("ggggg")))
                break;
            processResponse(currentResponse, currentGuess);
            currentGuess = dictionary.get(0);
        }
        System.out.println(
                "I won! The solution was: \"" + currentGuess + "\". It took me " + guessCount + " guess(es). Good game!");
        reader.close();
    }

    static void initialize() throws FileNotFoundException {
        dictionary = new ArrayList<>();
        File inFile = new File("/home/nate/personal/wordle/words.txt");
        Scanner fileReader = new Scanner(inFile);
        while (fileReader.hasNextLine())
            dictionary.add(fileReader.nextLine());
        fileReader.close();
        graySets = new ArrayList[5];
        for (int i = 0; i < 5; i++)
            graySets[i] = new ArrayList<Character>();
        yellowSets = graySets.clone(); // they're empty right now, so this is harmless
        solution = new char[5];
    }

    static void processResponse(String response, String guess) {
        for (int i = 0; i < 5; i++) {
            if (solution[i] != '\u0000') // possible funny line
                continue;
            char responseChar = response.charAt(i);
            char guessChar = guess.charAt(i);
            ArrayList<String> removeList = new ArrayList<>();
            switch (responseChar) {
                case 'g':
                    for (String s : dictionary)
                        if (!(s.charAt(i) == guessChar))
                            removeList.add(s);
                    solution[i] = guessChar;
                    break;
                case 'y':
                    for (String s : dictionary)
                        if ((s.charAt(i) == guessChar))
                            removeList.add(s);
                    yellowSets[i].add(guessChar);
                    break;
                case 'x':
                    for (String s : dictionary)
                        if ((s.charAt(i) == guessChar))
                            removeList.add(s);
                    graySets[i].add(guessChar);
                    break;
            }
            for (String s : removeList)
                dictionary.remove(s);

        }
    }
}
