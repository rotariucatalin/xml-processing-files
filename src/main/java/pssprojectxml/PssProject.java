package pssprojectxml;

import java.util.Timer;

public class PssProject {

    public static void main(String[] args) {

        //Create a cron job that is executed every 10 seconds
        Timer timer = new Timer();
        OrderTask orderTask = new OrderTask();
        timer.scheduleAtFixedRate(orderTask, 0, 10000);

    }
}
