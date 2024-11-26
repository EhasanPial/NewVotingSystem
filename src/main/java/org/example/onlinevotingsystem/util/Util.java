package org.example.onlinevotingsystem.util;

import java.util.List;

public class Util {

	public static String weightTilesToString(List<String> weightTiles) {
        StringBuilder sb = new StringBuilder();
        for (String tile : weightTiles) {
            sb.append(tile+"-");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
}
