package org.aprestos.code.fw;
import org.junit.Test;import org.junit.Before;
import org.junit.After;import static org.junit.Assert.*;
public class DefaultTest{
int four = 0;
@Test public void example(){assertEquals(4,four);}
@Before public void prepareTestData() {four=4;}
@After public void cleanupTestData() {four=0;}
}