// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   JDBCTestWin.java
package jdbcTest;

import java.awt.*;

class AboutBox extends Dialog
{

    public AboutBox(Frame frame)
    {
        super(frame, "About", false);
        setResizable(false);
        setLayout(null);
        addNotify();
        resize(insets().left + insets().right + 291, insets().top + insets().bottom + 83);
        label1 = new Label("JDBC Test Suite");
        label1.setFont(new Font("Dialog", 1, 14));
        add(label1);
        label1.reshape(insets().left + 15, insets().top + 18, 161, 16);
        OKButton = new Button("OK");
        add(OKButton);
        OKButton.reshape(insets().left + 194, insets().top + 18, 73, 23);
    }

    public void clickedOKButton()
    {
        handleEvent(new Event(this, 201, null));
    }

    public boolean handleEvent(Event event)
    {
        if(event.id == 1001 && event.target == OKButton)
        {
            clickedOKButton();
            return true;
        }
        if(event.id == 201)
        {
            hide();
            return true;
        } else
        {
            return super.handleEvent(event);
        }
    }

    public synchronized void show()
    {
        Rectangle rectangle = getParent().bounds();
        Rectangle rectangle1 = bounds();
        move(rectangle.x + (rectangle.width - rectangle1.width) / 2, rectangle.y + (rectangle.height - rectangle1.height) / 2);
        super.show();
    }

    public synchronized void wakeUp()
    {
        notify();
    }

    Label label1;
    Button OKButton;
}
