package se.hitta.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Stopwatch;

public class TarIndexTest
{

    @Ignore //this test has an external dependency and should only be used as a lab...
    @Test
    public void canCreateTarIndexForHugeArchive() throws IOException
    {
        System.out.println("indexing...");
        File tarFile = new File("/data/streetview/2013-03-14#3207_19207.tar");
        Stopwatch stopwatch = new Stopwatch().start();
        TarIndex tarIndex = new TarIndex(tarFile);

        System.out.println(tarIndex.getSize() + " headers indexed in " + stopwatch.stop().elapsedMillis() + " millisec");

        System.out.println("ready");
    }

    @Test
    public void canCreateTarIndex() throws IOException
    {
        URL fileName = getClass().getClassLoader().getResource("test.tar");
        File tarFile = new File(fileName.getPath());

        TarIndex tarIndex = new TarIndex(tarFile);

        assertEquals(15, tarIndex.getSize());
        TarHeader header = tarIndex.get("images/top.jpg").get();
        assertNotNull(header);
        assertEquals("images/top.jpg", header.getName());
        assertEquals(69202, header.getSize());
        assertEquals(565760, header.getTarFileOffset());
    }

    @Test
    public void canSerializeTarIndex() throws IOException
    {
        URL fileName = getClass().getClassLoader().getResource("test.tar");
        File tarFile = new File(fileName.getPath());

        TarIndex tarIndexExpected = new TarIndex(tarFile);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        tarIndexExpected.serialize(bos);
        
        TarIndex tarIndex = TarIndex.deserialize(new ByteArrayInputStream(bos.toByteArray()));
        
        assertEquals(tarIndexExpected.getSize(), tarIndex.getSize());
        assertTrue(tarIndexExpected.getLastModified().equals(tarIndex.getLastModified()));
    }
    
    @Test
    public void canDeserializeTarIndex() throws IOException
    {
        URL fileName = getClass().getClassLoader().getResource("test-large.ser");
        File tarFile = new File(fileName.getPath());
        
        TarIndex tarIndex2 = TarIndex.deserialize(tarFile);
        assertEquals(19778, tarIndex2.getSize());
    }
}
