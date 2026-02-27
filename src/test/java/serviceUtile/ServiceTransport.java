package serviceUtile;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTransport {
    static ServiceTransport service;
    @BeforeAll
    static void setup(){
        service = new ServiceTransport();}
    @Test
    void testAjouterPersonne() {

    }
}
