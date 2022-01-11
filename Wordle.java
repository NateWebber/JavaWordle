import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

public class Wordle {

    static ArrayList<String> dictionary;

    static ArrayList<String> possibleResponses;

    static HashSet<Character>[] wrongSets;

    static HashSet<Character>[] maybeSets;

    static HashMap<Character, Integer> letterScores;

    static char[] solution;

    public static void main(String[] args) throws IOException {
        if (Integer.valueOf(args[0]) == 0)
            manualGame();
        else if (Integer.valueOf(args[0]) > 0)
            executeStats(Integer.valueOf(args[0]));
    }

    static void manualGame() throws FileNotFoundException {
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
            doFiltering();
            if (guessCount < 3) {
                currentGuess = getMostLikelyWord(dictionary);
            } else {
                currentGuess = minimaxNextGuess();
            }
        }
        System.out.println(
                "I won! The solution was: \"" + currentGuess + "\". It took me " + guessCount
                        + " guess(es). Good game!");
        reader.close();
    }

    static void executeStats(int count) throws IOException {
        File outFile = new File("/home/nate/personal/wordle/output/WordleOut.txt");
        if (outFile.createNewFile())
            System.out.println("Created output file WordleOut.txt");
        else
            System.out.println("Output file WordleOut.txt already existed!");
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
        int totalGuesses = 0;
        long totalTime = 0;
        long longestTime = 0;
        String longestTimeWord = "";
        long shortestTime = Long.MAX_VALUE;
        String shortestTimeWord = "";
        int leastGuesses = Integer.MAX_VALUE;
        String leastGuessesWord = "";
        int mostGuesses = 0;
        String mostGuessesWord = "";
        for (int i = 1; i <= count; i++) {
            initialize();
            long startTime = System.currentTimeMillis();
            int guessCount = 0;
            String currentResponse = "";
            String currentGuess = "roate";
            Random rng = new Random();
            String secretCode = dictionary.get(rng.nextInt(dictionary.size())); // randomly pick a secret code to try
                                                                                // and solve for
            System.out.printf("Starting run %d. Secret code: %s\n", i, secretCode);
            while (true) {
                guessCount += 1;
                currentResponse = generateResponse(currentGuess, secretCode);
                if ((currentResponse.equals("ggggg")))
                    break;
                processResponse(currentResponse, currentGuess);
                doFiltering();
                if (guessCount < 3) {
                    currentGuess = getMostLikelyWord(dictionary);
                } else {
                    currentGuess = minimaxNextGuess();
                }
            }
            long elapsedTime = (System.currentTimeMillis() - startTime);
            System.out.printf("Finished run %d. Secret code: %s Guess count: %d Run time: %d\n", i, secretCode,
                    guessCount, elapsedTime);
            totalGuesses += guessCount;
            totalTime += elapsedTime;

            if (elapsedTime > longestTime) {
                longestTime = elapsedTime;
                longestTimeWord = secretCode;
            }
            if (elapsedTime < shortestTime) {
                shortestTime = elapsedTime;
                shortestTimeWord = secretCode;
            }
            if (guessCount > mostGuesses) {
                mostGuesses = guessCount;
                mostGuessesWord = secretCode;
            }
            if (guessCount < leastGuesses) {
                leastGuesses = guessCount;
                leastGuessesWord = secretCode;
            }

            writer.write(i + " " + secretCode + " " + guessCount + " " + elapsedTime);
            writer.newLine();
        }
        writer.write("FINAL STATS:");
        writer.newLine();
        writer.write("Total games played: " + count);
        writer.newLine();
        writer.write("Total guesses: " + totalGuesses);
        writer.newLine();
        writer.write("Total time elapsed: " + totalTime + "ms");
        writer.newLine();
        writer.write("Average guesses per game: " + (totalGuesses / count));
        writer.newLine();
        writer.write("Average time per game: " + (totalTime / count) + "ms");
        writer.newLine();
        writer.write("Fastest Game: " + shortestTimeWord + " - " + shortestTime + "ms");
        writer.newLine();
        writer.write("Slowest Game: " + longestTimeWord + " - " + longestTime + "ms");
        writer.newLine();
        writer.write("Least Guesses: " + leastGuessesWord + " - " + leastGuesses + " guesses");
        writer.newLine();
        writer.write("Most Guesses: " + mostGuessesWord + " - " + mostGuesses + " guesses");
        writer.close();
    }

    static void initialize() throws FileNotFoundException {
        dictionary = new ArrayList<>();
        File wordFile = new File("/home/nate/personal/wordle/words.txt");
        Scanner fileReader = new Scanner(wordFile);
        while (fileReader.hasNextLine())
            dictionary.add(fileReader.nextLine());
        fileReader.close();
        wrongSets = new HashSet[5];
        for (int i = 0; i < 5; i++)
            wrongSets[i] = new HashSet<Character>();
        maybeSets = new HashSet[5];
        for (int i = 0; i < 5; i++)
            maybeSets[i] = new HashSet<Character>();
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
        // first, remove the characters we're going to learn more about
        for (int i = 0; i < 5; i++) {
            HashSet<Character> maybeSet = maybeSets[i];
            for (int j = 0; j < 5; j++) {
                if (maybeSet.contains(guess.charAt(j)))
                    maybeSet.remove(guess.charAt(j));
            }
        }
        // next, add to the maybe/wrong sets as warranted
        for (int i = 0; i < 5; i++) {
            char responseChar = response.charAt(i);
            char guessChar = guess.charAt(i);
            if (solution[i] != '\u0000') // we've already confirmed this character
                continue;
            if (responseChar == 'g') { // found a green, write it down
                solution[i] = guessChar;
                continue;
            }
            HashSet<Character> wrongSet = wrongSets[i];

            if (responseChar == 'y') { // we got a yellow
                wrongSet.add(guessChar); // it can't be here
                for (int j = 0; j < 5; j++) { // for every other maybeSet
                    if (i == j)
                        continue;
                    if (!wrongSets[j].contains(guessChar)) // if it's not already in the wrong set
                        maybeSets[j].add(guessChar); // add it to the maybe set
                }
            } else { // we got a gray
                wrongSet.add(guessChar);
            }
        }
    }

    static void doFiltering() {
        ArrayList<String> removeList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (String s : dictionary) {
                if (solution[i] != '\u0000' && s.charAt(i) != solution[i]) { // if we know the letter here and this word
                                                                             // doesn't have it
                    removeList.add(s);
                    // System.out.printf("Removing %s because character %d didn't match known
                    // character %c\n", s, i + 1,
                    // solution[i]);
                } else if (wrongSets[i].contains(s.charAt(i))) { // if this has a known wrong letter
                    removeList.add(s);
                    // System.out.printf("Removing %s because character %d was in the wrong set\n",
                    // s, i + 1);
                }
            }
        }
        for (HashSet<Character> set : maybeSets)
            for (Character c : set)
                for (String s : dictionary)
                    if (s.indexOf(c) == -1) {
                        removeList.add(s);
                        // System.out.printf("Removing %s because it didn't have %c\n", s, c);
                    }
        for (String s : removeList)
            dictionary.remove(s);
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
        ArrayList<String>[] scoreArr = new ArrayList[16000];
        for (int i = 0; i < 16000; i++)
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
        // System.out.printf("generated all scores, time elapsed: %d ms\n",
        // (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        for (int i = 15999; i >= 0; i--) {
            if (scoreArr[i].size() > 0) {
                // return scoreArr[i].get(0);
                // System.out.printf("Best score was %d\n", i);
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
