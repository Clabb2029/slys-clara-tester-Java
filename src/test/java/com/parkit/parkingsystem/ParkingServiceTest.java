package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.junit.MockitoTestListener;
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

    @Mock
    private static FareCalculatorService fareCalculatorService;



    @BeforeEach
    private void setUpPerTest() {
        try {
            // when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            // when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            // when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
            // when(inputReaderUtil.readSelection()).thenReturn(1);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    // erreur Wanted but not invoked: ticketDAO.saveTicket(<any com.parkit.parkingsystem.model.Ticket>);
    @Test
    public void processIncomingVehicleTest() {
        try {
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any());
            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        parkingService.processIncomingVehicle();

    }

    // erreur "Wanted but not invoked: parkingSpotDAO.updateParking(<any com.parkit.parkingsystem.model.ParkingSpot>);
    @Test
    public void processExitingVehicleTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.getNbTicket(anyString())).thenReturn(2);
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(ticket.getParkingSpot()).thenReturn(parkingSpot);
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        parkingService.processExitingVehicle();
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.getNbTicket(anyString())).thenReturn(2);
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
            verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        parkingService.processExitingVehicle();
    }

    // erreur "expected 0, actual 1"
    @Test
    public void getNextParkingNumberIfAvailableTest() {
        try {
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
            assertEquals(parkingSpot.getId(), 1);
            assertTrue(parkingSpot.isAvailable());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        parkingService.getNextParkingNumberIfAvailable();
    }

    // test réussi
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

    // test réussi
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
