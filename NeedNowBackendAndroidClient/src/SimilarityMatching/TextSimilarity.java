package SimilarityMatching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class TextSimilarity {
	/** @return an array of adjacent letter pairs contained in the input string */
	private static String[] letterPairs(String str) {
		int numPairs = str.length() - 1;
		String[] pairs = new String[numPairs];
		for (int i = 0; i < numPairs; i++) {
			pairs[i] = str.substring(i, i + 2);
		}
		return pairs;
	}

	/** @return an ArrayList of 2-character Strings. */
	private static ArrayList wordLetterPairs(String str) {
		ArrayList allPairs = new ArrayList();
		// Tokenize the string and put the tokens/words into an array
		String[] words = str.split("\\s");
		// For each word
		for (int w = 0; w < words.length; w++) {
			// Find the pairs of characters
			String[] pairsInWord = letterPairs(words[w]);
			for (int p = 0; p < pairsInWord.length; p++) {
				allPairs.add(pairsInWord[p]);
			}
		}
		return allPairs;
	}

	/** @return lexical similarity value in the range [0,1] */
	public static double compareStrings(String str1, String str2) {
		ArrayList pairs1 = wordLetterPairs(str1.toUpperCase());
		ArrayList pairs2 = wordLetterPairs(str2.toUpperCase());
		int intersection = 0;
		int union = pairs1.size() + pairs2.size();
		for (int i = 0; i < pairs1.size(); i++) {
			Object pair1 = pairs1.get(i);
			for (int j = 0; j < pairs2.size(); j++) {
				Object pair2 = pairs2.get(j);
				if (pair1.equals(pair2)) {
					intersection++;
					pairs2.remove(j);
					break;
				}
			}
		}
		return (2.0 * intersection) / union;
	}
	
	public static LinkedHashMap<String, Double> sortHashMapByValuesD(
			HashMap<String, Double> passedMap) {
		List<Integer> mapKeys = new ArrayList(passedMap.keySet());
		List<Integer> mapValues = new ArrayList(passedMap.values());
		Collections.sort(mapValues, Collections.reverseOrder());
		Collections.sort(mapKeys, Collections.reverseOrder());

		LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();

		Iterator valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Object val = valueIt.next();
			Iterator keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				double comp1 = Double
						.parseDouble(passedMap.get(key).toString());
				double comp2 = Double.parseDouble(val.toString());

				if (comp1 == comp2) {
					passedMap.remove(key);
					mapKeys.remove(key);
					sortedMap.put((String) key, (Double) val);
					break;
				}

			}

		}
		return sortedMap;
	}

	public static void main(String args[]) {
		String str1 = "Phy Book";
		String str2 = "Physics Book";
		String str3 = "Eng Book";
		String str4 = "math book";
		
		String str5 = "Engg Drafter";
		String str6 = "Drafting Instrument";
		
		String str7 = "Engg graphics";
		String str8 = "Medical Instrument";
		String str9 = "Mathematics book";
		
		System.out.println(compareStrings(str1, str2));
		System.out.println(compareStrings(str1, str3));
		System.out.println(compareStrings(str1, str4));

		System.out.println(compareStrings(str5, str6));
		System.out.println(compareStrings(str5, str7));
		
		System.out.println(compareStrings(str6, str8));
		System.out.println(compareStrings(str6, str5));
		
		System.out.println(compareStrings(str4, str9));
		
		
	}
}
