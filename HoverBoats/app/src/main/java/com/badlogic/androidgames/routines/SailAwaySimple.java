package com.badlogic.androidgames.routines;

import com.badlogic.androidgames.hoverboats.SailAwayFrom;
import com.badlogic.androidgames.hoverboats.Ship;
import com.badlogic.androidgames.hoverboats.World;

import java.util.ArrayList;
import java.util.List;

import framework.classes.Vector2;

/**
 * Created by f mgregor on 25/04/2015.
 */
public class SailAwaySimple extends Routine {
    Ship self;
    Ship enemy;
    Routine sailAway;
    List<Vector2> patrolPoints = new ArrayList<Vector2>();
    boolean fleeing = false;

    Routine patrol;

    public SailAwaySimple(){
        sailAway = new SailAwayFrom();
        patrolPoints.add(new Vector2(2000f,3500f));
        patrolPoints.add(new Vector2(2000f,3000f));
        patrolPoints.add(new Vector2(1800f,3800f));
        patrol = Routines.patrol(patrolPoints);


    }

    @Override
    public void reset() {
        start();
        sailAway.reset();
        patrol.reset();
    }

    @Override
    public void act(Ship ship, World world, float delta) {
        self = ship;
        if (!self.bb.targets.isEmpty()) {
            enemy = ship.bb.targets.get(0);
            if (self.position.dist(enemy.position)<300 && !fleeing){
                fleeing = true;
                sailAway.act(ship,world,delta);
            }
            else if (fleeing){
                sailAway.act(ship,world,delta);
                if (self.position.dist(enemy.position)>800){
                    fleeing = false;
                }
            }

        }
        else {
            patrol.act(self,world,delta);
            fleeing = false;
        }

        if (patrol.isFailure()){
            fail();
        }
    }
}
