import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class MiniMaxWordle {
    static ArrayList<String> possibleResponses;

    static HashSet<String> possibleCodes;

    static HashSet<String> workingSet;

    public static void main(String[] args) throws FileNotFoundException {
        long startTime = System.currentTimeMillis();
        initialize();
        System.out.printf("initialize() finished, time elapsed: %d ms\n", (System.currentTimeMillis() - startTime));
        Scanner reader = new Scanner(System.in);

        String currentResponse = "";

        String currentGuess = "roate"; // this may need tuning

        System.out.println("Let's pay Wordle!");

        while (true) {
            possibleCodes.remove(currentGuess);
            System.out.println("I guess: " + currentGuess);
            System.out.println(
                    "Please input the response for this guess (Use 'C' for Green, 'W' for Yellow, and 'X' for blank");

            currentResponse = reader.nextLine();
            if (currentResponse.equals("CCCCC"))
                break;
            ArrayList<String> removeList = new ArrayList<String>();
            startTime = System.currentTimeMillis();
            for (String s : workingSet) {
                String thisResponse = generateResponse(s, currentGuess);
                if (!thisResponse.equals(currentResponse)) {
                    removeList.add(s);
                }
            }
            for (String s : removeList)
                workingSet.remove(s);
            System.out.printf("removed non-matching possibilities, time elapsed: %d ms\n",
                    (System.currentTimeMillis() - startTime));
            currentGuess = minimaxNextGuess();

        }
        System.out.println("I won! Good game!");

        reader.close();

    }

    static String minimaxNextGuess() {
        long startTime = System.currentTimeMillis();
        ArrayList<String>[] scoreArr = new ArrayList[6000];
        for (int i = 0; i < 6000; i++)
            scoreArr[i] = new ArrayList<>();
        System.out.printf("initialized scoreArr[], time elapsed: %d ms\n",
                (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        for (String possibleGuess : possibleCodes) {
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
                for (String s : scoreArr[i]) {
                    if (workingSet.contains(s)) {
                        System.out.printf("found response in working set, time elapsed: %d ms\n",
                                (System.currentTimeMillis() - startTime));
                        return s;
                    }
                }
                System.out.printf("no response in working set, time elapsed: %d ms\n",
                        (System.currentTimeMillis() - startTime));
                return scoreArr[i].get(0);
            }
        }
        System.out.println("CRITICAL ERROR IN MINIMAX: CRASH IMMINENT");
        return null;
    }

    static int getMinimaxScore(String possibleGuess) {
        int maxHits = 0;
        // this loop will run 21 times (21 possible responses)
        for (String response : possibleResponses) {
            // System.out.printf("minimaxScore: starting with response: %s\n", response);
            int hits = 0;
            for (String s : workingSet) {
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
        int score = workingSet.size() - maxHits;
        return score;
    }

    /*
     * given a secret code, and a guess at that code, generate the response
     */
    static String generateResponse(String guess, String code) {
        String retString = "";
        ArrayList<Integer> correctIndices = new ArrayList<>();
        HashMap<Character, Integer> letterMap = new HashMap<>();
        /*
         * first pass: find exactly correct letters and setup "budgets"
         */
        for (int i = 0; i < 5; i++) {
            char guessChar = guess.charAt(i);
            char codeChar = code.charAt(i);
            if (guessChar == codeChar) {
                correctIndices.add(i);
                retString += "C";
                continue;
            } else {
                if (letterMap.containsKey(codeChar)) {
                    letterMap.put(codeChar, letterMap.get(codeChar) + 1);
                } else {
                    letterMap.put(codeChar, 1);
                }

            }
        }
        /*
         * second pass: for non exactly correct letters, determine if they earn a yellow
         * mark
         * or no mark
         */
        for (int i = 0; i < 5; i++) {
            if (correctIndices.contains(i))
                continue;
            char guessChar = guess.charAt(i);
            if (!(letterMap.containsKey(guessChar))) {
                retString += "X";
            } else {
                if (letterMap.get(guessChar) == 0)
                    retString += "X";
                else {
                    retString += "W";
                    letterMap.put(guessChar, letterMap.get(guessChar) - 1);
                }
            }
        }

        /*
         * sort the return string
         */
        char[] arr = retString.toCharArray();
        Arrays.sort(arr);
        retString = new String(arr);
        return retString;
    }

    /*
     * setup initial values
     */
    static void initialize() throws FileNotFoundException {
        possibleResponses = new ArrayList<String>();

        for (int i = 0; i <= 5; i++) {
            int cCount = i;
            for (int j = 0; j <= (5 - cCount); j++) {
                int wCount = j;
                int xCount = (5 - cCount - wCount);
                // System.out.printf("%d : %d : %d\n", cCount, wCount, xCount);
                String addString = "";
                for (int c = 1; c <= cCount; c++)
                    addString += "C";
                for (int w = 1; w <= wCount; w++)
                    addString += "W";
                for (int x = 1; x <= xCount; x++)
                    addString += "X";
                // System.out.printf("%s\n", addString);
                possibleResponses.add(addString);
            }
        }

        possibleCodes = new HashSet<>();

        File inFile = new File("/home/nate/personal/wordle/words.txt");
        Scanner fileReader = new Scanner(inFile);
        while (fileReader.hasNextLine())
            possibleCodes.add(fileReader.nextLine());
        workingSet = (HashSet<String>) possibleCodes.clone();
        fileReader.close();
    }
}