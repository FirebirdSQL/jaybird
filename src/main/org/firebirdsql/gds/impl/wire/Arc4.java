/*
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.impl.wire;

/**
 * @author <a href="mailto:nakagami@gmail.com">Hajime Nakagami</a>
 */

public class Arc4 {
    private byte state[] = new byte[256];
    private int x = 0, y = 0;

    public Arc4(byte[] seed) {
        for (int i = 0; i< 256; i++) {
            state[i] = (byte)i;
        }
        int i1 =0, i2 = 0;

        for (int i = 0; i< 256; i++) {
            i2 = ((int)seed[i1] + (int)state[i] + i2) & 0xff;
            byte tmp = state[i];
            state[i] = state[i2];
            state[i2] = tmp;
            i1 = (i1 + 1) % seed.length;
        }
    }

    public void translate(byte[] buf, int off, int end) {
        for (int i=off; i < end; i++) {
            x = (x+1) & 0xff;
            y = (y + (int)state[x]) & 0xff;
            byte tmp = state[x];
            state[x] = state[y];
            state[y] = tmp;
            int xorIndex = ((int)state[x]+(int)state[y]) & 0xff;
            buf[i] = (byte)(buf[i] ^ state[xorIndex]);
        }
        return;
    }

    public void translate(byte[] buf, int len) {
        translate(buf, 0, len);
    }

    public void translate(byte[] buf) {
        translate(buf, 0, buf.length);
    }
}
