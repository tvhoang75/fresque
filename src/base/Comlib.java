/**
 * 
 */
package base;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/**
 * @author tvhoang46
 *
 */
public class Comlib {
	
	public static void main(String[] args) throws Exception {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 1000000; i++) {
			list.add(i);
		}
		long startTime = System.currentTimeMillis();
		Collections.shuffle(list);
		System.out.println(System.currentTimeMillis()-startTime);
//		List<Integer> list = new ArrayList<>();
//		RandomAccessFile input = 
//				new RandomAccessFile("/Users/hoang/Documents/data/sensory/distribution.csv", "r");
//		String line;
//		String parts[];
//		
//		while((line = input.readLine()) != null) {
//			parts = line.split(",");
//			list.add(Integer.parseInt(parts[1]));
//		}
//		
//		System.out.println("right size = " + findRightSize(list, 2));
		
	}
	
	/**
	 * if the file exists, append text to it, otherwise create a new file first and append text then
	 * @param fileName
	 * @param text
	 * @throws Exception
	 */
	public static void appendFile(String fileName, String text) throws Exception {
		File f = new File(fileName);
		long fileLength = f.length();
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		raf.seek(fileLength);
		raf.writeBytes(text);
		raf.close();
	}
	
	/**
	 * Find a suitable size for buckets. 
	 * The size must be larger than or equal sizeMin 
	 * and sum of the distance from it to all sizes in listOfSizes is the least
	 * @param listOfSizes
	 * @param sizeMin
	 * @return bucket size
	 */
	public static int findRightSize(List<Integer> listOfSizes, int sizeMin) {
		int k = sizeMin;
		int maxSize = sizeMin;
		// find maxSize
		for (Integer size : listOfSizes) {
			if (size > maxSize) {
				maxSize = size;
			}
		}
		
		// calculate total number according to k which is between [defaultMin, maxSize]
		Map<Integer, Integer> mapCandidates = new HashMap<Integer, Integer>();
		int minTotalDummy = Integer.MAX_VALUE;
		for (int potentialSize = sizeMin; potentialSize <= maxSize; potentialSize++) {
			int totalDummy = 0;
			// calculate number of dummy tuples
			for (Integer size : listOfSizes) {
				int numBucket = 0;
				numBucket = (size / potentialSize);
				if (size % potentialSize != 0) {
					numBucket++;
				}
				totalDummy += ((numBucket * potentialSize) - size);
			}
			mapCandidates.put(potentialSize, totalDummy);
			// keep minTotalDummy
			if (minTotalDummy > totalDummy) {
				minTotalDummy = totalDummy;
			}
			totalDummy = 0;
		}
		System.out.println("===> Total dummy tuple : " + minTotalDummy);
		// if there are more than one candidates, choose the one that is the largest among them
		k = Integer.MIN_VALUE;
		Iterator<Entry<Integer, Integer>> it = mapCandidates.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, Integer> pair = (Entry<Integer, Integer>) it.next();
	        if (pair.getValue() == minTotalDummy) {
	        		if (pair.getKey() > k) {
	        			k = pair.getKey();
	        		}
	        }
	    }
		return k;
	}
	
	/** Remove non-printable characters in a string. 
	 * This case can happen as byte[] is transformed to String
	 * @param text
	 * @return cleared text
	 */
	public static String cleanTextContent(String text) 
	{
	    // strips off all non-ASCII characters
	    text = text.replaceAll("[^\\x00-\\x7F]", "");
	 
	    // erases all the ASCII control characters
	    text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
	     
	    // removes non-printable characters from Unicode
	    text = text.replaceAll("\\p{C}", "");
	 
	    return text.trim();
	}
	
	/**
	 * @param binaryString
	 * @return a list of positions of non-zero bits
	 */
	public static List<Integer> getNonZeroPositions(String binaryString){
		List<Integer> listPositions = new ArrayList<Integer>();
		int end = binaryString.length() - 1;
		int pos = 0;
		for (int i = end; i >= 0; i--) {
			if (binaryString.charAt(i) != '0') {
				listPositions.add(pos);
			}
			pos++;
		}
		return listPositions;
	}
	
	/**
	 * return size of the serialization of an object in bytes
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static long sizeof(Object obj) throws IOException {
	    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
	    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

	    objectOutputStream.writeObject(obj);
	    objectOutputStream.flush();
	    objectOutputStream.close();

	    return byteOutputStream.toByteArray().length;
	}
	
	/**
	 * This scales a value of range [min,max]  into new range [newMin,newMax]
	 * @param val
	 * @param min
	 * @param max
	 * @param newMin
	 * @param newMax
	 * @return
	 */
	public static float normalizedValue(float val, int min, int max, int newMin, int newMax) {
		float normalizedVal = 0;
		normalizedVal = newMin + ((val - min) * (newMax - newMin)) / (max - min);
		return normalizedVal;
	}
	
	/**
	 * Check whether a string is number or not
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(final String str) {
        boolean numeric = true;
        numeric = str.matches("-?\\d+(\\.\\d+)?");
        return numeric;
    }
	
	
	public static BigDecimal round(float d, int decimalPlace) {
	    BigDecimal bd = new BigDecimal(Float.toString(d));
	    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);       
	    return bd;
	}
	
	public static String zeroPadding(String original, int targetedLen) {
		StringBuilder resultString = new StringBuilder();
		resultString.append(original);
		while (resultString.length() < targetedLen) {
			resultString.insert(0, "0");
		}
		return resultString.toString();
	}
}
