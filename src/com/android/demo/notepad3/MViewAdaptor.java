package com.android.demo.notepad3;
//INSTRUMENTATION

import java.util.LinkedList;

import android.view.View;
import android.view.ViewGroup;
import edu.berkeley.wtchoi.cc.MonkeyView;

public interface MViewAdaptor {
	public MonkeyView get(); 
}

class MViewAdaptorV implements MViewAdaptor {
	private View mv;
	private MonkeyView mmv;
	
	public MViewAdaptorV(View v){
		mv = v;
		mmv = null;
	}
	
	private static MonkeyView build(View v){
		//Log.d("wtchoi","MViewBuilder.biuld");
		LinkedList<MonkeyView> ch;
		try{
			ViewGroup vg = (android.view.ViewGroup)v;
			ch = new LinkedList<MonkeyView>();
			for(int i =0;i<vg.getChildCount(); i++){
				ch.add(build(vg.getChildAt(i)));
			}
		}
		catch(Exception e){
			ch = null;
		}
		
		MonkeyView mv = new MonkeyView(v.getLeft(), v.getTop(), v.getWidth(),v.getHeight(), ch);
		//Log.d("wtchoi","MViewBuilder.built==");
		//Log.d("wtchoi","top="+Integer.toString(v.getTop()));
		//Log.d("wtchoi","top="+Integer.toString(mv.getX()));
		return mv;
	}
	
	public MonkeyView get(){
		if(mmv == null){
			mmv = build(mv);
		}
		return mmv;
	}
}

class MViewAdaptorMV implements MViewAdaptor {
	private MonkeyView mmv;
	
	public MViewAdaptorMV(MonkeyView mv){
		mmv = mv;
	}
	
	public MonkeyView get(){return mmv;}
}


