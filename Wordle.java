import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Wordle {

    static ArrayList<String> dictionary;

    static ArrayList<String> possibleResponses;

    static HashSet<Character>[] graySets;

    static HashSet<Character>[] yellowSets;

    static HashMap<Character, Integer> letterScores;

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
            // currentGuess = dictionary.get(0);
            currentGuess = minimaxNextGuess();
        }
        System.out.println(
                "I won! The solution was: \"" + currentGuess + "\". It took me " + guessCount
                        + " guess(es). Good game!");
        reader.close();

    }

    static void initialize() throws FileNotFoundException {
        dictionary = new ArrayList<>();
        File wordFile = new File("/home/nate/personal/wordle/words.txt");
        Scanner fileReader = new Scanner(wordFile);
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

        possibleResponses = new ArrayList<String>();
        possibleResponses.add("");

        for (int i = 0; i < 5; i++)
            responseGenerator();

        letterScores = new HashMap<>();
        File scoreFile = new File("/home/nate/personal/wordle/letterscores.txt");
        fileReader = new Scanner(scoreFile);
        while (fileReader.hasNextLine()) {
            String line = fileReader.nextLine();
            letterScores.put(line.charAt(0), Integer.valueOf(line.substring(2)));
        }
        fileReader.close();
        // System.out.println("INITIALIZE: possibleResponses size: " +
        // possibleResponses.size());
    }

    static void responseGenerator() {
        ArrayList<String> removeList = new ArrayList<>();
        ArrayList<String> addList = new ArrayList<>();
        for (String s : possibleResponses) {
            removeList.add(s);
            String new1 = s + "g";
            String new2 = s + "y";
            String new3 = s + "x";
            addList.add(new1);
            addList.add(new2);
            addList.add(new3);
        }
        for (String s : removeList)
            possibleResponses.remove(s);
        for (String s : addList)
            possibleResponses.add(s);
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

    static String generateResponse(String guess, String code) {
        HashMap<Character, Integer> letterMap = new HashMap<>();
        ArrayList<Integer> correctIndices = new ArrayList<>();
        char[] retArr = new char[5];
        for (int i = 0; i < 5; i++) {
            char guessChar = guess.charAt(i);
            char codeChar = code.charAt(i);
            // System.out.printf("guessChar: %c codeChar: %c\n", guessChar, codeChar);
            if (guessChar == codeChar) {
                retArr[i] = 'g';
                correctIndices.add(i);
                continue;
            } else {
                if (letterMap.containsKey(codeChar)) {
                    letterMap.put(codeChar, letterMap.get(codeChar) + 1);
                } else {
                    letterMap.put(codeChar, 1);
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            if (correctIndices.contains(i))
                continue;
            char guessChar = guess.charAt(i);
            if (!(letterMap.containsKey(guessChar))) {
                retArr[i] = 'x';
            } else {
                if (letterMap.get(guessChar) == 0)
                    retArr[i] = 'x';
                else {
                    retArr[i] = 'y';
                    letterMap.put(guessChar, letterMap.get(guessChar) - 1);
                }
            }
        }
        return String.valueOf(retArr);
    }

    static int getMinimaxScore(String possibleGuess) {
        int maxHits = 0;
        for (String response : possibleResponses) {
            // System.out.printf("minimaxScore: starting with response: %s\n", response);
            int hits = 0;
            for (String s : dictionary) {
                String thisResponse = generateResponse(s, possibleGuess);
                if (thisResponse.equals(response)) {
                    // System.out.printf("Hit! Response for %s, %s was %s\n", s, possibleGuess,
                    // thisResponse);
                    hits += 1;
                }
            }
            if (hits > maxHits)
                maxHits = hits;
        }
        // System.out.printf("maxHits: %d\n", maxHits);
        int score = dictionary.size() - maxHits;
        return score;
    }

    /*
     * The speed at which this minimax function executes is heavily reliant on how
     * much the initial filtering pass removes
     * Fortunately with a good starting guess there isn't too much to worry about
     * Always room for improvement though
     * After two rounds of filtering I've never seen this function take more than a
     * millisecond
     */
    static String minimaxNextGuess() {
        long startTime = System.currentTimeMillis();
        ArrayList<String>[] scoreArr = new ArrayList[6000];
        for (int i = 0; i < 6000; i++)
            scoreArr[i] = new ArrayList<>();
        // System.out.printf("initialized scoreArr[], time elapsed: %d ms\n",
        // (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        for (String possibleGuess : dictionary) {
            int score = getMinimaxScore(possibleGuess);
            // System.out.printf("Found score of %d for possibleGuess %s\n", score,
            // possibleGuess);
            scoreArr[score].add(possibleGuess);
        }
        System.out.printf("generated all scores, time elapsed: %d ms\n",
                (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        for (int i = 5999; i >= 0; i--) {
            if (scoreArr[i].size() > 0) {
                // return scoreArr[i].get(0);
                return getMostLikelyWord(scoreArr[i]);
            }
        }
        System.out.println("CRITICAL ERROR IN MINIMAX: CRASH IMMINENT");
        return null;
    }

    static String getMostLikelyWord(ArrayList<String> list) {
        String retString = "";
        int minScore = 9999;
        for (String s : list) {
            int currScore = 0;
            for (int i = 0; i < 5; i++) {
                char currChar = s.charAt(i);
                currScore += letterScores.get(currChar);
            }
            if (currScore < minScore) {
                minScore = currScore;
                retString = s;
            }
        }
        return retString;
    }
    
}
