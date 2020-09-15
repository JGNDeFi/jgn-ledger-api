package org.jgn.api.utils;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RPCConstants {
	
    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap() {
        return Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue());
    }

    public static <K, U> Collector<Map.Entry<K, U>, ?, ConcurrentMap<K, U>> entriesToConcurrentMap() {
        return Collectors.toConcurrentMap((e) -> e.getKey(), (e) -> e.getValue());
    }


	protected static Map<String, String> flowMap() {
        return Collections.unmodifiableMap(Stream.of(
                entry("QueryAssetFlow", "jgnRPCOperations"),
                entry("OpenNewAccountFlow", "trusteeRPCOperations")).
                collect(entriesToMap()));
    }
	
	public static String toText(Map<String, String> map, String val) {
        return map.getOrDefault(val, val);
    }
	public static void main(String args[])
	{
		final Map<String, String> map = RPCConstants.flowMap();
	    System.out.println(toText(map, "QueryAssetFlow") );
			
	}
   
    
}
