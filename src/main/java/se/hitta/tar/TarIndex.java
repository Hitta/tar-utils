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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Iterators;

/**
 * This class will build an index of a provided tar archive.
 */
/**
 * @author jebl01
 *
 */
/**
 * @author jebl01
 *
 */
/**
 * @author jebl01
 *
 */
public class TarIndex implements Serializable
{
    private static final long serialVersionUID = 2521850273227117136L;

    private final TarHeader[] headers;
    
    private final File tarFile;
    private final Date lastModified;
    
    /**
     * @param tarFile the tar archive to index
     * @throws IOException if the file cannot be opened for reading
     */
    public TarIndex(File tarFile) throws IOException
    {
        this.tarFile = tarFile;
        this.lastModified = DateUtils.truncate(new Date(tarFile.lastModified()), Calendar.SECOND); //trim milliseconds
        
        TarHeaderIterator tarHeaderIterator = new TarHeaderIterator(tarFile);
        this.headers = Iterators.toArray(tarHeaderIterator, TarHeader.class);
        Arrays.sort(this.headers);
    }
    
    /**
     * @param key the path of the file, as saved in the tar archive (i.e. including directories)
     * @return if found, a {@link TarHeader} will be present in the response, if not it will be absent
     */
    public Optional<TarHeader> get(String key)
    {
        int index = Arrays.binarySearch(headers, TarHeader.buildMatcher(key));
        
        return index >= 0 ? Optional.of(this.headers[index]) : Optional.<TarHeader>absent(); 
    }

    /**
     * @return the tar archive this index was initialized with
     */
    public File getTarFile()
    {
        return tarFile;
    }
    
    /**
     * @return the number of indexed files
     */
    public long getSize()
    {
        return this.headers.length;
    }
    
    /**
     * @return the "last modified" property of the tar archive this index was initialized with (milliseconds trimmed)
     */
    public Date getLastModified()
    {
        return lastModified;
    }
    
    /**
     * Serialization is performed using standard java.io serialization + compression using the ZLIB library
     * @param file target file for the serialization
     * @throws IOException if the operation for any reasons fail
     */
    public void serialize(File file) throws IOException
    {
        FileOutputStream fileOut = null;
        try
        {
            fileOut = new FileOutputStream(file);
            serialize(fileOut);
        }
        finally
        {
            IOUtils.closeQuietly(fileOut);
        }
    }
    
    /**
     * Serialization is performed using standard java.io serialization + compression using the ZLIB library
     * @param out target output stream for the serialization
     * @throws IOException if the operation for any reasons fail
     */
    public void serialize(OutputStream out) throws IOException
    {
        ObjectOutputStream oOut = null;
        DeflaterOutputStream dos = null;
        
        try
        {
            Deflater def = new Deflater(Deflater.BEST_SPEED);
            
            dos = new DeflaterOutputStream(out, def);
            oOut = new ObjectOutputStream(dos);
            oOut.writeObject(this);
            
        } catch (Exception e)
        {
            throw new IOException("failed to serialize tarindex", e);
        }
        finally
        {
            IOUtils.closeQuietly(oOut);
            IOUtils.closeQuietly(dos);
        }
    }
    
    /**
     * Deserialization is performed using standard java.io serialization + decompression using the ZLIB library
     * @param file ths file to deserialize.
     * @return the deserialized {@link TarIndex}
     * @throws IOException if the operation for any reasons fail
     */
    public static TarIndex deserialize(File file) throws IOException
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
            return deserialize(fis);
        }
        finally
        {
            IOUtils.closeQuietly(fis);
        }
    }
    
    /**
     * Deserialization is performed using standard java.io serialization + decompression using the ZLIB library
     * @param input the input stream deserialize.
     * @return the deserialized {@link TarIndex}
     * @throws IOException if the operation for any reasons fail
     */
    public static TarIndex deserialize(InputStream input) throws IOException
    {
        ObjectInputStream ois = null;
        InflaterInputStream iis = null;
        try
        {
            iis = new InflaterInputStream(input);
            ois = new ObjectInputStream(iis);
            return (TarIndex)ois.readObject();
        }
        catch(Exception e)
        {
            throw new IOException("failed to deserialize tarindex", e);
        }
        finally
        {
            IOUtils.closeQuietly(ois);
            IOUtils.closeQuietly(iis);
        }
    }
}
