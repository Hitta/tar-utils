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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * An input stream for the archived file described by the provided {@link TarHeader}
 *
 */
public class TarEntryInputStream extends InputStream
{
    private static final String READ_MODE = "r";
    
    private final TarHeader tarHeader;
    private final RandomAccessFile tarArchive;
    private final long eof;
    private boolean closed = false;
    
    /**
     * @param tarHeader The {@link TarHeader} for the file to create the input stream for 
     * @param tarArchive The tar archive containing the file to create the input stream for
     * @throws IOException If the tar archive cannot be opened for reading
     */
    public TarEntryInputStream(TarHeader tarHeader, File tarArchive) throws IOException
    {
        this.tarHeader = tarHeader;
        this.tarArchive = new RandomAccessFile(tarArchive, READ_MODE);
        this.eof = tarHeader.getTarFileOffset() + tarHeader.getSize();
        
        this.tarArchive.seek(this.tarHeader.getTarFileOffset());
    }
    
    
    @Override
    public int available() throws IOException
    {
        if(this.closed) throw new IOException("stream closed");
        
        long available = this.eof - this.tarArchive.getFilePointer();
        
        if(available > Integer.MAX_VALUE)
        {
            return Integer.MAX_VALUE;
        }
        
        return (int)available;
    }
    
    @Override
    public void close() throws IOException
    {
        if(!this.closed)
        {
            this.tarArchive.close();
            this.closed = true;
        }
    }
    
    @Override
    public int read() throws IOException
    {
        if(this.tarArchive.getFilePointer() < this.eof)
        {
            return this.tarArchive.read();            
        }
        return -1;
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        if(this.tarArchive.getFilePointer() >= this.eof) return -1;
        
        int available = this.available();
        
        if(b.length > available)
        {
            return this.tarArchive.read(b, 0, available);
        }
        
        return this.tarArchive.read(b);
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if(this.tarArchive.getFilePointer() >= this.eof) return -1;
        
        int available = this.available();
        
        if(len > available)
        {
            return this.tarArchive.read(b, off, available);
        }
        
        return this.tarArchive.read(b, off, len);
    }
    
    @Override
    public long skip(long n) throws IOException
    {
        if(n < 0)return 0;
        
        int available = this.available();
        
        if(n > available)
        {
            this.tarArchive.seek(this.eof);
            return available;
        }
        
        this.tarArchive.seek(this.tarArchive.getFilePointer() + n);
        return n;
    }
}
