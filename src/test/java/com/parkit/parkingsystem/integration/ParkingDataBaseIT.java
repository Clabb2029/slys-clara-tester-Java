package com.parkit.parkingsystem.integration;
import com.parkit.parkingsystem.constants.Fare;
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
    }

    @AfterAll
    private static void tearDown(){
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testParkingACar(){
        dataBasePrepareService.clearDataBaseEntries();
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());
        assertFalse(parkingSpot.isAvailable());
    }

    @Test
    public void testParkingLotExit(){
        Ticket newTicket = new Ticket();
        newTicket.setInTime(new Date(System.currentTimeMillis() - (60*60*10000)));
        newTicket.setParkingSpot(parkingService.getNextParkingNumberIfAvailable());
        newTicket.setVehicleRegNumber(vehicleRegNumber);
        ticketDAO.saveTicket(newTicket);

        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        Date inTime = ticket.getInTime();
        Date outTime = ticket.getOutTime();
        assertTrue(ticket.getPrice() > 0);
        assertNotNull(outTime);
        assertTrue(outTime.compareTo(inTime) > 0);
        assertTrue(parkingSpotDAO.updateParking((parkingSpot)));
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        testParkingLotExit();
        testParkingLotExit();

        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        double actualPrice = ticket.getPrice();
        double inTime = ticket.getInTime().getTime();
        double outTime = ticket.getOutTime().getTime();
        double actualTicketDuration = (outTime - inTime) / 3600000;
        double expectedPrice = Math.round(actualTicketDuration * Fare.CAR_RATE_PER_HOUR * 0.95 * 100.0) / 100.0;
        assertEquals(actualPrice, expectedPrice);
        dataBasePrepareService.clearDataBaseEntries();
    }
}
