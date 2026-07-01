package qoi;

/**
 * Utility class to manipulate arrays.
 * @apiNote First Task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @since 1.0
 */
public final class ArrayUtils {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private ArrayUtils(){}

    // ==================================================================================
    // =========================== ARRAY EQUALITY METHODS ===============================
    // ==================================================================================

    /**
     * Check if the content of both arrays is the same
     * @param a1 (byte[]) - First array
     * @param a2 (byte[]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[] a1, byte[] a2){
        assert (a1 == null && a2 == null) || (a1 != null && a2 != null);

        if(a1 == null & a2 == null){
            return true;}
        if(a1.length != a2.length){
            return false;}
        for(int i = 0; i < a1.length; ++i){
            if(a1[i] != a2[i]){
                return false;}
        }
        return true;}

    /**
     * Check if the content of both arrays is the same
     * @param a1 (byte[][]) - First array
     * @param a2 (byte[][]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[][] a1, byte[][] a2){
        assert (a1 == null && a2 == null) || (a1 != null && a2 != null);

        if(a1 == null && a2 == null){
            return true;}
        if(a1.length != a2.length){
            return false;}
        for(int i = 0; i < a1.length; ++i){
            if((a1[i] == null & a2[i] != null) || (a1[i] != null & a2[i] == null)) {
                return false;}
            if(a1[i] != null & a2[i] !=null){
                if(a1[i].length != a2[i].length){
                    return false;}
                for(int j = 0; j < a1[i].length; ++j){
                    if(a1[i][j] != a2[i][j]){
                        return false;}
                }
            }
        }
        return true;
    }

    // ==================================================================================
    // ============================ ARRAY WRAPPING METHODS ==============================
    // ==================================================================================

    /**
     * Wrap the given value in an array
     * @param value (byte) - value to wrap
     * @return (byte[]) - array with one element (value)
     */
    public static byte[] wrap(byte value){
        byte[]wrappedValue = new byte[]{value};

        return wrappedValue;
    }

    // ==================================================================================
    // ========================== INTEGER MANIPULATION METHODS ==========================
    // ==================================================================================

    /**
     * Create an Integer using the given array. The input needs to be considered
     * as "Big Endian"
     * (See handout for the definition of "Big Endian")
     * @param bytes (byte[]) - Array of 4 bytes
     * @return (int) - Integer representation of the array
     * @throws AssertionError if the input is null or the input's length is different from 4
     */
    public static int toInt(byte[] bytes){
        assert bytes != null;
        assert bytes.length == 4;

        int value = bytes[0] << 24 | bytes[1] << 16 & 0xFF0000 | bytes[2] << 8 & 0xFF00 | bytes[3] & 0xFF;

        return value;
    }

    /**
     * Méthode identique à toInt, sauf que la fonction traitre un tableau RGBA
     */

    public static int toIntRGBA(byte[] bytes){
        assert bytes != null;
        assert bytes.length == 4;

        int value = bytes[3] << 24 | bytes[0] << 16 & 0xFF0000 | bytes[1] << 8 & 0xFF00 | bytes[2] & 0xFF;

        return value;
    }

    /**
     * Separate the Integer (word) to 4 bytes. The Memory layout of this integer is "Big Endian"
     * (See handout for the definition of "Big Endian")
     * @param value (int) - The integer
     * @return (byte[]) - Big Endian representation of the integer
     */
    public static byte[] fromInt(int value){
        byte[]bytes = new byte[4];

        int pos = 0;
        for(int j = 24; j >= 0; j-= 8){
            bytes[pos]=(byte)(value >> j);
            ++pos;
        }
        return bytes;
    }

    /**
     * Méthode identique à fromInt, sauf que la fonction traitre un tableau RGBA
     */

    public static byte[] fromIntRGBA(int value){
        byte[]bytes = new byte[4];

        int pos = 0;
        for(int j = 24; j >= 0; j-= 8){
            bytes[pos]=(byte)(value >> j);
            ++pos;
        }
        byte temp = bytes[0];
        for(int i = 0; i < 3; ++i){
            bytes[i] = bytes[i + 1];
        }
        bytes[3] = temp;

        return bytes;
    }

    // ==================================================================================
    // ========================== ARRAY CONCATENATION METHODS ===========================
    // ==================================================================================

    /**
     * Concatenate a given sequence of bytes and stores them in an array
     * @param bytes (byte ...) - Sequence of bytes to store in the array
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     */
    public static byte[] concat(byte ... bytes){
        assert bytes != null;

        int len = bytes.length;
        byte[] concatTab = new byte[len];
        for(int i = 0; i < len; ++i){
            concatTab[i]= bytes[i];
        }
        return concatTab;
    }

