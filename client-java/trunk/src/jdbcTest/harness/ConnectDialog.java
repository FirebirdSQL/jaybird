// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ConnectDialog.java

package jdbcTest.harness;

import java.awt.*;

public class ConnectDialog extends Dialog
{

    public ConnectDialog(Frame frame)
    {
        super(frame, "Connection Strings", false);
        setLayout(null);
        addNotify();
        resize(502, 229);
        label1 = new Label("User ID:", 2);
        add(label1);
        label1.reshape(25, 30, 130, 21);
        tfUserID = new TextField(31);
        tfUserID.setFont(new Font("Courier", 0, 12));
        add(tfUserID);
        tfUserID.reshape(172, 30, 262, 33);
        label2 = new Label("Password", 2);
        add(label2);
        label2.reshape(25, 75, 130, 21);
        tfPassword = new TextField(31);
        add(tfPassword);
        tfPassword.reshape(172, 75, 262, 33);
        label3 = new Label("Connection URL:", 2);
        add(label3);
        label3.reshape(25, 120, 130, 21);
        tfUrl = new TextField(31);
        add(tfUrl);
        tfUrl.reshape(172, 120, 262, 33);
        button1 = new Button("OK");
        add(button1);
        button1.reshape(140, 165, 140, 23);
        setResizable(false);
        tfPassword.setEchoCharacter('*');
    }

    public void clickedOKButton()
    {
        hide();
    }

    public String getPassword()
    {
        return tfPassword.getText();
    }

    public String getUrl()
    {
        return tfUrl.getText();
    }

    public String getUserID()
    {
        return tfUserID.getText();
    }

    public boolean handleEvent(Event event)
    {
        if(event.id == 1001 && event.target == button1)
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
    TextField tfUserID;
    Label label2;
    TextField tfPassword;
    Label label3;
    TextField tfUrl;
    Button button1;
}
