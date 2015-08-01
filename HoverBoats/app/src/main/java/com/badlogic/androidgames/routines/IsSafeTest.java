package com.badlogic.androidgames.routines;

import com.badlogic.androidgames.hoverboats.Ship;
import com.badlogic.androidgames.hoverboats.World;

/**
 * Created by f mgregor on 24/04/2015.
 */
public class IsSafeTest extends Routine {
    @Override
    public void reset() {
        start();
    }

    @Override
    public void act(Ship ship, World world, float delta) {
        if(isRunning()){
            if(ship.bb.inDanger){
                fail();
            }else {
                succeed();
            }
        }

    }
}
