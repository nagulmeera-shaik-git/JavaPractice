import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Jcoding1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String text = "my self nagulmeera java my self sudheer java";
		Map<String, Integer> lnMap = new LinkedHashMap<>();
		for (String word : text.split(" ")) {
			lnMap.put(word, lnMap.getOrDefault(word, 0) + 1);
		}
		System.out.println(lnMap);
		System.out.println("===================================");
		Arrays.asList(text.split(" ")).stream().forEach(e -> System.out.print(e + "*  "));

	}

}
