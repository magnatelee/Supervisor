package com.android.demo.notepad3;

import android.app.Activity;

import android.app.Application;
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
import android.view.Window;

import edu.berkeley.wtchoi.cc.*;

class ActivityState{
	public boolean isActive;
	
	public ActivityState(){
		isActive = false;
	}
	
	public void setActive(boolean b){
		isActive = b;
	}
}

enum SupervisorState{
    INIT,
    INDEPENDENT,
    STABLE,
    UNSTABLE;

    public boolean isStable(){
        return this == STABLE;
    }
    
    public boolean isUnstable(){
        return this == UNSTABLE;
    }

    public boolean isNotStable(){
        return this != STABLE;
    }

    public boolean isIndependent(){
        return this == INDEPENDENT;
    }
}


public class Supervisor implements Runnable{
	 
	private static Supervisor supervisor = null;
	private Supervisor sSupervisor;
	private Thread sThread;
	private LinkedList<SLog> sList;
	private LinkedList<SLog> sStack;
	private int tickcount = 0;
	private HashMap<Activity,ActivityState> activityStates;
	private java.io.ObjectOutputStream oos;
	private java.io.ObjectInputStream ois;
    private SupervisorState state;
    private ApplicationWrapper app_wrapper;

	
	private Supervisor(){}

	public static void init(Application app){
		if(supervisor == null){
			supervisor = new Supervisor();
			supervisor.sThread = new Thread(supervisor);
			supervisor.sList = new LinkedList<SLog>();
			supervisor.sStack = new LinkedList<SLog>();
			supervisor.activityStates = new HashMap<Activity,ActivityState>();

            supervisor.app_wrapper = new ApplicationWrapper(app);

            supervisor.state = SupervisorState.INIT;
		}
	}

    public static void clearData(){
        supervisor.app_wrapper.clearData();
    }

	@Override
	public void run(){	
		int prevsize = 0;
		
		while(true){
			//Establish Server Connection, at first
			if(state == SupervisorState.INIT)
				initiateChannel();
			
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
			
            //Sanity Check
            if(state.isStable() && this.sList.size() != 0){
                throw new RuntimeException("Unreachable program point Reached");
            }

            if(state.isStable()){
                try{
                    AckPacket ap = (AckPacket) ois.readObject();
                    state = SupervisorState.UNSTABLE;
                }
                catch(Exception e){
                    throw new RuntimeException("cannot read ack");
                }
            }
			
			synchronized(this.sList){
				if(tickcount == 0){
                    //Program is stable state
					if(prevsize == this.sList.size() && sStack.size() == 0){
                        if(state.isNotStable()){
                            if(state.isUnstable()){
						        try {
                                    //view hierarchy analysis
        							View[] views = getViewRoots();
	        						LinkedList<MonkeyView> vlist = new LinkedList<MonkeyView>();
		        					for(View v: views){
			        					//TODO: recognize interesting views
				        				//Assumption: views are sorted w.r.t Z-hierarchy
					        			Log.d("wtchoi!","<<" + v.getWidth() + "," + v.getHeight() + ">>");
						        		vlist.add((new MViewAdoptorV(v)).get());
        							}
	        						MonkeyView mroot = new MonkeyView(0,0,0,0,vlist);
		        					analyzeViewTree(new MViewAdoptorMV(mroot));
                                    Log.d("wtchoi","handle!:"+prevsize);
                                }
					    	    catch (Exception e) {
						    	    e.printStackTrace();
							        System.exit(1);
						        }
                                state = SupervisorState.STABLE;
                            }
                            sList.clear();
                        }
						else{
                            throw new RuntimeException("Unreachable program point Reached");
                        }
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

	private void initiateChannel(){
		java.net.Socket socket = null;
		try {
			socket = new java.net.Socket("10.0.1.2",13339);
			//Log.d("wtchoi","connected!");
		
			oos = new java.io.ObjectOutputStream(socket.getOutputStream());
			ois = new java.io.ObjectInputStream(socket.getInputStream());
			
			Log.d("wtchoi","stream initialized");
			state = SupervisorState.UNSTABLE;
		} catch (Exception e) {
			Log.d("wtchoi","cannot connect to the server");
            state = SupervisorState.INDEPENDENT;
		}
	}
	
	private View getActivityViewRoot(Activity a){
		Window rootW = a.getWindow();
		while(true){
			if(rootW.getContainer() == null) break;
			rootW = rootW.getContainer();
		}
		return rootW.getDecorView();
	}
	
	private View[] getViewRoots() 
			throws ClassNotFoundException, SecurityException, 
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException
	{
		//Code snippet from ROBOTIUM
		//SNIPPET START
		Class<?> windowManagerImpl = Class.forName("android.view.WindowManagerImpl");
		Field viewsField = windowManagerImpl.getDeclaredField("mViews");
	
		String windowManagerString;
		if(android.os.Build.VERSION.SDK_INT >= 13)
			windowManagerString = "sWindowManager";
		else
			windowManagerString = "mWindowManager";
	
		Field instanceField = windowManagerImpl.getDeclaredField(windowManagerString);
	
		viewsField.setAccessible(true);
		instanceField.setAccessible(true);
		Object instance = instanceField.get(null);
		return (View[]) viewsField.get(instance);
	}
	
	private boolean analyzeViewTree(MViewAdoptor v){
		if(state.isIndependent())
			return true;
		
		//Generate and send data
		MonkeyView mv = v.get();
		Log.d("wtchoi","mv Built");
		try{
			oos.writeObject(mv); // View Hierarchy
			oos.flush();
		}
		catch (IOException e){
			Log.d("wtchoi","Error writing to the socket");
			return false;
		}
		return true;
	}
	
	public static void start(){
		supervisor._start();
	}
	
	private void _start(){
		if(sThread.isAlive()) return;
		sThread.start();
	}
	
	private void snooze(){
		tickcount = (tickcount > 5)?tickcount:5;
	}
	
	private Activity getCurrentActivity(){	
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
