package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.EventHandle;

public class EventHandleImp implements EventHandle {
    
    private volatile int inputBufferHandle = 0;
    private volatile int outputBufferHandle = 0;
    private volatile int eventStructHandle = 0;
    private int eventCount = -1;
    private int size = -1;
    private String eventName;
    private int eventId = -1;
    private volatile boolean cancelled = false;

    public EventHandleImp(String eventName){
        if (eventName == null){
            throw new NullPointerException();
        }
        this.eventName = eventName;
    }

    public String getEventName(){
        return this.eventName;
    }

    public void setSize(int size){
        this.size = size;
    }

    public int getSize(){
        return this.size;
    }

    public boolean isValid(){
        return inputBufferHandle != 0 && outputBufferHandle != 0
            && size > 0;
    }

    public void setInputBufferHandle(int handle){
        this.inputBufferHandle = handle;
    }

    public void setOutputBufferHandle(int handle){
        this.outputBufferHandle = handle;
    }

    public int getInputBufferHandle(){
        return this.inputBufferHandle;
    }

    public int getOutputBufferHandle(){
        return this.outputBufferHandle;
    }

    public void setEventCount(int eventCount){
        this.eventCount = eventCount;
    }

    public int getEventCount(){
        return this.eventCount;
    }

    public String toString(){
        return "input: " + inputBufferHandle + ", output: " 
            + outputBufferHandle + ", size: " + size
            + ", eventCount: " + eventCount;
    }

    public void setEventId(int eventId){
        this.eventId = eventId;
    }

    public int getEventId(){
       return this.eventId;
    } 

    public void cancel(){
        this.cancelled = true;
    }

    public boolean isCancelled(){
        return this.cancelled;
    }

    public void setEventStructHandle(int handle){
        this.eventStructHandle = handle;
    }

    public int getEventStructHandle(){
        return this.eventStructHandle;
    }
}
