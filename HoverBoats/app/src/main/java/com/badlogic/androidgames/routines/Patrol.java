package com.badlogic.androidgames.routines;

import com.badlogic.androidgames.hoverboats.Ship;
import com.badlogic.androidgames.hoverboats.World;

import java.util.ArrayList;
import java.util.List;

import framework.classes.Vector2;

/**
 * Created by New PC 2012 on 08/02/2015.
 */
public class Patrol extends Routine {

    boolean first = true;
    List<Vector2> points;
    List<Routine> routines;
    Routine sequence;
    Routine repeat;


    public Patrol(List<Vector2> points){

        this.points = points;
        this.routines = new ArrayList<Routine>();


    }

    @Override
    public void reset() {
        start();
        first = true;

    }

    @Override
    public void act(Ship ship, World world, float delta) {
        if (isRunning()){
            if (first){
                findStartPoint(ship.position);
                first = false;
                for (Vector2 point : points){
                    routines.add(Routines.moveTo(point.x,point.y));
                    System.out.println("MoveTo: " + point.display());
                }
                sequence = Routines.sequence(routines);
                repeat = Routines.repeat(sequence);
                repeat.reset();
            }

            repeat.act(ship, world, delta);

            if (sequence.isFailure()){
                fail();
                System.out.println("patrol failed");
            }


        }


    }

    private void findStartPoint(Vector2 position){
        float closest = 1000000f;
        List<Vector2> newPoints;
        int i = 0;
        int index = 0;
        for (Vector2 point : points){

            float dist = point.dist(position);
            if (closest > dist){
                closest = dist;
                index = i;
            }
            i ++;
        }
        if (index != 0) {
            System.out.println("Closest: " + index);
            newPoints = points.subList(index, points.size());

            newPoints.addAll(points.subList(0, index - 1));
            points = newPoints;
        }
    }
}
