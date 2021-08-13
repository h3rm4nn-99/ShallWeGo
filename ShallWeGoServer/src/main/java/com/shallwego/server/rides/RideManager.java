package com.shallwego.server.rides;

import com.shallwego.server.logic.entities.DestinationsByStopAndLine;
import com.shallwego.server.logic.entities.Line;
import com.shallwego.server.logic.entities.Stop;
import com.shallwego.server.logic.service.DestinationsByStopAndLineRepository;
import com.shallwego.server.service.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("singleton")
public class RideManager {

    //@Autowired
    //private DestinationsByStopAndLineRepository destinationsByStopAndLineRepository;

    private final HashSet<Ride> rides = new HashSet<>();

    public RideManager() {}

    public void addRide(Ride ride) {
        rides.add(ride);
    }

    public Ride findById(int id) {
        for (Ride ride: rides) {
            if (ride.getId() == id) {
                return ride;
            }
        }
        return null;
    }

    public List<Ride> findByLineAndDestination(Line line, String destination) {
        ArrayList<Ride> output = new ArrayList<>();
        for (Ride ride: rides) {
            if (ride.getLine().equals(line) && ride.getDestination().equals(destination)) {
                output.add(ride);
            }
        }

        return output;
    }

    public List<Ride> findByLine(Line line) {
        ArrayList<Ride> output = new ArrayList<>();
        for (Ride ride: rides) {
            if (ride.getLine().equals(line)) {
                output.add(ride);
            }
        }

        return output;
    }

    public void updateLocation(Ride ride, Location location) {
        rides.remove(ride);
        ride.setLastLocation(location);
        rides.add(ride);
        //ride.setNearestStop(findNearestStop(location, ride.getLine(), ride.getDestination()));
    }

    public void updateCrowding(Ride ride, int crowding) {
        rides.remove(ride);
        ride.setCrowding(crowding);
        rides.add(ride);
    }

    /*private Stop findNearestStop(Location location, Line line, String destination) {
        List<Stop> linesStops = line.getStops();
        List<Stop> targetStops = new ArrayList<>();

        for (Stop stop: linesStops) {
            DestinationsByStopAndLine destinationsByStopAndLine = destinationsByStopAndLineRepository.findByTargetLineAndTargetStop(stop, line).get(0);
            if (destinationsByStopAndLine.getTargetDestinations().contains(destination)) {
                targetStops.add(stop);
            }
        }

        Stop nearest = targetStops.get(0);
        double nearestDistance = location.distance(new Location(nearest.getLatitude(), nearest.getLongitude()));

        for (Stop stop: targetStops) {
            double currentDistance = location.distance(new Location(stop.getLatitude(), stop.getLongitude()));
            if (currentDistance < nearestDistance) {
                nearestDistance = currentDistance;
                nearest = stop;
            }
        }

        return nearest;
    } */
}