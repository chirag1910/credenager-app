package com.credenager.utils;

public class Crypt {
    private static int[] textToAsciis(String s){
        char[] chars = s.toCharArray();
        int[] asciis = new int[chars.length];

        for (int i = 0; i < chars.length; i++){
            asciis[i] = chars[i];
        }

        return asciis;
    }

    private static String asciisToText(int[] asciis){
        StringBuilder s = new StringBuilder();

        for(int a : asciis){
            s.append((char) a);
        }

        return s.toString();
    }

    private static int customReduce(int[] arr, int initialValue){
        int res = initialValue;

        for (int i : arr){
            res = res ^ i;
        }

        return res;
    }

    private static String asciisToHexString (int[] asciis){
        StringBuilder hexString = new StringBuilder();

        for (int a: asciis){
            String hex = "000" + Integer.toHexString(a);
            hexString.append(hex.substring(hex.length() - 4));
        }

        return hexString.toString();
    }

    private static int[] hexStringToAsciis (String hexString){
        int[] asciis = new int[hexString.length() / 4];

        for (int i = 0; i < asciis.length; i ++){
            String asciiString = hexString.substring(i*4, i*4 + 4);
            asciis[i] = Integer.parseInt(asciiString, 16);
        }

        return asciis;
    }

    public static String encrypt (String text, String salt){
        int[] saltAsciis = textToAsciis(salt);

        int[] textAsciis = textToAsciis(text);

        int[] textAsciisSalted = new int[textAsciis.length];
        for (int i = 0; i < textAsciisSalted.length; i++){
            textAsciisSalted[i] = customReduce(saltAsciis, textAsciis[i]);
        }

        return asciisToHexString(textAsciisSalted);
    }

    public static String decrypt (String text, String salt){
        int[] saltAsciis = textToAsciis(salt);

        int[] textAsciisSalted = hexStringToAsciis(text);

        int[] textAsciis = new int[textAsciisSalted.length];
        for (int i = 0; i < textAsciis.length; i++){
            textAsciis[i] = customReduce(saltAsciis, textAsciisSalted[i]);
        }

        return asciisToText(textAsciis);
    }
}
