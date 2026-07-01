package qoi;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * "Quite Ok Image" Encoder
 * @apiNote Second task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @since 1.0
 */
public final class QOIEncoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIEncoder(){}

    // ==================================================================================
    // ============================ QUITE OK IMAGE HEADER ===============================
    // ==================================================================================

    /**
     * Generate a "Quite Ok Image" header using the following parameters
     * @param image (Helper.Image) - Image to use
     * @throws AssertionError if the colorspace or the number of channels is corrupted or if the image is null.
     *  (See the "Quite Ok Image" Specification or the handouts of the project for more information)
     * @return (byte[]) - Corresponding "Quite Ok Image" Header
     */
    public static byte[] qoiHeader(Helper.Image image){
        assert image!= null;
        assert (image.channels() == QOISpecification.RGB | image.channels() == QOISpecification.RGBA);
        assert (image.color_space() == QOISpecification.sRGB | image.color_space() == QOISpecification.ALL);

        ArrayList<byte[]> qoiHeaderL = new ArrayList<>();

        byte[] qoiMagic = QOISpecification.QOI_MAGIC;
        qoiHeaderL.add(qoiMagic);

        byte[] qoiWidth = ArrayUtils.fromInt(image.data()[0].length);
        qoiHeaderL.add(qoiWidth);

        byte[] qoiHeight = ArrayUtils.fromInt(image.data().length);
        qoiHeaderL.add(qoiHeight);

        byte[] qoiChannels = ArrayUtils.wrap(image.channels());
        qoiHeaderL.add(qoiChannels);

        byte[] qoiColorSpace = ArrayUtils.wrap(image.color_space());
        qoiHeaderL.add(qoiColorSpace);

        byte[][] qoiHeaderTemp = qoiHeaderL.toArray(new byte[5][]);
        byte[] qoiHeader = ArrayUtils.concat(qoiHeaderTemp);

        return qoiHeader;
    }

    // ==================================================================================
    // ============================ ATOMIC ENCODING METHODS =============================
    // ==================================================================================

    /**
     * Encode the given pixel using the QOI_OP_RGB schema
     * @param pixel (byte[]) - The Pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
     */
    public static byte[] qoiOpRGB(byte[] pixel){
        assert pixel.length == 4;

        byte[] rgbPixel = ArrayUtils.extract(pixel, 0, 3);
        byte[] rgbTag = ArrayUtils.wrap(QOISpecification.QOI_OP_RGB_TAG);

        return ArrayUtils.concat(rgbTag, rgbPixel);
    }

    /**
     * Encode the given pixel using the QOI_OP_RGBA schema
     * @param pixel (byte[]) - The pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
     */
    public static byte[] qoiOpRGBA(byte[] pixel){
        assert pixel.length == 4;

        byte[] rgbaTag = ArrayUtils.wrap(QOISpecification.QOI_OP_RGBA_TAG);

        return ArrayUtils.concat(rgbaTag, pixel);
    }

    /**
     * Encode the index using the QOI_OP_INDEX schema
     * @param index (byte) - Index of the pixel
     * @throws AssertionError if the index is outside the range of all possible indices
     * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
     */
    public static byte[] qoiOpIndex(byte index){
        assert index >= 0;
        assert index < 64;

        return ArrayUtils.wrap((byte) (QOISpecification.QOI_OP_INDEX_TAG | index));

    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpDiff(byte[] diff){
        assert diff != null;
        assert diff.length == 3;
        for(int i = 0; i < diff.length; ++i){
            assert -3 < diff[i] && diff[i] < 2;
        }
        byte diffEncoded = (byte)
                (QOISpecification.QOI_OP_DIFF_TAG |
                diff[0] + 2 << 4 |
                diff[1] + 2 << 2 |
                diff[2] + 2);

        return ArrayUtils.wrap(diffEncoded);
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints
     * or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpLuma(byte[] diff){
        assert diff != null;
        assert diff.length == 3;
        assert -33 < diff[1] && diff[1] < 32;
        assert -9 < diff[0] - diff[1] && diff[0] - diff[1] < 8;
        assert -9 < diff[2] - diff[1] && diff[2] - diff[1] < 8;

        byte tag_dg = (byte) (QOISpecification.QOI_OP_LUMA_TAG | diff[1] + 32);
        byte dr_dgDiff = (byte) (diff[0] - diff[1]);
        byte db_dgDiff = (byte) (diff[2] - diff[1]);
        byte dr_db = (byte) (dr_dgDiff + 8 << 4 | db_dgDiff + 8);

        return new byte[]{tag_dg,dr_db};
    }

    /**
     * Encode the number of similar pixels using the QOI_OP_RUN schema
     * @param count (byte) - Number of similar pixels
     * @throws AssertionError if count is not between 0 (exclusive) and 63 (exclusive)
     * @return (byte[]) - Encoding of count
     */
    public static byte[] qoiOpRun(byte count){
        assert 0 < count && count < 63;

        byte runEncoded = (byte) (QOISpecification.QOI_OP_RUN_TAG | count - 1);

        return ArrayUtils.wrap(runEncoded);
    }

    // ==================================================================================
    // ============================== GLOBAL ENCODING METHODS  ==========================
    // ==================================================================================

    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
     * @param image (byte[][]) - Formatted image to encode
     * @return (byte[]) - "Quite Ok Image" representation of the image
     */
    public static byte[] encodeData(byte[][] image){
        assert image != null;
        for(int i = 0; i < image.length; ++i){
            assert image[i] != null;
            assert image[i].length == 4;
        }
        //Initialisation des variables nécessaires
        byte[] previousPixel = QOISpecification.START_PIXEL;
        byte[][] hashTable = new byte[64][4];
        int counter = 0;
        ArrayList<byte[]> qoiImageL = new ArrayList<>();

        for(int pixPos = 0; pixPos < image.length; ++pixPos){

            //1) Pixel répété
            if(ArrayUtils.equals(image[pixPos], previousPixel)){
                ++counter;
                if(counter == 62 || pixPos == image.length - 1){
                    qoiImageL.add(qoiOpRun((byte) counter));
                    counter = 0;
                }
            //1) Pas de pixel répété
            }else{
                if(counter != 0){
                    qoiImageL.add(qoiOpRun((byte) counter));
                    counter = 0;
                }
                //Pixel diff
                byte dr = (byte) (image[pixPos][0] - previousPixel[0]);
                byte dg = (byte) (image[pixPos][1] - previousPixel[1]);
                byte db = (byte) (image[pixPos][2] - previousPixel[2]);
                byte da = (byte) (image[pixPos][3] - previousPixel[3]);
                byte[] diff = new byte[]{dr, dg, db};

                //Pixel index
                byte index = QOISpecification.hash(image[pixPos]);

                //2) Pixel dans HashTable
                if(Arrays.equals(image[pixPos], hashTable[index])){
                    qoiImageL.add((qoiOpIndex(index)));

                //2) Pixel pas dans HashTable
                }else{
                    hashTable[index] = image[pixPos];

                    //3) Pixel avec alpha identique, petite différence
                    if(da == 0
                       && -3 < dr && dr < 2
                       && -3 < dg && dg < 2
                       && -3 < db && db < 2){
                        qoiImageL.add(QOIEncoder.qoiOpDiff(diff));

                    //4) Pixel avec alpha identique, petite différence de différence
                    }else if(da == 0
                            && -33 < dg && dg < 32
                            && -9 < dr - dg && dr - dg < 8
                            && -9 < db - dg && db - dg < 8){
                        qoiImageL.add(QOIEncoder.qoiOpLuma(diff));

                    //5) Pixel avec alpha identique
                    }else if(da == 0){
                        qoiImageL.add(QOIEncoder.qoiOpRGB(image[pixPos]));

                    //6) Pixel unique
                    }else{
                        qoiImageL.add(QOIEncoder.qoiOpRGBA(image[pixPos]));
                    }
                }
            }
            previousPixel = image[pixPos];
        }
        //Transformation de notre ArrayList en byte[]
        byte[][] qoiImageTemp = qoiImageL.toArray(new byte[0][]);
        byte[] qoiImage = ArrayUtils.concat(qoiImageTemp);

        return qoiImage;
    }


    /**
     * Creates the representation in memory of the "Quite Ok Image" file.
     * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
     * TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
     * @param image (Helper.Image) - Image to encode
     * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
     * @throws AssertionError if the image is null
     */
    public static byte[] qoiFile(Helper.Image image){
        assert image != null;

        ArrayList<byte[]> qoiFileL = new ArrayList<>();

        byte[] header = qoiHeader(image);
        qoiFileL.add(header);

        int[][] pixelTab = image.data();
        byte[][] channels = ArrayUtils.imageToChannels(pixelTab);
        byte[] qoiImage = encodeData(channels);
        qoiFileL.add(qoiImage);

        byte[] endOfFile = QOISpecification.QOI_EOF;
        qoiFileL.add(endOfFile);

        byte[][] qoiFileTemp = qoiFileL.toArray(new byte[3][]);
        byte[] qoiFile = ArrayUtils.concat(qoiFileTemp);

        return qoiFile;
    }

}