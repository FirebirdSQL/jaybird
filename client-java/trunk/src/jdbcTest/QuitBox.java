// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   JDBCTestWin.java
package jdbcTest;

import java.awt.*;

class QuitBox extends Dialog
{

    public QuitBox(Frame frame)
    {
        super(frame, "Quit Application?", false);
        setResizable(false);
        setLayout(null);
        addNotify();
        resize(insets().left + insets().right + 261, insets().top + insets().bottom + 63);
        yesButton = new Button("Yes");
        add(yesButton);
        yesButton.reshape(insets().left + 68, insets().top + 10, 46, 23);
        noButton = new Button("No");
        add(noButton);
        noButton.reshape(insets().left + 135, insets().top + 10, 47, 23);
    }

    public void clickedNoButton()
    {
        handleEvent(new Event(this, 201, null));
    }

    public void clickedYesButton()
    {
        System.exit(0);
    }

    public boolean handleEvent(Event event)
    {
        if(event.id == 1001 && event.target == noButton)
        {
            clickedNoButton();
            return true;
        }
        if(event.id == 1001 && event.target == yesButton)
        {
            clickedYesButton();
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

    Button yesButton;
    Button noButton;
}
