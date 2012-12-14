package se.hitta.tar;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Stopwatch;

public class TarIndexTest
{
    @Ignore
    @Test
    public void canCreateTarIndexForHugeArchive() throws IOException
    {
        System.out.println("indexing...");
        File tarFile = new File("/Users/jebl01/dev/cyclomedia/Demo/Export/images.tar");
        Stopwatch stopwatch = new Stopwatch().start();
        TarIndex tarIndex =  new TarIndex(tarFile);
        
        System.out.println(tarIndex.size + " headers indexed in " + stopwatch.stop().elapsedMillis() + " millisec");
        
        System.out.println(tarIndex.get("3066/19805/2-90930060332_1_L_1_1.jpg"));
    }
    
    @Test
    public void canCreateTarIndex() throws IOException
    {
        URL fileName = getClass().getClassLoader().getResource("test.tar");
        File tarFile = new File(fileName.getPath());
        
        TarIndex tarIndex =  new TarIndex(tarFile);
        
        assertEquals(13, tarIndex.size);
        TarHeader header = tarIndex.get("images/top.jpg");
        assertNotNull(header);
        assertEquals("images/top.jpg", header.getName());
        assertEquals(69202, header.getSize());
        assertEquals(565760, header.getTarFileOffset());
    }
}
