package se.hitta.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import com.google.common.base.Stopwatch;

public class TarIndexTest
{

    @Test
    public void canCreateTarIndexForHugeArchive() throws IOException
    {
        System.out.println("indexing...");
        File tarFile = new File("/data/streetview/2012-12-19#3066_19805.tar");
        Stopwatch stopwatch = new Stopwatch().start();
        TarIndex tarIndex =  new TarIndex(tarFile);
        
        System.out.println(tarIndex.getSize() + " headers indexed in " + stopwatch.stop().elapsedMillis() + " millisec");
        
        System.out.println("ready");
    }
    
    @Test
    public void canCreateTarIndex() throws IOException
    {
        URL fileName = getClass().getClassLoader().getResource("test.tar");
        File tarFile = new File(fileName.getPath());
        
        TarIndex tarIndex =  new TarIndex(tarFile);
        
        assertEquals(15, tarIndex.getSize());
        TarHeader header = tarIndex.get("images/top.jpg").get();
        assertNotNull(header);
        assertEquals("images/top.jpg", header.getName());
        assertEquals(69202, header.getSize());
        assertEquals(565760, header.getTarFileOffset());
    }
}
