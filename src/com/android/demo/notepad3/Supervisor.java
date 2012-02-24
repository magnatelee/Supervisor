package com.android.demo.notepad3;

import android.app.Activity;

import android.util.Log;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import android.os.*;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import edu.berkeley.wtchoi.cc.*;

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


class ActivityState{
	public boolean isActive;
	
	public ActivityState(){
		isActive = false;
	}
	
	public void setActive(boolean b){
		isActive = b;
	}
}


public class Supervisor implements Runnable{
	private Supervisor(){}

	@Override
	public void run(){
		int prevsize = 0;
		
		while(true){
			Activity activeActivity = getCurrentActivity();
			//Tick Sleep
			try{
				if(activeActivity == null){
					//App is Inactive
					Thread.sleep(1000);
					continue;
				}
				else{
					Thread.sleep(100);
				}
			}
			catch(InterruptedException e){}
			
			//Log.d("wtchoi","Loop"+tickcount+","+sList.size()+","+sStack.size());
			//if(sStack.size() == 1)
			//	Log.d("wtchoi",sStack.getLast().toString());
			
			synchronized(this.sList){
				if(tickcount == 0){
					if(prevsize == this.sList.size() && sStack.size() == 0){
						//TODO : recognize this is new state. only send information at new state
						//view hierarchy analysis
						View root = activeActivity.getWindow().getDecorView();
						
						if(analyzeViewTree(root)){
							sList.clear();
						}
						Log.d("wtchoi","handle!: "+prevsize);
					}
					tickcount = 10;
				}
				else{
					tickcount--;
				}
				prevsize = sList.size();
			}
		}
	}

	
	 
	private static Supervisor supervisor = null;
	private Supervisor sSupervisor;
	private Thread sThread;
	private LinkedList<SLog> sList;
	private LinkedList<SLog> sStack;
	private int tickcount = 0;
	private HashMap<Activity,ActivityState> activityStates;
	
		
	private static boolean analyzeViewTree(View v){
		//1. Establish Server Connection
		java.net.Socket socket = null;
		java.io.ObjectOutputStream oos = null;
		
		try {
			socket = new java.net.Socket("128.32.45.173",13339);
			Log.d("wtchoi","connected!");
			java.io.OutputStream out = socket.getOutputStream();
			Log.d("wtchoi","get stream");
			oos = new java.io.ObjectOutputStream(out);
			Log.d("wtchoi","get object stream");
		} catch (Exception e) {
			Log.d("wtchoi","cannot connect to the server");
			return false;
			//e.printStackTrace();
		}
		Log.d("wtchoi","stream initialized");
		
		
		//2. Generate and send data
		MonkeyView mv = MViewBuilder.build(v);
		Log.d("wtchoi","mv Built");
		try{
			oos.writeObject(mv); // View Hierarchy
			oos.flush();
		}
		catch (IOException e){
			Log.d("wtchoi","Error writing to the socket");
			return false;
		}
		
		//3. Finishing Connection
		try{
			oos.close();
			socket.close();
		}
		catch(Exception e){}
		return true;
	}
	
	public static void init(){
		if(supervisor == null){
			supervisor = new Supervisor();
			supervisor.sThread = new Thread(supervisor);
			supervisor.sList = new LinkedList<SLog>();
			supervisor.sStack = new LinkedList<SLog>();
			supervisor.activityStates = new HashMap<Activity,ActivityState>();
		}
	}
	
	public static void start(){
		supervisor._start();
	}
	
	private void _start(){
		if(sThread.isAlive()) return;
		sThread.start();
	}
	
	public static void snooze(){
		supervisor._snooze();
	}
	private void _snooze(){
		tickcount = (tickcount > 5)?tickcount:5;
	}

	public static Activity getCurrentActivity(){
		return supervisor._getCurrentActivity();
	}
	
	private Activity _getCurrentActivity(){	
		for(Entry<Activity, ActivityState> e:activityStates.entrySet()){
			if(e.getValue().isActive)
				return e.getKey();
		}
		return null;
	}
	
	public static void logCall(String f, Object o){
		supervisor._logCall(f,o);
	}
	
	private void _logCall(String fname, Object o){
		synchronized(sList){
			SLog log = new SLog(SLog.CALL,fname,o);
			sList.add(log);
			sStack.add(log);
			snooze();
		}
	}
	
	public static void logReturn(String fname, Object o){
		supervisor._logReturn(fname,o);
	}
	
	private void _logReturn(String fname, Object o){
		synchronized(sList){
			SLog log = new SLog(SLog.RETURN,fname,o);
			sList.add(log);
			
			SLog stackTop = sStack.getLast();
			if(stackTop.type == SLog.CALL && stackTop.obj == o && stackTop.message == fname){
				sStack.removeLast();
			}
			else{
				Log.d("wtchoi","stack top:"+stackTop.toString());
				Log.d("wtchoi","new log:"+log.toString());
				Log.d("wtchoi","somethings is wrong! call return missmatch");
				System.exit(0);
			}
			snooze();
		}
	}
	
	public static void logTrue(String fname, Object o){
		supervisor._logTrue(fname,o);
	}
	
	private void _logTrue(String fname, Object o){
		synchronized(sList){
			sList.add(new SLog(SLog.TRUE,fname,o));
			snooze();
		}
	}
	
	public static void logFalse(String f, Object o){
		supervisor._logFalse(f,o);
	}
	
	private void _logFalse(String fname, Object o){
		synchronized(sList){
			sList.add(new SLog(SLog.FALSE,fname,o));
			snooze();
		}
	}
	
	public static void logSwitch(String sname, Object o){
		supervisor._logSwitch(sname,o);
	}
	
	private void _logSwitch(String sname, Object o){
		synchronized(sList){
			sList.add(new SLog(SLog.SWITCH,sname,o));
			snooze();
		}
	}

	public static void logActivityCreated(Activity a){
		supervisor._logActivityCreated(a);
	}
	
	private void _logActivityCreated(Activity a){
		Log.d("wtchoi","Activity(" + a.toString() + ") is Created");
		activityStates.put(a, new ActivityState());
		snooze();
	}
	
	public static void logStart(Activity a){
		supervisor._logStart(a);
	}
	
	private void _logStart(Activity a){
		Log.d("wtchoi","Activity(" + a.toString() + ") is Started");
		activityStates.get(a).setActive(true);
		snooze();
	}
	
	public static void logStop(Activity a){
		supervisor._logStop(a);
	}
	
	private void _logStop(Activity a){
		Log.d("wtchoi","Activity(" + a.toString() + ") is Stoped");
		ActivityState t = activityStates.get(a);
		t.setActive(false);
		snooze();
	}
}
