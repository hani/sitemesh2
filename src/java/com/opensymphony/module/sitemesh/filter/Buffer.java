/* This software is published under the terms of the OpenSymphony Software
 * License version 1.1, of which a copy has been included with this
 * distribution in the LICENSE.txt file. */
package com.opensymphony.module.sitemesh.filter;

import com.opensymphony.module.sitemesh.Factory;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.PageParser;
import com.opensymphony.module.sitemesh.util.FastByteArrayOutputStream;

import javax.servlet.ServletOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * When SiteMesh is activated for a request, the contents of the response are stored in this buffer, where they can
 * later be accessed as a parsed Page object.
 *
 * @author Joe Walnes
 * @version $Revision: 1.1 $
 */
public class Buffer {

    private final PageParser pageParser;
    private final String encoding;
    private final static TextEncoder TEXT_ENCODER = new TextEncoder();

    private CharArrayWriter bufferedWriter;
    private FastByteArrayOutputStream bufferedStream;
    private PrintWriter exposedWriter;
    private ServletOutputStream exposedStream;

    public Buffer(Factory factory, String contentType, String encoding) {
        this.encoding = encoding;
        pageParser = factory.getPageParser(contentType);
    }

    public Page parse() throws IOException {
        if (bufferedWriter != null) {
            return pageParser.parse(bufferedWriter.toCharArray());
        } else if (bufferedStream != null) {
            return pageParser.parse(TEXT_ENCODER.encode(bufferedStream.toByteArray(), encoding));
        } else {
            return pageParser.parse(new char[0]);
        }
    }

    public PrintWriter getWriter() {
        if (bufferedWriter == null) {
            if (bufferedStream != null) {
                throw new IllegalStateException("response.getWriter() called after response.getOutputStream()");
            }
            bufferedWriter = new CharArrayWriter(128);
            exposedWriter = new PrintWriter(bufferedWriter);
        }
        return exposedWriter;
    }

    public ServletOutputStream getOutputStream() {
        if (bufferedStream == null) {
            if (bufferedWriter != null) {
                throw new IllegalStateException("response.getOutputStream() called after response.getWriter()");
            }
            bufferedStream = new FastByteArrayOutputStream();
            exposedStream = new ServletOutputStream() {
                public void write(int b) {
                    bufferedStream.write(b);
                }
            };
        }
        return exposedStream;
    }

    public boolean isUsingStream() {
        return bufferedStream != null;
    }
}