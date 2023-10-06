package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();

        double duration = (outHour - inHour) / 3600000;

        if (duration <= 0.5) {
        	ticket.setPrice(0);
        } else {
        	if(discount) {
        		switch (ticket.getParkingSpot().getParkingType()){
		        	case CAR: {
		        		ticket.setPrice(roundPrice(duration * Fare.CAR_RATE_PER_HOUR * 0.95));
		        		break;
		        	}
		        	case BIKE: {
		        		ticket.setPrice(roundPrice(duration * Fare.BIKE_RATE_PER_HOUR * 0.95));
		        		break;
		        	}
		        	default: throw new IllegalArgumentException("Unkown Parking Type");
	        	}
        	} else {
        		switch (ticket.getParkingSpot().getParkingType()){
	        		case CAR: {
	        			ticket.setPrice(roundPrice(duration * Fare.CAR_RATE_PER_HOUR));
	        			break;
	        		}
	        		case BIKE: {
	        			ticket.setPrice(roundPrice(duration * Fare.BIKE_RATE_PER_HOUR));
	        			break;
	        		}
	        		default: throw new IllegalArgumentException("Unkown Parking Type");
        		}        		
        	}
        }
    }
    
    public void calculateFare(Ticket ticket) {
    	calculateFare(ticket, false);
    }

	public double roundPrice(double price) {
		return Math.round(price * 100.0) / 100.0;
	}

}