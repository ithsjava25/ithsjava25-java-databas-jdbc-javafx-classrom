package com.example.cli;

public class InputReader {


    // Reads a line from System.in using Java 25 IO
    private static String readLine(InputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') break;
            buffer[len++] = (byte) b;
        }
        return new String(buffer, 0, len).trim();
    }


}
