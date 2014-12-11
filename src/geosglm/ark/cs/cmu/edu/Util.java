package geosglm.ark.cs.cmu.edu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Util {

	public static ArrayList<Object> sortHashMapByValue(HashMap<?, ?> hm) {
		
		ArrayList<Object> sortedCollocates=new ArrayList<Object>();
		
		Set entries2=hm.entrySet();
		
		Map.Entry[] entries=new Map.Entry[entries2.size()];
		
		Iterator<Map.Entry> it=entries2.iterator();
		int n=0;
		while(it.hasNext()) {
			entries[n]=it.next();
			n++;
		}
	
		Arrays.sort(entries, new Comparator() {
			public int compare(Object lhs, Object rhs) {
			Map.Entry le = (Map.Entry)lhs;
			Map.Entry re = (Map.Entry)rhs;
			return ((Comparable)re.getValue()).compareTo((Comparable)le.getValue());}}
		);
				
		for (int i=0; i<entries.length; i++) {
			Map.Entry<Object, Integer> entry=entries[i];
			sortedCollocates.add(entry.getKey());
		}
		
		return sortedCollocates;

	}

}
