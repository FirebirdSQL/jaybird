package org.firebirdsql.jdbc.parser;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;


public class CaseInsensitiveStream extends ANTLRStringStream {

    
    public CaseInsensitiveStream() {
        super();
    }

    public CaseInsensitiveStream(char[] data, int numberOfActualCharsInArray) {
        super(data, numberOfActualCharsInArray);
    }

    public CaseInsensitiveStream(String input) {
        super(input);
    }

    public int LA(int i) {
        //return stream.LA(i);
        if ( i==0 ) {
            return 0; // undefined
        }
        if ( i<0 ) {
            i++; // e.g., translate LA(-1) to use offset 0
        }

        if ( (p+i-1) >= n ) {

            return CharStream.EOF;
        }
        return Character.toLowerCase(data[p+i-1]);

    }

}