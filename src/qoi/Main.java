package qoi;

import java.io.File;

/**
 * Main entry point of the program.
 * Converts an image between the "PNG" and "Quite Ok Image" (QOI) formats,
 * based on the extension of the file given as argument.
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @since 1.0
 */
public final class Main {

    private Main(){}

    /**
     * Main entry point to the program
     * @param args (String[]) - args[0] is the path to a .png or .qoi file to convert.
     * The converted file is written to the "res/" folder.
     */
    public static void main(String[] args){
        if(args.length != 1){
            System.out.println("Usage: java qoi.Main <image.png|image.qoi>");
            return;
        }

        String inputPath = args[0];
        String fileName = new File(inputPath).getName();
        int dot = fileName.lastIndexOf('.');
        if(dot < 0){
            System.out.println("File has no extension: " + inputPath);
            return;
        }
        String base = fileName.substring(0, dot);
        String extension = fileName.substring(dot + 1).toLowerCase();

        switch(extension){
            case "png" -> {
                pngToQoi(inputPath, base + ".qoi");
                System.out.println("Wrote res/" + base + ".qoi");
            }
            case "qoi" -> {
                qoiToPng(inputPath, base + ".png");
                System.out.println("Wrote res/" + base + ".png");
            }
            default -> System.out.println("Unsupported extension: ." + extension + " (expected .png or .qoi)");
        }
    }

    /**
     * Encodes a given file from "PNG" to "QOI"
     * @param inputFile (String) - The path of the file to encode
     * @param outputFile (String) - The path where to store the generated "Quite Ok Image"
     */
    public static void pngToQoi(String inputFile, String outputFile){
        var inputImage = Helper.readImage(inputFile);
        var outputFileContent = QOIEncoder.qoiFile(inputImage);
        Helper.write(outputFile, outputFileContent);
    }

    /**
     * Decodes a given file from "QOI" to "PNG"
     * @param inputFile (String) - The path of the file to decode
     * @param outputFile (String) - The path where to store the generated "PNG" Image
     */
    public static void qoiToPng(String inputFile, String outputFile){
        var inputFileContent = Helper.read(inputFile);
        var computedImage = QOIDecoder.decodeQoiFile(inputFileContent);
        Helper.writeImage(outputFile, computedImage);
    }

}
