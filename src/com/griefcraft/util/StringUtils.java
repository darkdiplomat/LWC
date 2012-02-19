package com.griefcraft.util;

public class StringUtils{
	public static String capitalizeFirstLetter(String arg){
		if (arg.length() == 0){
			return arg;
		}
		if (arg.length() == 1){
			return arg.toUpperCase();
		}
		String str1 = arg.substring(0, 1);
		String str2 = arg.substring(1);
		return str1.toUpperCase() + str2.toLowerCase();
	}

	 public static boolean hasFlag(String[] args, String arg){
		 String str = args[0].toLowerCase();
		 return (str.equals(arg)) || (str.equals("-" + arg));
	 }

	 public static String join(String[] args, int i){
		 String str = "";
		 if ((args == null) || (args.length == 0)){
			 return str;
		 }
		 StringBuilder builder = new StringBuilder();
		 for (String str2 : args){
			 builder.append(str2+" ");
		 }
		 str = builder.toString();
		 return str.trim();
	 }

	 public static String transform(String str, char ch){
		 char[] charg = str.toCharArray();
		 for (int i = 0; i < charg.length; i++){
			 charg[i] = ch;
		 }
		 return new String(charg);
	 }
}
