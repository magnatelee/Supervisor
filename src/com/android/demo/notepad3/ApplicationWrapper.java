package com.android.demo.notepad3;

import android.app.Application;
import android.os.FileObserver;

import java.io.File;

public class ApplicationWrapper {
    private Application app;
    
    public ApplicationWrapper(Application a){
        app = a;   
    }

    //SNIPPET from  http://www.hrupin.com/2011/11/how-to-clear-user-data-in-your-android-application-programmatically
    //Removing Application Data
    public void clearData(){
        File cache = app.getCacheDir();
        File appDir = new File(cache.getParent());
        if(appDir.exists()){
            String[] children = appDir.list();
            for(String s: children){
                if(!s.equals("lib")){
                    deleteDir(new File(appDir,s));
                }
            }
        }
    }
    
    private static boolean deleteDir(File dir){
        if(dir != null && dir.isDirectory()){
            String[] children = dir.list();
            for(int i=0; i< children.length; i++){
                boolean success = deleteDir(new File(dir, children[i]));
                if(!success){
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
