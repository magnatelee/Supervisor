package com.android.demo.notepad3;

class SLog{
	public static final int CALL = 1;
	public static final int RETURN = 2;
	public static final int TRUE = 3;
	public static final int FALSE = 4;
	public static final int SWITCH = 5;
	
	public int type;
	public String message;
	public Object obj;
	
	public SLog(int lt, String m, Object o){
		type = lt;
		message = m;
		obj = o;
	}
	
	public String toString(){
		String typ = null;
		switch(type){
			case CALL:
				typ = "CALL";
				break;
			case RETURN:
				typ = "RETURN";
				break;
			default:
				break;
		}
		return typ+"("+message+","+obj+")";
	}
}