// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TestDriverInterface.java

package jdbcTest.harness;


// Referenced classes of package jdbcTest.harness:
//            TestDriverSubscriber

public interface TestDriverInterface
{

    public abstract String getString(String s, String s1, String s2);

    public abstract ThreadGroup getThreadGroup();

    public abstract void start();

    public abstract void stop();

    public abstract void subscribe(TestDriverSubscriber testdriversubscriber);
}
