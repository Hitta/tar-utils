/*
 * Copyright 2012 Hittapunktse AB (http://www.hitta.se/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.hitta.tar;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;

import static se.hitta.tar.TarHeader.*;

/**
 * An iterator that iterates over {@link TarHeader}s in the given tar archive.<br>
 * The iterator will create a {@link RandomAccessFile} for the provided {@link File}. The {@link RandomAccessFile}
 * will be closed when the iterator reaches the end of the tar archive, or if an error occurs.<br><br>
 * <b>Note! the caller has to explicitly close the iterator if iteration is intentionally ended prematurely.</b>
 *
 */
public class TarHeaderIterator implements Iterator<TarHeader>, Closeable
{
    private static final String READ_MODE = "r";
    private RandomAccessFile file;
    private final long length;
    
    public TarHeaderIterator(File file) throws IOException
    {
        this.file =  new RandomAccessFile(file, READ_MODE);;
        this.length = file.length();
    }
    
    @Override
    public boolean hasNext()
    {
        try
        {
            if (file.getFilePointer() >= (length - DATA_BLOCK * 2))
            {
                IOUtils.closeQuietly(this.file);
                return false;
            }
            
            return getNextHeader();
            
        } catch (IOException e)
        {
            IOUtils.closeQuietly(this.file);
            return false;
        }
    }
    
    private TarHeader nextHeader =  null;
    
    private boolean getNextHeader()
    {
        if(this.nextHeader != null)
        {
            return true;
        }
        
        try
        {
            byte[] header = new byte[HEADER_BLOCK];
            file.read(header, 0, HEADER_BLOCK);
            
            TarHeader tarHeader = TarHeader.build(header, file.getFilePointer());
            
            long fileOffset = tarHeader.getSize();
            
            //adjust file offset for block padding
            if(tarHeader.getSize() % DATA_BLOCK > 0)
            {
                fileOffset = tarHeader.getSize() + (DATA_BLOCK -(tarHeader.getSize() % DATA_BLOCK));                
            }

            file.seek(file.getFilePointer() + fileOffset);

            this.nextHeader = tarHeader;
            
            return true;
            
        } catch (Throwable e)
        {
            return false;
        }            
    }
    
    @Override
    public TarHeader next()
    {
        try
        {
            getNextHeader();
            return this.nextHeader;
        }
        finally
        {
            this.nextHeader = null;
        }
    }

    @Override
    public void remove()
    {
        throw new RuntimeException("this iterator doesn't support removal");
    }

    @Override
    public void close() throws IOException
    {
        IOUtils.closeQuietly(this.file);
    }
}