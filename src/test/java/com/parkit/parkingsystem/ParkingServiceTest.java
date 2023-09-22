package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    @Mock
    private static ParkingSpot parkingSpot;
    @Mock
    private static Ticket ticket;

    @BeforeEach
    private void setUpPerTest() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processIncomingVehicleTest() {
        try {
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
            parkingService.processIncomingVehicle();
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any());
            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Test
    public void processExitingVehicleTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            parkingService.processExitingVehicle();
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
            parkingService.processExitingVehicle();
            verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getNextParkingNumberIfAvailableTest() {
        try {
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
            ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
            assertEquals(parkingSpot.getId(), 1);
            assertTrue(parkingSpot.isAvailable());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getNextParkingNumberIfAvailableParkingNumberNotFoundTest() {
        try {
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
            assertEquals(parkingSpot.getId(), 0);
            assertFalse(parkingSpot.isAvailable());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        parkingService.getNextParkingNumberIfAvailable();
    }

    @Test
    public void getNextParkingNumberIfAvailableParkingNumberWrongArgumentTest() {
        try {
            when(inputReaderUtil.readSelection()).thenReturn(3);
            verify(parkingSpotDAO, Mockito.times(0)).getNextAvailableSlot(any(ParkingType.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        parkingService.getNextParkingNumberIfAvailable();
    }
}
