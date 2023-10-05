package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static ParkingService parkingService;
    String vehicleRegNumber = "ABCDEF";

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        try {
            parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up 'test' DB");
        }
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegNumber);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){
        // y mettre de quoi remettre au propre le tout après les tests
    }

    @Test
    public void testParkingACar(){
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());
        assertFalse(parkingSpot.isAvailable());
    }

    @Test
    public void testParkingLotExit(){
        Ticket advancedTicket = new Ticket();
        advancedTicket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        advancedTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR,false));
        advancedTicket.setVehicleRegNumber("ABCDEF");
        ticketDAO.saveTicket(advancedTicket);
        testParkingACar();

        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        Date outTime = ticket.getOutTime();
        Date inTime = ticket.getInTime();
        assertNotNull(ticket.getPrice());
        assertNotNull(ticket.getOutTime());
        assertTrue(outTime.compareTo(inTime) > 0);
        assertTrue(ticketDAO.updateTicket(ticket));
        assertTrue(parkingSpot.isAvailable());
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        testParkingLotExit();
        testParkingLotExit();

        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        double actualPrice = ticket.getPrice();
        double actualTicketDuration = ticket.getOutTime().getTime() - ticket.getInTime().getTime();
        double expectedPrice = actualTicketDuration * Fare.CAR_RATE_PER_HOUR * 0.95;
        assertEquals(actualPrice, expectedPrice);
    }
}