    /**
     * Concatenate a given sequence of arrays into one array
     * @param tabs (byte[] ...) - Sequence of arrays
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null.
     */
    public static byte[] concat(byte[] ... tabs){
        assert tabs != null;
        for(int i = 0; i < tabs.length; ++i){
            assert tabs[i] != null;
        }
        int len = 0;
        for(int i = 0; i < tabs.length; ++i) {
            len += tabs[i].length;}
        byte[] concatTab = new byte[len];

        int pos = 0;
        for(int i = 0; i < tabs.length; ++i){
            for(int j = 0; j < tabs[i].length; ++j){
                concatTab[pos] = tabs[i][j];
                ++pos;
            }
        }
        return concatTab;
    }

    // ==================================================================================
    // =========================== ARRAY EXTRACTION METHODS =============================
    // ==================================================================================

    /**
     * Extract an array from another array
     * @param input (byte[]) - Array to extract from
     * @param start (int) - Index in the input array to start the extract from
     * @param length (int) - The number of bytes to extract
     * @return (byte[]) - The extracted array
     * @throws AssertionError if the input is null or start and length are invalid.
     * start + length should also be smaller than the input's length
     */
    public static byte[] extract(byte[] input, int start, int length){
        assert input != null;
        assert 0 <= start && start < input.length;
        assert length >= 0;
        assert start + length <= input.length;

        byte[] extractTab = new byte[length];

        int pos = 0;
        for(int i = start; i < length + start; ++i){
            extractTab[pos] = input[i];
            ++pos;
        }
        return extractTab;
    }

    /**
     * Create a partition of the input array.
     * (See handout for more information on how this method works)
     * @param input (byte[]) - The original array
     * @param sizes (int ...) - Sizes of the partitions
     * @return (byte[][]) - Array of input's partitions.
     * The order of the partition is the same as the order in sizes
     * @throws AssertionError if one of the parameters is null
     * or the sum of the elements in sizes is different from the input's length
     */
    public static byte[][] partition(byte[] input, int ... sizes) {
        assert input != null;
        assert sizes != null;
        int partSum = 0;
        for(int i = 0; i < sizes.length; ++i){
            partSum += sizes[i];
        }
        assert partSum == input.length;

        byte[][]partitionTab = new byte[sizes.length][];
        for(int i = 0; i < sizes.length; ++i){
            partitionTab[i] = new byte[sizes[i]];
        }
        int pos = 0;
        for(int i = 0; i < sizes.length; ++i){
            for(int j = 0; j < sizes[i]; ++j) {
                partitionTab[i][j] = input[pos];
                ++pos;
            }
        }
        return partitionTab;
    }

    // ==================================================================================
    // ============================== ARRAY FORMATTING METHODS ==========================
    // ==================================================================================

    /**
     * Format a 2-dim integer array
     * where each dimension is a direction in the image to
     * a 2-dim byte array where the first dimension is the pixel
     * and the second dimension is the channel.
     * See handouts for more information on the format.
     * @param input (int[][]) - image data
     * @return (byte [][]) - formatted image data
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null
     */
    public static byte[][] imageToChannels(int[][] input){
        assert input != null;
        for(int i = 0; i < input.length; ++i){
            assert input[i] != null;
            assert input[i].length == input[0].length;
        }

        int pixelNumbers = input.length * input[0].length;
        byte[][] Channels = new byte[pixelNumbers][4];

        int pos = 0;
        for(int i = 0; i < input.length; ++i){
            for(int j = 0; j < input[0].length; ++j){
                Channels[pos]=fromIntRGBA(input[i][j]);
                ++pos;
            }
        }
        return Channels;
    }

    /**
     * Format a 2-dim byte array where the first dimension is the pixel
     * and the second is the channel to a 2-dim int array where the first
     * dimension is the height and the second is the width
     * @param input (byte[][]) : linear representation of the image
     * @param height (int) - Height of the resulting image
     * @param width (int) - Width of the resulting image
     * @return (int[][]) - the image data
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null
     * or input's length differs from width * height
     * or height is invalid
     * or width is invalid
     */
    public static int[][] channelsToImage(byte[][] input, int height, int width) {
        assert input != null;
        for(int i = 0; i < input.length; ++i){
            assert input[i] != null;
            assert input[i].length == 4;
        }
        assert height > 0;
        assert width > 0;
        assert height * width == input.length;

        int[][] image = new int[height][width];

        int pos = 0;
        for(int i = 0; i < height; ++i){
            for(int j = 0; j < width; ++j){
                image[i][j] = toIntRGBA(input[pos]);
                ++pos;
            }
        }
        return image;
    }
}
