package org.mupdfdemo2.model;

public class ChoosePDFItem {
	public final static int PARENT = 0;
	public final static int DIR = 1;
	public final static int DOC = 2;
	
	public final int type;
	public final String name;

	public ChoosePDFItem (int t, String n) {
		type = t;
		name = n;
	}
}
