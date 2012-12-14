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

import java.util.Arrays;

// Header
// 
// <pre>
// Offset  Size     Field
// 0       100      File name
// 100     8        File mode
// 108     8        Owner's numeric user ID
// 116     8        Group's numeric user ID
// 124     12       File size in bytes
// 136     12       Last modification time in numeric Unix time format
// 148     8        Checksum for header block
// 156     1        Link indicator (file type)
// 157     100      Name of linked file
// </pre>
// 
// 
// File Types
// 
// <pre>
// Value        Meaning
// '0'          Normal file
// (ASCII NUL)  Normal file (now obsolete)
// '1'          Hard link
// '2'          Symbolic link
// '3'          Character special
// '4'          Block special
// '5'          Directory
// '6'          FIFO
// '7'          Contigous
// </pre>
// 
// 
// 
// Ustar header
// 
// <pre>
// Offset  Size    Field
// 257     6       UStar indicator "ustar"
// 263     2       UStar version "00"
// 265     32      Owner user name
// 297     32      Owner group name
// 329     8       Device major number
// 337     8       Device minor number
// 345     155     Filename prefix
// </pre>

/**
 * This class wrappes a minumum of information about a tar file header (that's
 * the header for a file in the tar file). A tar file has the following very
 * simple format:<br>
 * 
 * <pre>
 * ---------------------------------------------------///-------------------------
 * | header: 512b | file data: x * 512b | header: 512b ... | >= 2 * paddig: 512b |
 * ---------------------------------------------------///-------------------------
 * </pre>
 * 
 * The header information wrapped by this class is:<br>
 * <ul>
 * <li>the name of the file</li>
 * <li>the size in bytes of the file</li>
 * <li>the type of file</li>
 * <li>the offset in bytes in the tar archive</li>
 * </ul>
 * To save memory, the {@link TarHeader} will not keep a reference to the tar file itself.
 */
public class TarHeader
{
    public static final int DATA_BLOCK = 512;
    public static final int HEADER_BLOCK = 512;

    /*
     * Header
     */
    public static final int NAMELEN = 100;
    public static final int MODELEN = 8;
    public static final int UIDLEN = 8;
    public static final int GIDLEN = 8;
    public static final int SIZELEN = 12;
    public static final int MODTIMELEN = 12;
    public static final int CHKSUMLEN = 8;
    public static final byte LF_OLDNORM = 0;

    /*
     * File Types
     */
    public static final byte LF_NORMAL = (byte) '0';
    public static final byte LF_LINK = (byte) '1';
    public static final byte LF_SYMLINK = (byte) '2';
    public static final byte LF_CHR = (byte) '3';
    public static final byte LF_BLK = (byte) '4';
    public static final byte LF_DIR = (byte) '5';
    public static final byte LF_FIFO = (byte) '6';
    public static final byte LF_CONTIG = (byte) '7';

    /*
     * Ustar header
     */
    public static final String USTAR_MAGIC = "ustar"; // POSIX
    public static final int USTAR_MAGICLEN = 8;
    public static final int USTAR_USER_NAMELEN = 32;
    public static final int USTAR_GROUP_NAMELEN = 32;
    public static final int USTAR_DEVLEN = 8;
    public static final int USTAR_FILENAME_PREFIX = 155;

    public static TarHeader build(byte[] bh, long fileOffset)
    {
        int offset = 0;

        TarHeader header = new TarHeader(fileOffset);

        header.name = TarHeader.parseName(bh, offset, TarHeader.NAMELEN);
        offset += TarHeader.NAMELEN;
        offset += TarHeader.MODELEN;
        offset += TarHeader.UIDLEN;
        offset += TarHeader.GIDLEN;
        header.size = parseOctal(bh, offset, TarHeader.SIZELEN);
        offset += TarHeader.SIZELEN;
        offset += TarHeader.MODTIMELEN;
        offset += TarHeader.CHKSUMLEN;
        header.linkFlag = bh[offset++];
        return header;
    }

    private String name;
    private long size;
    private byte linkFlag;
    private long tarFileOffset;

    private TarHeader(long fileOffset)
    {
        this.tarFileOffset = fileOffset;
    }

    /**
     * @return the name of the file denoted by this header
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the size of the file denoted by this header
     */
    public long getSize()
    {
        return size;
    }

    /**
     * @return the type of file denoted by this header
     *         <ul>
     *         <li>{@link #LF_NORMAL}</li>
     *         <li>{@link #LF_LINK}</li>
     *         <li>{@link #LF_SYMLINK}</li>
     *         <li>{@link #LF_CHR}</li>
     *         <li>{@link #LF_BLK}</li>
     *         <li>{@link #LF_DIR}</li>
     *         <li>{@link #LF_FIFO}</li>
     *         <li>{@link #LF_CONTIG}</li>
     *         </ul>
     */

    public byte getLinkFlag()
    {
        return linkFlag;
    }

    /**
     * @return the offset of the denoted file in the tar archive
     */
    public long getTarFileOffset()
    {
        return tarFileOffset;
    }

    /**
     * @see #getName()
     */
    @Override
    public String toString()
    {
        return this.name.toString();
    }

    /**
     * Parse an octal string from a header buffer. This is used for the file
     * permission mode value.
     * 
     * @param header
     *            The header buffer from which to parse.
     * @param offset
     *            The offset into the buffer from which to parse.
     * @param length
     *            The number of header bytes to parse.
     * 
     * @return The long value of the octal string.
     */
    private static long parseOctal(byte[] header, int offset, int length)
    {

        byte[] buf = Arrays.copyOfRange(header, offset, offset + length);
        String x = new String(buf).replaceAll("[^0-9]", "");

        return Long.parseLong(x, 8);
    }

    /**
     * Parse an entry name from a header buffer.
     * 
     * @param name
     * @param header
     *            The header buffer from which to parse.
     * @param offset
     *            The offset into the buffer from which to parse.
     * @param length
     *            The number of header bytes to parse.
     * @return The header's entry name.
     */
    public static String parseName(byte[] header, int offset, int length)
    {
        StringBuffer result = new StringBuffer(length);

        int end = offset + length;
        for (int i = offset; i < end; ++i)
        {
            if (header[i] == 0)
                break;
            result.append((char) header[i]);
        }

        return result.toString();
    }
}
