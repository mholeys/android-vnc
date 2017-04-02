package uk.co.mholeys.android.vnc.input;

import java.util.LinkedList;
import java.util.Queue;

import uk.co.mholeys.vnc.data.PointerPoint;
import uk.co.mholeys.vnc.display.IMouseManager;
import uk.co.mholeys.vnc.log.Logger;

/**
 * Created by Matthew on 02/04/2017.
 */

public class AndroidMouse implements IMouseManager {

    public Queue<PointerPoint> miceUpdates = new LinkedList<PointerPoint>();

    public boolean left, right, middle, mwUp, mwDown;

    public short localX, localY;
    public short remoteX, remoteY;

    public void mouseMoved(int x, int y) {
        localX = (short) x;
        localY = (short) y;
        PointerPoint p = new PointerPoint(localX, localY);
        p.left = left;
        p.right = right;
        p.middle = middle;
        p.mwUp = mwUp;
        p.mwDown = mwDown;
        boolean allowed = miceUpdates.offer(p);
        if (!allowed) {
            Logger.logger.printLn("Could not queue mouse");
        }
    }

    public void mouseClicked(boolean left, boolean right) {
        middle = false; // No middle click for now

        PointerPoint p = new PointerPoint(localX, localY);
        p.left = left;
        p.right = right;
        p.middle = middle;
        p.mwUp = mwUp;
        p.mwDown = mwDown;
        boolean allowed = miceUpdates.offer(p);
        if (!allowed) {
            System.out.println("Could not queue mouse");
        }
    }

    @Override
    public boolean sendLocalMouse() {
        return !miceUpdates.isEmpty();
    }

    @Override
    public PointerPoint getLocalMouse() {
        PointerPoint p = miceUpdates.poll();
        return p;
    }

    @Override
    public void setRemoteMouse(PointerPoint remote) {
        remoteX = remote.x;
        remoteY = remote.y;
    }

    public void addToQueue() {
        PointerPoint p = new PointerPoint(localX, localY);
        p.left = left;
        p.right = right;
        miceUpdates.offer(p);
        left = false;
        right = false;
        middle = false;
    }

}
