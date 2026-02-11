package services;


import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class serviceUserTest {

    @Test
    @Order(1)
    void test1() {}

    @Test
    @Order(2)

    void test2() {}
}

