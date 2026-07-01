package qoi;

import static qoi.Helper.Image;

/**
 * "Quite Ok Image" Decoder
 * @apiNote Third task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @since 1.0
 */
public final class QOIDecoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIDecoder(){}

    // ==================================================================================
    // =========================== QUITE OK IMAGE HEADER ================================
    // ==================================================================================

    /**
     * Extract useful information from the "Quite Ok Image" header
     * @param header (byte[]) - A "Quite Ok Image" header
     * @return (int[]) - Array such as its content is {width, height, channels, color space}
     * @throws AssertionError See handouts section 6.1
     */
    public static int[] decodeHeader(byte[] header){
        assert header !=null;
        assert header.length == QOISpecification.HEADER_SIZE;
        byte[][] headerTemp = ArrayUtils.partition(header,4,4,4,1,1);
        assert ArrayUtils.equals(headerTemp[0], QOISpecification.QOI_MAGIC);
        assert (header[12] == QOISpecification.RGB | header[12] == QOISpecification.RGBA);
        assert (header[13] == QOISpecification.sRGB | header[13] == QOISpecification.ALL);


        int[] decodeHeader = new int[4];

        int width = ArrayUtils.toInt(headerTemp[1]);
        decodeHeader[0] = width;

        int height = ArrayUtils.toInt(headerTemp[2]);
        decodeHeader[1] = height;

        int channels = headerTemp[3][0];
        decodeHeader[2] = channels;

        int colorSpace = headerTemp[4][0];
        decodeHeader[3] = colorSpace;

        return decodeHeader;

    }

    // ==================================================================================
    // =========================== ATOMIC DECODING METHODS ==============================
    // ==================================================================================

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param input (byte[]) - Stream of bytes to read from
     * @param alpha (byte) - Alpha component of the pixel
     * @param position (int) - Index in the buffer
     * @param idx (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.1
     */
    public static int decodeQoiOpRGB(byte[][] buffer, byte[] input, byte alpha, int position, int idx){
        assert buffer != null;
        assert input != null;
        assert position >= 0 && position < buffer.length;
        assert idx >= 0 && idx < input.length;
        assert input.length >= 3;

        byte[] APixel = ArrayUtils.wrap(alpha);
        byte[] RGBPixel = ArrayUtils.extract(input,idx,3);
        byte[] RGBAPixel = ArrayUtils.concat(RGBPixel, APixel);
        buffer[position] = RGBAPixel;

        return 3;
    }

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param input (byte[]) - Stream of bytes to read from
     * @param position (int) - Index in the buffer
     * @param idx (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.2
     */
    public static int decodeQoiOpRGBA(byte[][] buffer, byte[] input, int position, int idx){
        assert buffer != null;
        assert input != null;
        assert position >= 0 && position < buffer.length;
        assert idx >= 0 && idx < input.length;
        assert input.length >= 4;

        byte[] ARGBPixel = ArrayUtils.extract(input,idx,4);
        buffer[position] = ARGBPixel;

        return 4;
    }

    /**
     * Create a new pixel following the "QOI_OP_DIFF" schema.
     * @param previousPixel (byte[]) - The previous pixel
     * @param chunk (byte) - A "QOI_OP_DIFF" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.4
     */
    public static byte[] decodeQoiOpDiff(byte[] previousPixel, byte chunk){
        assert previousPixel != null;
        assert previousPixel.length == 4;
        byte tag = (byte) (chunk & 0b11_00_00_00);
        assert tag == QOISpecification.QOI_OP_DIFF_TAG;

        byte dr = (byte) (((chunk & 0b11_00_00) >>> 4) -2);
        byte dg = (byte) (((chunk & 0b11_00) >>> 2) - 2);
        byte db = (byte) ((chunk & 0b11) - 2);

        byte[] currentPixel = new byte[4];
        currentPixel[0] = (byte) (previousPixel[0] + dr);
        currentPixel[1] = (byte) (previousPixel[1] + dg);
        currentPixel[2] = (byte) (previousPixel[2] + db);
        currentPixel[3] = previousPixel[3];

        return currentPixel;
    }

    /**
     * Create a new pixel following the "QOI_OP_LUMA" schema
     * @param previousPixel (byte[]) - The previous pixel
     * @param data (byte[]) - A "QOI_OP_LUMA" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.5
     */
    public static byte[] decodeQoiOpLuma(byte[] previousPixel, byte[] data){
        assert previousPixel != null;
        assert data != null;
        assert previousPixel.length == 4;
        byte tag = (byte) (data[0] & 0b11_00_00_00);
        assert tag == QOISpecification.QOI_OP_LUMA_TAG;

        byte dg = (byte) ((data[0] & 0b11_11_11)-32);
        byte dr = (byte) (((data[1] & 0b11_11_00_00) >>> 4) - 8 + dg);
        byte db = (byte) (((data[1] & 0b11_11)) - 8 + dg);

        byte[] currentPixel = new byte[4];
        currentPixel[0] = (byte) (previousPixel[0] + dr);
        currentPixel[1] = (byte) (previousPixel[1] + dg);
        currentPixel[2] = (byte) (previousPixel[2] + db);
        currentPixel[3] = previousPixel[3];

        return currentPixel;
    }

    /**
     * Store the given pixel in the buffer multiple times
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param pixel (byte[]) - The pixel to store
     * @param chunk (byte) - a QOI_OP_RUN data chunk
     * @param position (int) - Index in buffer to start writing from
     * @return (int) - number of written pixels in buffer
     * @throws AssertionError See handouts section 6.2.6
     */
    public static int decodeQoiOpRun(byte[][] buffer, byte[] pixel, byte chunk, int position){
        assert buffer != null;
        assert position >= 0 && position < buffer.length;
        assert pixel != null;
        assert pixel.length == 4;
        assert buffer[0].length == 4;

        int count = (chunk & 0b00_11_11_11) + 1;
        assert position + count <= buffer.length;
        for(int i = position; i < count + position; ++i){
            buffer[i] = pixel;
        }

        return count-1;
    }

    // ==================================================================================
    // ========================= GLOBAL DECODING METHODS ================================
    // ==================================================================================

    /**
     * Decode the given data using the "Quite Ok Image" Protocol
     * @param data (byte[]) - Data to decode
     * @param width (int) - The width of the expected output
     * @param height (int) - The height of the expected output
     * @return (byte[][]) - Decoded "Quite Ok Image"
     * @throws AssertionError See handouts section 6.3
     */
    public static byte[][] decodeData(byte[] data, int width, int height){
        assert data != null;
        assert width > 0;
        assert height > 0;
        assert data.length > 0;

        //Initialisation des variables nécessaires
        byte[] previousPixel = QOISpecification.START_PIXEL;
        byte[][] buffer = new byte[width*height][4];
        byte[][] hashTable = new byte[64][4];
        byte rgbaTag = QOISpecification.QOI_OP_RGBA_TAG;
        byte rgbTag = QOISpecification.QOI_OP_RGB_TAG;
        byte lumaTag = QOISpecification.QOI_OP_LUMA_TAG;
        byte diffTag = QOISpecification.QOI_OP_DIFF_TAG;
        byte indexTag = QOISpecification.QOI_OP_INDEX_TAG;
        int position = 0;

        for(int idx = 0; idx < data.length; ++idx){
            if((data[idx] & (byte) 0b11_00_00_00) != indexTag){

                if(data[idx] == rgbaTag){
                    idx += decodeQoiOpRGBA(buffer,data,position,idx + 1);

                }else if(data[idx] == rgbTag){
                    idx += decodeQoiOpRGB(buffer,data,previousPixel[3],position,idx + 1);

                }else if((data[idx] & (byte) 0b11_00_00_00) == lumaTag){
                    byte[] chunk = {data[idx], data[idx+1]};
                    buffer[position] = decodeQoiOpLuma(previousPixel,chunk);
                    ++idx;

                }else if((data[idx] & (byte) 0b11_00_00_00) == diffTag){
                    buffer[position] = decodeQoiOpDiff(previousPixel,data[idx]);

                }else{//On a forcément le tag run
                    position += decodeQoiOpRun(buffer,previousPixel,data[idx],position);
                }
                byte index = QOISpecification.hash(buffer[position]);
                hashTable[index] = buffer[position];

            }else{
                buffer[position] = hashTable[data[idx]];
            }
            previousPixel = buffer[position];
            ++position;
        }
        return buffer;
    }

    /**
     * Decode a file using the "Quite Ok Image" Protocol
     * @param content (byte[]) - Content of the file to decode
     * @return (Image) - Decoded image
     * @throws AssertionError if content is null
     */
    public static Image decodeQoiFile(byte[] content){
        assert content != null;
        int signPos = content.length - QOISpecification.QOI_EOF.length;
        byte[] signature = ArrayUtils.extract(content, signPos, QOISpecification.QOI_EOF.length);
        assert ArrayUtils.equals(QOISpecification.QOI_EOF, signature);

        byte[] header = ArrayUtils.extract(content,0, 14);
        int[] decodedHeader = decodeHeader(header);
        int width = decodedHeader[0];
        int height = decodedHeader[1];
        byte channels = (byte) decodedHeader[2];
        byte colorSpace = (byte) decodedHeader[3];

        int dataLength = content.length - QOISpecification.HEADER_SIZE - QOISpecification.QOI_EOF.length;
        byte[]data = ArrayUtils.extract(content, 14, dataLength);
        byte[][] buffer = decodeData(data,width,height);
        int[][] imageTemp = ArrayUtils.channelsToImage(buffer, height, width);
        Helper.Image image = Helper.generateImage(imageTemp, channels, colorSpace);

        return image;
    }
}