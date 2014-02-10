package com.ax003d.sichu.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PinYin4j {
	
	
	public PinYin4j(){
	}
	/**
	 * �ַ���ת���ַ�(���ŷָ�)
	 * 
	 * @author wyh
	 * @param stringSet
	 * @return
	 */
	public String makeStringByStringSet(Set<String> stringSet) {
		StringBuilder str = new StringBuilder();
		int i = 0;
		for (String s : stringSet) {
			if (i == stringSet.size() - 1) {
				str.append(s);
			} else {
				str.append(s + ",");
			}
			i++;
		}
		return str.toString().toLowerCase();
	}

	
	/**
	 * ��ȡƴ������
	 * 
	 * @author wyh
	 * @param src
	 * @return Set<String>
	 */
	public Set<String> getPinyin(String src) {
			char[] srcChar;
			srcChar = src.toCharArray();

			//1:���ٸ�����
			//2:ÿ�����ֶ����ֶ���
			String[][] temp = new String[src.length()][];
			for (int i = 0; i < srcChar.length; i++) {
				char c = srcChar[i];
				// �����Ļ���a-z����A-Zת��ƴ��(�ҵ������Ǳ������Ļ���a-z����A-Z)
				if (String.valueOf(c).matches("[\\u4E00-\\u9FA5]+")) {
						String[] t = PinyinHelper.getUnformattedHanyuPinyinStringArray(c);
						temp[i] = new String[t.length];
						for(int j=0;j<t.length;j++){
							temp[i][j]=t[j].substring(0,1);//��ȡ����ĸ
						}
				} else if (((int) c >= 65 && (int) c <= 90)
						|| ((int) c >= 97 && (int) c <= 122)||c>=48&&c<=57||c==42) {//a-zA-Z0-9*
					temp[i] = new String[] { String.valueOf(srcChar[i]) };
				} else {
					temp[i] = new String[] {"null!"};
				}
				
			}
			String[] pingyinArray = paiLie(temp);
			return array2Set(pingyinArray);//Ϊ��ȥ���ظ���
	}
	
	/*
	 * ��2ά������������������
	 * ����:{{1,2},{3},{4},{5,6}}����2������,Ϊ:1345,1346,2345,2346
	 */
	private String[] paiLie(String[][] str){
		int max=1;
		for(int i=0;i<str.length;i++){
			max*=str[i].length;
		}
		String[] result=new String[max];
		for(int i = 0; i < max; i++){
	            String s = "";
	            int temp = 1;      //ע�����temp���÷���
	            for(int j = 0; j < str.length; j++){
	                temp *= str[j].length;
	                s += str[j][i / (max / temp) % str[j].length];
	            }
	            result[i]=s;
	    }
		
		return result;
	}
	
	public static <T extends Object> Set<T> array2Set(T[] tArray) {   
        Set<T> tSet = new HashSet<T>(Arrays.asList(tArray));   
        // TODO û��һ����λ�ķ�������ݾ�������ã�ѡ����ʵ�Set��������ת����   
        return tSet;   
    } 
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//nongyeyinheng,nongyeyinhang,nongyeyinxing
		PinYin4j t=new PinYin4j();
		String str = "ũҵ����1234567890abcdefghijklmnopqrstuvwxyz*";
		System.out.println(t.makeStringByStringSet(t.getPinyin(str)));
	}

}
