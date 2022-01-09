import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class Wordle {

    static ArrayList<String> dictionary;

    static HashSet<Character>[] graySets;

    static HashSet<Character>[] yellowSets;

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
            // printYellowSets();
            filterYellows();
            // currentGuess = findMostGreens();
            currentGuess = dictionary.get(0);
        }
        System.out.println(
                "I won! The solution was: \"" + currentGuess + "\". It took me " + guessCount
                        + " guess(es). Good game!");
        reader.close();
    }

    static void initialize() throws FileNotFoundException {
        dictionary = new ArrayList<>();
        File inFile = new File("/home/nate/personal/wordle/words.txt");
        Scanner fileReader = new Scanner(inFile);
        while (fileReader.hasNextLine())
            dictionary.add(fileReader.nextLine());
        fileReader.close();
        graySets = new HashSet[5];
        for (int i = 0; i < 5; i++)
            graySets[i] = new HashSet<Character>();
        yellowSets = new HashSet[5];
        for (int i = 0; i < 5; i++)
            yellowSets[i] = new HashSet<Character>();
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
                    for (int j = 0; j < 5; j++) {
                        if (j == i)
                            continue;
                        // System.out.printf("Adding %c to yellowSets[%d]\n", guessChar, j);
                        yellowSets[j].add(guessChar);
                    }
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

    static void filterYellows() {
        ArrayList<String> removeList = new ArrayList<>();
        for (HashSet<Character> set : yellowSets)
            for (Character c : set)
                for (String s : dictionary)
                    if (s.indexOf(c) == -1) {
                        removeList.add(s);
                        // System.out.printf("Going to remove %s because it didn't have %c\n", s, c);
                    }
        for (String s : removeList)
            dictionary.remove(s);
        // System.out.println("exited filterYellows");
    }

    static void printYellowSets() {
        for (int i = 0; i < 5; i++) {
            HashSet<Character> currSet = yellowSets[i];
            System.out.printf("YELLOW SET %d :\n", i);
            for (Character c : currSet)
                System.out.println(c);
        }
    }

    // static int countGreens(String word) {
    // int retInt = 0;
    // for (int i = 0; i < 5; i++) {
    // if (solution[i] != '\u0000')
    // if (word.charAt(i) == solution[i])
    // retInt += 1;
    // }
    // return retInt;
    // }

    // static String findMostGreens() {
    // int max = 0;
    // String word = "";
    // for (String s : dictionary) {
    // int greens = countGreens(s);
    // if (greens > max) {
    // max = greens;
    // word = s;
    // }
    // }
    // if (word.equals("")) {
    // System.out.println("No words in the dictionary had any greens!");
    // return dictionary.get(0);
    // }
    // System.out.printf("%s had the most greens (%d)\n", word, max);
    // return word;
    // }
}
