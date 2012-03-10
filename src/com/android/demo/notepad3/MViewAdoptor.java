package com.android.demo.notepad3;
//INSTRUMENTATION

import java.io.Serializable;
import java.util.LinkedList;

import android.view.View;
import android.view.ViewGroup;
import edu.berkeley.wtchoi.cc.MonkeyView;

import android.util.Log;

public interface MViewAdoptor{
	public MonkeyView get(); 
}

class MViewAdoptorV implements MViewAdoptor{
	private View mv;
	private MonkeyView mmv;
	
	public MViewAdoptorV(View v){
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

class MViewAdoptorMV implements MViewAdoptor{
	private MonkeyView mmv;
	
	public MViewAdoptorMV(MonkeyView mv){
		mmv = mv;
	}
	
	public MonkeyView get(){return mmv;}
}


