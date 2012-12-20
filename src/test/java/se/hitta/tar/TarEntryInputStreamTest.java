package se.hitta.tar;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class TarEntryInputStreamTest
{
    @Test
    public void canReadFromTarArchive() throws IOException
    {
        String expected = IOUtils.toString(getClass().getClassLoader().getResource("glUtils.js"));
        
        File tarFile = new File(getClass().getClassLoader().getResource("test.tar").getPath());
        
        TarIndex tarIndex =  new TarIndex(tarFile);
        
        TarHeader header = tarIndex.get("scripts/glUtils.js").get();
        
        
        InputStream stream = new TarEntryInputStream(header, tarFile);
        
        try
        {
            assertEquals(expected, IOUtils.toString(stream));            
        }
        finally
        {
            stream.close();            
        }
    }
}
