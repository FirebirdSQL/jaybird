// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TestDriverSubscriber.java

package jdbcTest.harness;


public interface TestDriverSubscriber
{

    public abstract void receiveNotification(Object obj, String s);
}
